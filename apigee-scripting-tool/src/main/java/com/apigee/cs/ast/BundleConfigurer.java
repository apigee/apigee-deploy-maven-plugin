package com.apigee.cs.ast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jruby.RubyException;
import org.jruby.RubyObject;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apigee.cs.util.ZipUtils;
import com.apigee.cs.util.crypto.CryptoException;
import com.apigee.cs.util.crypto.CryptoUtil;

/**
 * BundleConfigurer is the core of the environment-specific bundle configuration process.
 * @author rob
 *
 */
public class BundleConfigurer {

	Logger log = LoggerFactory.getLogger(BundleConfigurer.class);
	File sourceBundle;

	File rubyTransformSource;
	File tempWorkspaceDir;
	File targetBundle;
	CryptoUtil cryptoUtil;
	ScriptingContainer rubyScriptingContainer = new ScriptingContainer(
			LocalVariableBehavior.PERSISTENT);
	String profileName;

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String environment) {
		this.profileName = environment;
	}

	public File getSourceBundle() {
		return sourceBundle;
	}

	public void setTargetBundle(File file) {
		targetBundle = file;
	}

	public File getTargetBundle() {
		return targetBundle;
	}

	public void setSourceBundle(File sourceBundle) {
		this.sourceBundle = sourceBundle;
	}

	public File getTempWorkspaceDir() {
		return tempWorkspaceDir;
	}

	public void createTempWorkspace(File f) {
		log.debug("Creating temporary workspace: " + f.getAbsolutePath());
		f.mkdirs();
		this.tempWorkspaceDir = f;
	}

	public void createTempWorkspace() {
		try {
			File f = new File(File.createTempFile("temp", ".tmp")
					.getParentFile(), "" + System.currentTimeMillis());

			createTempWorkspace(f);

		} catch (IOException e) {
			throw new AstException(e);
		}
	}

	public void unzipBundleIntoWorkspace(File sourceBundleFile) {

		if (tempWorkspaceDir == null || !tempWorkspaceDir.exists()
				|| !tempWorkspaceDir.isDirectory()) {
			throw new IllegalStateException("Workspace location not found: "
					+ tempWorkspaceDir);
		}
		if (sourceBundleFile == null || !sourceBundleFile.exists()
				|| !sourceBundleFile.isFile()) {
			throw new IllegalStateException("Source bundle not found: "
					+ sourceBundleFile);
		}
		ZipUtils zu = new ZipUtils();
		zu.unzipArchive(sourceBundleFile, tempWorkspaceDir);

		// zu.unzipArchive(sourceBundleFile, workspaceLocation);
	}

	public boolean isConfigurationAvailable() {
		boolean available = true;
		if (!getEnvironmentYaml().exists()) {
			log.warn("Missing config: " + getEnvironmentYaml());
			available = false;
		}

		if (getProfileName() == null || getProfileName().trim().length() < 1) {
			available = false;
		}
		return available;
	}

	protected File getEnvironmentYaml() {
		return new File(getTempWorkspaceDir(), "application/conf/config.yml");
	}

	protected File getEnvironmentRuby() {
		return new File(getTempWorkspaceDir(), "application/conf/config.rb");
	}

	/**
	 * Initialize the jruby environment.
	 * @throws IOException
	 */
	protected void initScriptingContainer() throws IOException {
		
		// Too bad that Java doesn't support heredocs or multi-line strings.  It would make embedding script code in java source
		// so much easier.  The bridge back and forth between java and ruby is a bit complicated here, but very powerful.
		
		// I'm pretty sure there is a better way to do all of this, but this works.
		
		rubyScriptingContainer.runScriptlet("require 'yaml'");
		rubyScriptingContainer.runScriptlet("require 'rexml/document'");
		rubyScriptingContainer.runScriptlet("require 'logger'");

		rubyScriptingContainer.runScriptlet("include REXML");
		rubyScriptingContainer.put(
				"$ASTLOG",
				LoggerFactory.getLogger(BundleConfigurer.class.getName()
						+ ".helper.rb"));
		
		
		// helper.rb contains the key ruby code that we use to process things.
		// This could be factored out
		rubyScriptingContainer.runScriptlet(getClass().getClassLoader()
				.getResourceAsStream("helper.rb"), "helper.rb");

		IOFileFilter ff = new IOFileFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		
		// Find all the xml files
		Collection<File> c = (Collection<File>) FileUtils.listFiles(
				getTempWorkspaceDir(), new SuffixFileFilter(".xml"), ff);
		List<RubyObject> tmp = new ArrayList<RubyObject>();

		// Now for each file, instantiate a ruby class AstXmlHolder (defined in helper.rb)
		for (File f : c) {

			// Parse the XML with REXML
			// Set the profile to be used
			// Parse the config.yml 
			String scriptlet = "tmp = AstXmlHolder.new\n" + "tmp.parse_xml '"
					+ f.getCanonicalPath() + "'\n" + 
					"tmp.profile='" + getProfileName() + "'\n" + 
					
					"tmp.parse_yaml('"
					+ getEnvironmentYaml().getCanonicalPath() + "')\n";

			rubyScriptingContainer.runScriptlet(scriptlet);

			// Make the currently executing (this) instance available to ruby by passing it as a scripting variable
			rubyScriptingContainer.put("bundle_configurer",this);
			
			// Also set it on the AstXmlHolder
			rubyScriptingContainer.runScriptlet("tmp.bundle_configurer=bundle_configurer");
			
			
			// Now get the AstXmlHolder back as a RubyObject
			RubyObject tmpx = (RubyObject) rubyScriptingContainer.get("tmp");

			// Add it to the array of AstXmlHolder
			tmp.add((RubyObject) rubyScriptingContainer.get("tmp"));

			// Remember, nothing has really been MODIFIED yet
		}

		// Now set the array of RubyObjects and make it available back to ruby
		rubyScriptingContainer.put("xml_files",
				(Object) tmp.toArray(new RubyObject[0]));

	}

	public void configure() {

		Reader rubyTransformReader = null;
		try {
			cryptoUtil = new CryptoUtil();
			cryptoUtil.loadKeyStore();
		}
		catch (CryptoException e) {
			log.warn("Could not locate keystore.  Crypto will be disabled.  "+e.toString());
		}
		try {
			
			// Look for config.rb
			File rubyTransformFile = getEnvironmentRuby();
			if (rubyTransformFile == null || !rubyTransformFile.exists()) {
				rubyTransformFile = null;
			} else {
				rubyTransformReader = new FileReader(rubyTransformFile);
			}
			if (getTargetBundle() == null) {
				throw new IllegalStateException("Must set target bundle file");
			}

			// Initialize the jruby interpreter
			initScriptingContainer();

			// Now process config.yml
			// Note that xml_files is an array of all the XML files in the bundle.  
			// So here, we just iterate across them with a ruby closure
			rubyScriptingContainer
					.runScriptlet("xml_files.each { |f| $ASTLOG.debug \"processing #{f.file}\"\nf.execute }");
			
			
			// Process config.rb if it exists
			if (rubyTransformReader != null) {
				
				rubyScriptingContainer.runScriptlet(rubyTransformReader,
						getEnvironmentRuby().getAbsolutePath());
			}

			// Now the in-memory version of the parsed XML files may have been modified.  No we have to write them back out to the filesystem.
			rubyScriptingContainer
			.runScriptlet("xml_files.each { |f| f.write_file }");
	
			// Now zip everything back up in a new bundle
			log.debug("" + getTempWorkspaceDir());
			ZipUtils zu = new ZipUtils();
			zu.zipDir(getTargetBundle(), getTempWorkspaceDir(), null);
			
			// Done!!!
			
		} catch (IOException e) {
			throw new AstException(e);
		} finally {
			cleanupTempFiles();
			if (rubyTransformReader != null) {
				try {
					rubyTransformReader.close();
				} catch (IOException ex) {
					log.error("", ex);
				}
			}
		}
	}
	
	
	public void cleanupTempFiles() {
		FileUtils.deleteQuietly(getTempWorkspaceDir());
	}
	
	/**
	 * Pass config.yml values through an expression processor.  This could be made very very powerful.
	 * @param val
	 * @return
	 */
	public String evalExpression(String val) {
		
		String rval = val;
		if (val==null) {
			// No-op
		}
		else if (val.startsWith("{AES}")){
			rval = cryptoUtil.decryptString(val);
			log.debug("evalExpression("+val+") => '"+rval+"'");
		}
		else if (val.startsWith("#{")) {
			// TODO:  This would be VERY POWERFUL, if we see this syntax and pass it to the ruby interpreter.
			throw new UnsupportedOperationException("Ruby expression not supported...YET");
		}
		
		return rval;
	}
}

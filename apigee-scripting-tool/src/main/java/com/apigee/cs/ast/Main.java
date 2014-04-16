package com.apigee.cs.ast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.sonoa.security.util.DESBase64MaskingStrategy;
import com.sonoa.security.util.EncryptionUtil;

/**
 * Entry point for AST when running from shell directly.
 * @author rob
 *
 */
public class Main {

	static Logger log = LoggerFactory.getLogger(Main.class);

	// This ruby scripting variable will be available once the AST JRuby runtime is initialized
	public static final String BEAN_MGMT_CLIENT_FACTORY = "ast";

	String ARGV[];


	public static void main(String[] args) throws Exception {
		System.out.println(System.getProperty("http.nonProxyHosts"));

		// Map all java.util.logging calls to slf4j because JUL logging sucks
		SLF4JBridgeHandler.install();

		// Old version of Axis2 has a bug with non-proxy-hosts on Mac
		// We should probably be more precise about clobbering this variable.  That is, 
		// Only unset *if* non-proxyHosts contains the problematic characters and the platform is mac
		fixOSXNonProxyHosts();

		Main m = new Main(args);
		m.executeScript();

	}

	public Main(String[] args) {
		this.ARGV = args;
	}

	public void executeScript() {

		try {
			ScriptingContainer container = new ScriptingContainer();
			container.setClassLoader(this.getClass().getClassLoader());
			container.runScriptlet("require 'java'");
			if (log.isDebugEnabled()) {
				int count = 0;
				for (String path : container.getLoadPaths()) {
					log.debug("JRuby load path [" + (count++) + "]: " + path);
				}
				count = 0;
				for (String s : ARGV) {
					log.debug("ARGV[" + (count++) + "]: " + s);
				}
			}
			ManagementClientFactory clientFactory = new ManagementClientFactory();

			container.put(BEAN_MGMT_CLIENT_FACTORY, clientFactory);
			// Set ARGV that script will see
			container.setArgv(ARGV);
			container.put("ARGV", ARGV);

			EncryptionUtil
					.setDefaultMaskingStrategy(new DESBase64MaskingStrategy());

			if (ARGV.length < 1) {
				System.err.println("Usage: ast <FILE> [ARG...]");
				System.exit(1);
			} else {
				File scriptFile = new File(ARGV[0]);
				if (!scriptFile.exists()) {
					System.err.println("ERROR: File not found - "+scriptFile.getCanonicalPath());
					System.exit(1);
				}
				container.runScriptlet(new FileReader(scriptFile),
						scriptFile.getCanonicalPath());
			}

		} catch (FileNotFoundException e) {
			throw new AstException(e);
		} catch (IOException e) {
			throw new AstException(e);
		} catch (AstException e) {
			throw e;
		} catch (Exception e) {
			// Who writes code that declares methods with "throws Exception"
			// WHO? WHO? Aaargh!!
			throw new AstException(e);
		}

	}

	public ScriptingContainer newJRubyScriptingContainer() {
		return new ScriptingContainer();
	}
	
	protected static void fixOSXNonProxyHosts() {
		
		// This is required to work around a Mac OS X bug.  Probably would be better to be more selective about this.  Only override if we're
		// actually on OS X.  Or look at the value and only unset if it contains the problematic wildcard value.
		try {
			String osName = System.getProperty("os.name");
			String nonProxyHosts = System.getProperty("http.nonProxyHosts");
			if (osName!=null && osName.toLowerCase().contains("mac") && nonProxyHosts!=null && nonProxyHosts.contains("*")) {
				// Log this at debug to keep things quiet
				log.debug("System property http.nonProxyHosts contains characters that break Axis2.");
				log.debug("Setting this value to empty string to work around this problem.");
				System.setProperty("http.nonProxyHosts", "");	
			}
			
		}
		catch (RuntimeException e) {
			// just try to continue
			log.warn("",e);
		}
	}
}

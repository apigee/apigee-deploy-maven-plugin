/**
 * Copyright (C) 2014 Apigee Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public class GitUtil {
	Git git;
	
	public GitUtil(File f) {
		try {
			File workspace = findWorkspaceRoot(f);
			if (workspace==null) {
				throw new IllegalStateException("Does not appear to be a Git workspace: "+f.getCanonicalPath());
			}
			Repository r = new RepositoryBuilder().setWorkTree(findWorkspaceRoot(f)).build();
			git = new Git(r);
		}
		catch (IOException e) {
		    throw new AstException(e);
		}
	}
	public Git getGit() {
		return git;
	}
	
	public static File findWorkspaceRoot(File f) {
		if (f==null) {
			return null;
		}
		File gitDir = new File(f,".git");
		if (gitDir.exists() && gitDir.isDirectory()) {
			return f;
		}
		else {
			return findWorkspaceRoot(f.getParentFile());
		}
	}
	public  String getWorkspaceHeadRevisionString() {
	
	
		Ref headRef = getWorkspaceHeadRevision();
		
		String headRevision = headRef.getObjectId().getName();
		return headRevision;
	
	}
	public  Ref getWorkspaceHeadRevision() {
		Map<String, Ref> refs = git.getRepository().getAllRefs();
	
		Ref headRef = refs.get("HEAD");
		return headRef;
	
	}

	/**
	 * IMP NOTE: Tag number will be displayed only if git tags are peeled manually using the command "git gc"
	 * @return
	 */
	public  String getTagNameForWorkspaceHeadRevision() {
		String headRevision = getWorkspaceHeadRevisionString();
		
		Map<String, Ref> tags = git.getRepository().getAllRefs();
		for (Ref tagRef : tags.values()) {
			String tagName = tagRef.getName();
			ObjectId obj = tagRef.getPeeledObjectId();
			if (obj == null) obj = tagRef.getObjectId();
//			 System.out.println(">>>>>>>>>>>>>>>>>" + tagRef.getName() + "," +
//			 obj.getName() +"," + headRevision  );
			if (headRevision.equals(obj.getName())) {
				if (tagName.contains("/tags/")) {
					int lastSlashIndex = tagName.lastIndexOf("/");
					if (lastSlashIndex > 0) {
						tagName = tagName.substring(lastSlashIndex + 1);
						return tagName;
					}
				

				}
			}

		}
		return null;
	}
	

}

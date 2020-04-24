/**
 * Copyright (C) 2014 Apigee Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.mavenplugin;

import static java.lang.String.format;

import io.apigee.buildTools.enterprise4g.rest.Bundle;
import io.apigee.buildTools.enterprise4g.rest.RestClient;
import io.apigee.buildTools.enterprise4g.utils.ServerProfile;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;

/**
 * Goal to upload 4g gateway bundle on server
 *
 * @author rmishra
 * @goal deploy
 * @phase package
 */
public class DeployMojo extends GatewayAbstractMojo {

	/**
	 * the REST client to talk to the management API
	 */
	private RestClient client;

	/**
	 * Entry point for the mojo.
	 */
	public void execute() throws MojoExecutionException {

		if (isSkip()) {
			getLog().info("Skipping");
			return;
		}

		ServerProfile profile = createProfile();

		client = new RestClient(profile);

		Bundle bundle = createBundle();

		// below logic handles 4 distinct scenarios
		// 1) override a bundle
		// 2) update a bundle
		// 3) clean/delete a bundle
		// 4) anything else is just an upload

		try {
			if (profile.isOverride()) {
				overrideBundle(bundle);
			} else if (profile.isUpdate()) {
				updateBundle(bundle);
			} else if (profile.isClean()) {
				deleteBundle(bundle);
			} else {
				Long bundleRevision = client.uploadBundle(getApplicationBundlePath(), bundle);
				if (!profile.isInactive()) {
					client.refreshBundle(bundle.clone(bundleRevision));
				}
			}

		} catch (IOException ioe) {
			throw new MojoExecutionException(ioe.getMessage(), ioe);
		}

	}

	private void updateBundle(Bundle bundle) throws IOException {
		ServerProfile profile = client.getProfile();
		Long activeRevision = client.getDeployedRevision(bundle);

		// TODO there appear to be holes in the processing logic of update
		// 1. active and bundle revision
		// 2. no active revision but a bundle revision
		// 		Does that mean we just upload any odd revision and then bail?
		// 		Why are other decision branches below also activating bundles and this one does not?
		//
		// 3. active revision but no specific bundle revision
		// 4. no active or bundle revision

		// Logic is faulty an will result in an endless increment in revision numbers if someone tries to update
		// a module with a faulty one.

		// If revision is passed to the module, update that revision
		if (bundle.getRevision() != null) {

			getLog().info(format("Updating %s/%s revision %d (current active revision is %d)",
					bundle.getType().name().toLowerCase(),
					bundle.getName(),
					bundle.getRevision(),
					activeRevision == null ? -1 : activeRevision));

			Long updatedRevision = client.updateBundle(getApplicationBundlePath(), bundle);

			getLog().info(format("Updated %s/%s revision %d",
					bundle.getType().name().toLowerCase(),
					bundle.getName(),
					updatedRevision));

		} else {
			if (activeRevision != null) {
				//if there is a revision deployed in this environment we will update this one

				getLog().info(format("Updating %s/%s active revision %d",
						bundle.getType().name().toLowerCase(),
						bundle.getName(),
						activeRevision));

				Long updatedRevision = client.updateBundle(getApplicationBundlePath(), bundle.clone(activeRevision));
				//if (!profile.isInactive()) {
				//	client.activateBundleRevision(bundle.clone(updatedRevision));
				//}

				getLog().info(format("Updated %s/%s revision %d",
						bundle.getType().name().toLowerCase(),
						bundle.getName(),
						updatedRevision));

			} else {
				// no revision deployed, lets upload as a new revision
				// FIXME this should actually throw an error as neither a revision to update is specified nor any revision is active that could be updated. Also documented in https://github.com/apigee/apigee-deploy-maven-plugin/issues/46#issuecomment-225464906.

				getLog().info(format("Upload %s/%s as new revision",
						bundle.getType().name().toLowerCase(), bundle.getName()));

				Long updatedRevision = client.uploadBundle(getApplicationBundlePath(), bundle);

				if (!profile.isInactive()) {
					client.activateBundleRevision(bundle.clone(updatedRevision));
				}

				getLog().info(format("Uploaded %s/%s as new revision %d",
						bundle.getType().name().toLowerCase(),
						bundle.getName(),
						updatedRevision));

			}
		}
	}

	private void overrideBundle(Bundle bundle) throws IOException {
		Long activeRevision = client.getDeployedRevision(bundle);
		Long uploadedRevision = client.uploadBundle(getApplicationBundlePath(), bundle.clone(activeRevision));
		client.activateBundleRevision(bundle.clone(uploadedRevision));
	}

	private void deleteBundle(Bundle bundle) throws IOException {
		Long activeRevision = client.getDeployedRevision(bundle);
		if (activeRevision != null) {
			// TODO Why do we need a revision? Isn't the entire bundle deleted when we do this?
			client.deleteBundle(bundle.clone(activeRevision));
		} else {
			getLog().info("No active revision for " + client.getProfile().getEnvironment()
					+ " environment. Nothing to delete");
		}
	}

}





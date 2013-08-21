/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.deployment.spring;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.engine.spring.application.SpringServletProcessApplication;
import org.junit.Assert;

/**
 * @author Daniel Meyer
 *
 */
public class CustomSpringServletProcessApplication extends SpringServletProcessApplication {

  private boolean isPostDeployInvoked = false;
  private boolean isPreUndeployInvoked = false;

  @PostDeploy
  public void postDeploy() {
    isPostDeployInvoked = true;
  }

  @PreUndeploy
  public void preUndeploy() {
    isPreUndeployInvoked = true;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.assertFalse(isPostDeployInvoked);
    super.afterPropertiesSet();
    Assert.assertTrue("@PostDeploy Method not invoked", isPostDeployInvoked);
  }

  @Override
  public void destroy() throws Exception {
    Assert.assertFalse(isPreUndeployInvoked);
    super.destroy();
    Assert.assertTrue("@PreUndeploy Method not invoked", isPreUndeployInvoked);
  }

}

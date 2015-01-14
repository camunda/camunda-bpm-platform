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
package org.camunda.bpm.tasklist.test.util;

import org.camunda.bpm.cockpit.test.util.DeploymentHelper;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 * @author Roman Smirnov
 */
public class AbstractTasklistCoreTest {

  public static WebArchive createBaseDeployment() {
    return createBaseDeployment("test.war");
  }

  public static WebArchive createBaseDeployment(String archiveName) {
    return DeploymentHelper.getAdminWar(archiveName)
      .addAsLibraries(DeploymentHelper.getFestAssertions())
      .addClass(AbstractTasklistCoreTest.class);
  }

}

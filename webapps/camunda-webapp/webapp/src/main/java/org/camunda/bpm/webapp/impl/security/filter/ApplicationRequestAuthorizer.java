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
package org.camunda.bpm.webapp.impl.security.filter;

import java.util.Map;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.Authentications;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthentication;

/**
 * <p>This matcher can be used for restricting access to an app.</p>
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public class ApplicationRequestAuthorizer implements RequestAuthorizer {

  @Override
  public Authorization authorize(Map<String, String> parameters) {
    Authentications authentications = Authentications.getCurrent();

    if (authentications == null) {
      // the user is not authenticated
      // grant user anonymous access
      return grantAnnonymous();
    } else {
      String engineName = parameters.get("engine");
      String appName = parameters.get("app");

      Authentication engineAuth = authentications.getAuthenticationForProcessEngine(engineName);
      if (engineAuth == null) {
        // the user is not authenticated
        // grant user anonymous access
        return grantAnnonymous();
      }

      // get process engine
      ProcessEngine processEngine = Cockpit.getProcessEngine(engineName);
      if (processEngine == null) {
        // the process engine does not exist
        // grant user anonymous access
        return grantAnnonymous();
      }

      // check authorization
      if (engineAuth instanceof UserAuthentication) {
        UserAuthentication userAuth = (UserAuthentication) engineAuth;

        if (userAuth.isAuthorizedForApp(appName)) {
          return Authorization.granted(userAuth).forApplication(appName);
        } else {
          return Authorization.denied(userAuth).forApplication(appName);
        }
      }
    }

    // no auth granted
    return Authorization.denied(Authentication.ANONYMOUS);
  }

  private Authorization grantAnnonymous() {
    return Authorization.granted(Authentication.ANONYMOUS);
  }
}

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

import java.util.regex.Matcher;

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
public abstract class AuthorizedAppRequestMatcher extends RequestMatcher {

  public AuthorizedAppRequestMatcher(String pattern, String[] methods) {
    super(pattern, methods);
  }

  @Override
  public boolean isAuthorized(AppRequest request) {
    return isMethodMatched(request.getMethod()) && isUriMatched(request.getUri(), request);
  }

  protected boolean isUriMatched(String uri, AppRequest request) {
    Authentications authentications = Authentications.getCurrent();
    if (authentications == null) {
      // the user is not authenticated => he has access to app.
      return true;

    } else {
      Matcher matcher = uriMatcher.matcher(uri);

      if (matcher.matches() && matcher.groupCount() == 1) {
        String engineName = matcher.group(1);

        Authentication processEngineAuth = authentications.getAuthentictionForProcessEngine(engineName);
        if (processEngineAuth == null) {
          // user is not authenticated to process engine  => he has access to app.
          return true;
        }

        // get process engine
        ProcessEngine processEngine = Cockpit.getProcessEngine(engineName);
        if (processEngine == null) {
          return true;
        }

        // check authorization
        if (processEngineAuth instanceof UserAuthentication) {

          // attach authentication information to request
          request.authenticated().application(getAppName());

          return getAppAuthorization((UserAuthentication) processEngineAuth);
        } else {
          return false;
        }

      } else {
        return false;

      }
    }
  }

  protected abstract boolean getAppAuthorization(UserAuthentication userAuthentication);

  protected abstract String getAppName();
}

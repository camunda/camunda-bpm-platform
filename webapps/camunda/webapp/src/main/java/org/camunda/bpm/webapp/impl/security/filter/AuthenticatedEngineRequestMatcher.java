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

import org.camunda.bpm.webapp.impl.security.auth.Authentications;

/**
 * <p>This is a {@link RequestMatcher} which matches all process engine api 
 * requests based on the current authentication</p>
 * 
 * @author Daniel Meyer
 *
 */
public class AuthenticatedEngineRequestMatcher extends RequestMatcher {
  
  public AuthenticatedEngineRequestMatcher(String pattern, String... methods) {
    super(pattern, methods);
  }

  protected boolean isUriMatched(String uri) {
    Authentications authentications = Authentications.getCurrent();
    if(authentications == null) { 
      return false;
      
    } else {
      Matcher matcher = uriMatcher.matcher(uri);
      
      if(matcher.matches() && matcher.groupCount() == 1) {
        String engineName = matcher.group(1);
        return authentications.hasAuthenticationForProcessEngine(engineName);
        
      } else {
        return false;
        
      }
    }
  }

}

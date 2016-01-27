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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.filter.RequestMatcher.Match;
import org.springframework.util.PathMatcher;

/**
 * <p>A {@link SecurityFilterRule} that deleagates to a set of {@link PathMatcher}s</p>
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public class PathFilterRule implements SecurityFilterRule {

  protected List<RequestMatcher> allowedPaths = new ArrayList<RequestMatcher>();
  protected List<RequestMatcher> deniedPaths = new ArrayList<RequestMatcher>();

  @Override
  public Authorization authorize(String requestMethod, String requestUri) {

    boolean secured = false;

    for (RequestMatcher pattern : deniedPaths) {
      Match match = pattern.match(requestMethod, requestUri);

      if (match != null) {
        secured = true;
        break;
      }
    }

    if (!secured) {
      return Authorization.granted(Authentication.ANONYMOUS);
    }

    for (RequestMatcher pattern : allowedPaths) {
      Match match = pattern.match(requestMethod, requestUri);

      if (match != null) {
        return match.authorize();
      }
    }

    return null;
  }

  public List<RequestMatcher> getAllowedPaths() {
    return allowedPaths;
  }

  public List<RequestMatcher> getDeniedPaths() {
    return deniedPaths;
  }
}

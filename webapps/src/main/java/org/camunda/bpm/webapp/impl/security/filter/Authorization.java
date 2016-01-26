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

import java.util.Collection;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthentication;

/**
 *
 * @author nico.rehwaldt
 */
public class Authorization {

  private final Authentication authentication;
  private final boolean granted;

  private String application;

  public Authorization(Authentication authentication, boolean granted) {
    this.authentication = authentication;
    this.granted = granted;
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public boolean isGranted() {
    return granted;
  }

  public Authorization forApplication(String application) {
    this.application = application;

    return this;
  }

  public void attachHeaders(HttpServletResponse response) {

    if (authentication != null) {
      // header != null checks required for websphere compatibility
      if (authentication.getIdentityId() != null) {
        response.addHeader("X-Authorized-User", authentication.getIdentityId());
      }

      if (authentication.getProcessEngineName() != null) {
        response.addHeader("X-Authorized-Engine", authentication.getProcessEngineName());
      }

      if (authentication instanceof UserAuthentication) {
        response.addHeader("X-Authorized-Apps", join(",", ((UserAuthentication) authentication).getAuthorizedApps()));
      }
    }

    // response.addHeader("X-Authorized", Boolean.toString(granted));
  }

  public boolean isAuthenticated() {
    return authentication != null && authentication != Authentication.ANONYMOUS;
  }

  public String getApplication() {
    return application;
  }

  ////// static helpers //////////////////////////////

  public static Authorization granted(Authentication authentication) {
    return new Authorization(authentication, true);
  }

  public static Authorization denied(Authentication authentication) {
    return new Authorization(authentication, false);
  }

  public static Authorization grantedUnlessNull(Authentication authentication) {
    return authentication != null ? granted(authentication) : denied(authentication);
  }

  private static String join(String delimiter, Collection<?> collection) {

    StringBuilder builder = new StringBuilder();

    for (Object o: collection) {

      if (builder.length() > 0) {
        builder.append(delimiter);
      }

      builder.append(o);
    }

    return builder.toString();
  }
}

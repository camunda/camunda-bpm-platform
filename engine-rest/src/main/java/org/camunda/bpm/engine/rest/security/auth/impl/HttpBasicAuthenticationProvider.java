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
package org.camunda.bpm.engine.rest.security.auth.impl;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

/**
 * <p>
 * Authenticates a request against the provided process engine's identity service by applying http basic authentication.
 * </p>
 *
 * @author Thorben Lindhauer
 */
public class HttpBasicAuthenticationProvider implements AuthenticationProvider {

  protected static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";

  @Override
  public AuthenticationResult extractAuthenticatedUser(HttpServletRequest request,
      ProcessEngine engine) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorizationHeader != null && authorizationHeader.startsWith(BASIC_AUTH_HEADER_PREFIX)) {
      String encodedCredentials = authorizationHeader.substring(BASIC_AUTH_HEADER_PREFIX.length());
      String decodedCredentials = new String(Base64.decodeBase64(encodedCredentials));
      int firstColonIndex = decodedCredentials.indexOf(":");

      if (firstColonIndex == -1) {
        return AuthenticationResult.unsuccessful();
      } else {
        String userName = decodedCredentials.substring(0, firstColonIndex);
        String password = decodedCredentials.substring(firstColonIndex + 1);
        if (isAuthenticated(engine, userName, password)) {
          return AuthenticationResult.successful(userName);
        } else {
          return AuthenticationResult.unsuccessful(userName);
        }
      }
    } else {
      return AuthenticationResult.unsuccessful();
    }
  }

  protected boolean isAuthenticated(ProcessEngine engine, String userName, String password) {
    return engine.getIdentityService().checkPassword(userName, password);
  }

  @Override
  public void augmentResponseByAuthenticationChallenge(
      HttpServletResponse response, ProcessEngine engine) {
    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, BASIC_AUTH_HEADER_PREFIX + "realm=\"" + engine.getName() + "\"");
  }
}

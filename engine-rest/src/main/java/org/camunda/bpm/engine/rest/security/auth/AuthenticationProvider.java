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
package org.camunda.bpm.engine.rest.security.auth;

import org.camunda.bpm.engine.ProcessEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A provider to handle the authentication of {@link HttpServletRequest}s.
 * May implement a specific authentication scheme.
 *
 * @author Thorben Lindhauer
 */
public interface AuthenticationProvider {

  /**
   * Checks the request for authentication. May not return null, but always an {@link AuthenticationResult} that indicates, whether
   * authentication was successful, and, if true, always provides the authenticated user.
   *
   * @param request the request to authenticate
   * @param engine the process engine the request addresses. May be used to authenticate against the engine's identity service.
   */
  AuthenticationResult extractAuthenticatedUser(HttpServletRequest request, ProcessEngine engine);

  /**
   * <p>
   * Callback to add an authentication challenge to the response to the client. Called in case of unsuccessful authentication.
   * </p>
   *
   * <p>
   * For example, a Http Basic auth implementation may set the WWW-Authenticate header to <code>Basic realm="engine name"</code>.
   * </p>
   *
   * @param request the response to augment
   * @param engine the process engine the request addressed. May be considered as an authentication realm to create a specific authentication
   * challenge
   */
  void augmentResponseByAuthenticationChallenge(HttpServletResponse response, ProcessEngine engine);
}

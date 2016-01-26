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

import org.camunda.bpm.webapp.impl.security.auth.Authentication;

/**
 * The interface for request authorizers.
 *
 * @author nico.rehwaldt
 */
public interface RequestAuthorizer {

  public static final RequestAuthorizer AUTHORIZE_ANNONYMOUS = new AnnonymousAuthorizer();

  /**
   * Authorize a request with the given parameters by returning a valid {@link Authentication}.
   *
   * @param parameters
   *
   * @return a valid {@link Authentication} or <code>null</code> if authorization to this request
   *         has not been granted
   */
  public Authorization authorize(Map<String, String> parameters);

  public static class AnnonymousAuthorizer implements RequestAuthorizer {

    @Override
    public Authorization authorize(Map<String, String> parameters) {
      return Authorization.granted(Authentication.ANONYMOUS);
    }
  }
}

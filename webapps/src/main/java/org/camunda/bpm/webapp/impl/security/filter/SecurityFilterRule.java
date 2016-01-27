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


/**
 * <p>This interface is used by the {@link SecurityFilter} to authorize incoming requests.</p>
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public interface SecurityFilterRule {

  /**
   * Authorize the given request and return a {@link Authorization} as a result.
   * May return <code>null</code> if the request could not be authorized.
   *
   * @param requestMethod
   * @param requestUri
   *
   * @return the authorization for the given request or <code>null</code> if the authorization
   *         for the request could not be checked
   */
  public Authorization authorize(String requestMethod, String requestUri);

}

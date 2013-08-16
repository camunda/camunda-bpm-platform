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
 *
 * @author nico.rehwaldt
 */
public class AppRequest {

  private boolean authorized;
  private boolean authenticated;

  private String application;

  private final String method;
  private final String uri;

  public AppRequest(String method, String uri) {
    this(method, uri, false, false);
  }

  public AppRequest(String method, String uri, boolean authorized, boolean authenticated) {
    this.method = method;
    this.uri = uri;

    this.authorized = authorized;
    this.authenticated = authenticated;
  }

  public AppRequest unauthorize() {
    this.authorized = false;
    return this;
  }

  public AppRequest authorize() {
    this.authorized = true;
    return this;
  }

  public AppRequest authenticated() {
    this.authenticated = true;
    return this;
  }

  public AppRequest application(String application) {
    this.application = application;
    return this;
  }

  public String getMethod() {
    return method;
  }

  public String getUri() {
    return uri;
  }

  public boolean isAuthorized() {
    return authorized;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public String getApplication() {
    return application;
  }

  @Override
  public String toString() {
    return String.format("%s %s (authorized=%s, authenticated=%s)", method, uri, authorized, authenticated);
  }
}

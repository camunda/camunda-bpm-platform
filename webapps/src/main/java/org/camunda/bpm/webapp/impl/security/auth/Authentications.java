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
package org.camunda.bpm.webapp.impl.security.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * <p>Wrapper around current authentications.</p>
 *
 * <p>In camunda BPM rest and webapplicaitons, authentications are managed per
 * process engine: at a given point in time, there might be multiple authentications
 * active for different users and process engines in a single session. The ituition
 * is that a "physical" user may posess credentials for different process engines,
 * each of these representing a different process engine user. For each process
 * engine, there can be at most one authentication active in a given session.</p>
 *
 * <p>In addition, the {@link AuthenticationFilter} binds an instance of this
 * class to a thread local and may be obtained by {@link #getCurrent()}</p>
 *
 * @author Daniel Meyer
 *
 */
public class Authentications implements Serializable {

  private static final long serialVersionUID = 1L;

  private static String CAM_AUTH_SESSION_KEY = "authenticatedUser";

  /** holds the current authentication */
  private static ThreadLocal<Authentications> currentAuthentications = new ThreadLocal<Authentications>();

  /**holds an entry for each processEngine->userId pair currently authenticated */
  protected Map<String, Authentication> authentications = new HashMap<String, Authentication>();

  public Authentications() {
  }

  /**
   * Returns an {@link Authentication} for a provided process engine name or "null".
   *
   * @param engineName
   *          the name of the process engine for which the userId should be
   *          retrieved.
   * @return {@link Authentication} for the provided process engine or
   *         "null" if no user is authenticated for this process engine.
   */
  public Authentication getAuthenticationForProcessEngine(String engineName) {
    return authentications.get(engineName);
  }

  /**
   * Adds an authentication to the list of current authentications. If there already
   * existis an authentication of the same process engine, it is replaced silently.
   *
   * @param authentication
   *          the authentication to add
   */
  public void addAuthentication(Authentication authentication) {
    authentications.put(authentication.getProcessEngineName(), authentication);
  }

  /**
   * Removes the authentication for the provided process engine name.
   *
   * @param engineName
   *          the name of the process engine for which the authentication should
   *          be removed.
   */
  public void removeAuthenticationForProcessEngine(String engineName) {
    authentications.remove(engineName);
  }

  /**
   * @return all active {@link Authentication Authentications}.
   */
  public List<Authentication> getAuthentications() {
    return new ArrayList<Authentication>(authentications.values());
  }

  /**
   * Allows checking whether a user is currently authenticated for a given process engine name.
   *
   * @param engineName the name of the process engine for which we want to check for authentication.
   * @return true if a user is authenticated for the provided process engine name.
   */
  public boolean hasAuthenticationForProcessEngine(String engineName) {
    return getAuthenticationForProcessEngine(engineName) != null;
  }

  // thread-local //////////////////////////////////////////////////////////

  /** sets the {@link Authentications} for the current thread in a thread local.
   * @param auth the {@link Authentications} to set.
   * */
  public static void setCurrent(Authentications auth) {
    currentAuthentications.set(auth);
  }

  /** clears the {@link Authentications} for the current thread.
   * */
  public static void clearCurrent() {
    currentAuthentications.remove();
  }

  /**
   * Returns the authentications for the current thread.
   *
   * @return the authentications.
   */
  public static Authentications getCurrent() {
    return currentAuthentications.get();
  }

  // http session //////////////////////////////////////////////////////////

  /**
   * Allows obtaining an {@link Authentications} object from the
   * {@link HttpSession}. If no such object exists in the session, a new
   * instance is created and returned.
   *
   * @param session
   *          the {@link HttpSession} instance from which to retrieve the
   *          {@link Authentications}.
   * @return
   */
  public static Authentications getFromSession(HttpSession session) {
    Authentications authentications = (Authentications) session.getAttribute(CAM_AUTH_SESSION_KEY);
    if(authentications == null) {
      authentications = new Authentications();
      session.setAttribute(CAM_AUTH_SESSION_KEY, authentications);
    }
    return authentications;
  }

  public static void updateSession(HttpSession session, Authentications authentications) {
    session.setAttribute(CAM_AUTH_SESSION_KEY, authentications);
  }

}

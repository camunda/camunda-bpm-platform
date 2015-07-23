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
package org.camunda.bpm.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Exception thrown by the process engine in case a user tries to
 * interact with a resource in an unauthorized way.</p>
 *
 * <p>The exception contains a list of Missing authorizations. The List is a
 * disjunction i.e. a user should have any of the authorization for the engine
 * to continue the execution beyond the point where it failed.</p>
 *
 * @author Daniel Meyer
 *
 */
public class AuthorizationException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;

  protected final String userId;
  protected final List<MissingAuthorization> info;

  public AuthorizationException(String message) {
    super(message);
    this.userId = null;
    info = new ArrayList<MissingAuthorization>();
  }

  public AuthorizationException(String userId, MissingAuthorization exceptionInfo) {
    super(
        "The user with id '"+userId+
        "' does not have '"+exceptionInfo.getViolatedPermissionName()+"' permission " +
        "on resource '" + (exceptionInfo.getResourceId()!= null ? (exceptionInfo.getResourceId()+"' of type '") : "" ) +
        exceptionInfo.getResourceType()+"'.");
    this.userId = userId;
    info = new ArrayList<MissingAuthorization>();
    info.add(exceptionInfo);
    
  }
  
  public AuthorizationException(String userId, List<MissingAuthorization> info, String message) {
    super(message);
    this.userId = userId;
    this.info = info;
  }

  /**
   * @return the type of the resource if there
   * is only one {@link MissingAuthorization}, {@code null} otherwise
   *
   * @deprecated Use {@link #getInfo()} to get the type of the resource
   * of the {@link MissingAuthorization}(s). This method will be removed in future version.
   */
  @Deprecated
  public String getResourceType() {
    String resourceType = null;
    if (info.size() == 1) {
      resourceType = info.get(0).getResourceType();
    }
    return resourceType;
  }

  /**
   * @return the type of the violated permission name if there
   * is only one {@link MissingAuthorization}, {@code null} otherwise
   *
   * @deprecated Use {@link #getInfo()} to get the violated permission name
   * of the {@link MissingAuthorization}(s). This method will be removed in future version.
   */
  @Deprecated
  public String getViolatedPermissionName() {
    if (info.size() == 1) {
      return info.get(0).getViolatedPermissionName();
    }
    return null;
  }

  
  public String getUserId() {
    return userId;
  }

  /**
   * @return the id of the resource if there
   * is only one {@link MissingAuthorization}, {@code null} otherwise
   *
   * @deprecated Use {@link #getInfo()} to get the id of the resource
   * of the {@link MissingAuthorization}(s). This method will be removed in future version.
   */
  @Deprecated
  public String getResourceId() {
    if (info.size() == 1) {
      return info.get(0).getResourceId();
    }
    return null;
  }

  /**
   * @return Disjunctive list of {@link MissingAuthorization} from
   * which a user needs to have at least one for the authorization to pass
   */
  public List<MissingAuthorization> getInfo() {
    return Collections.unmodifiableList(info);
  }
}

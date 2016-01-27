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
import java.security.Principal;

/**
 * <p>Represents an active authentication of a given identity (usually a user).</p>
 *
 * <p>In camunda webapps, an authentication exists between some identity (user) and
 * a process engine</p>
 *
 * <p>Implements java.security.Principal so that this object may be used everywhere where a
 * {@link Principal} is required.</p>
 *
 * @author Daniel Meyer
 *
 */
public class Authentication implements Principal, Serializable {

  public static final Authentication ANONYMOUS = new Authentication(null, null);
  
  private static final long serialVersionUID = 1L;

  protected final String identityId;

  protected final String processEngineName;

  public Authentication(String identityId, String processEngineName) {
    this.identityId = identityId;
    this.processEngineName = processEngineName;
  }

  /**
   * java.security.Principal implementation: return the id of the identity
   * (userId) behind this authentication
   */
  public String getName() {
    return identityId;
  }

  /**
   * @return the id of the identity
   * (userId) behind this authentication
   */
  public String getIdentityId() {
    return identityId;
  }

  /**
   * @return return the name of the process engine for which this authentication
   *         was established.
   */
  public String getProcessEngineName() {
    return processEngineName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((identityId == null) ? 0 : identityId.hashCode());
    result = prime * result + ((processEngineName == null) ? 0 : processEngineName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Authentication other = (Authentication) obj;
    if (identityId == null) {
      if (other.identityId != null)
        return false;
    } else if (!identityId.equals(other.identityId))
      return false;
    if (processEngineName == null) {
      if (other.processEngineName != null)
        return false;
    } else if (!processEngineName.equals(other.processEngineName))
      return false;
    return true;
  }


}

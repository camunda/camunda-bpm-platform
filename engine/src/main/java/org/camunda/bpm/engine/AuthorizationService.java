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

import org.camunda.bpm.engine.identity.Authorization;
import org.camunda.bpm.engine.identity.AuthorizationQuery;


/**
 * <p>The authorization service allows managing {@link Authorization Authorizations}.
 * Authorizations manage permissions of a given user/group to interact with a given 
 * resource.</p>
 * 
 * <h2>Creating an authorization</h2>
 * <p>An authorization is created between a user/group and a resource. It describes 
 * the user/group's <em>permissions</em> to access that resource. An authorization may 
 * express different permissions, such as the permission to READ, WRITE, DELTE the 
 * resource. (See {@link Authorization} for details).</p>
 * 
 * <h2>Granting / revoking permissions</h2>
 * <p></p>
 * 
 * <h2>Checking a permission</h2>
 * <p></p>
 * 
 * @author Daniel Meyer
 *
 */
public interface AuthorizationService {
  
  // Authorization CRUD //////////////////////////////////////
  
  /** 
   * <p>Returns a new (transient) {@link Authorization} object. The Object is not 
   * yet persistent and must be saved using the {@link #saveAuthorization(Authorization)}
   * method.</p>
   *   
   * @param authorizationId
   * @return an non-persistent Authorization object.
   */
  public Authorization createNewAuthorization();
  
  /**
   * Allows saving an {@link Authorization} object.
   *  
   * @param transientAuthorization a transient (non-persistent) Authorization object. 
   * @return the authorization object.
   * @throws ProcessEngineException in case an internal error occurs
   */
  public Authorization saveAuthorization(Authorization authorization);
  
  /**
   * Allows deleting a persistent {@link Authorization} object.
   *  
   * @param authorizationId the id of the Authorization object to delete. 
   * @throws ProcessEngineException if no such authorization exists or if an internal error occurs.
   */
  public void deleteAuthorization(String authorizationId);

  // Query ////////////////////////////////////////////
  
  /**
   *  Constructs an authorization query.
   */
  public AuthorizationQuery createAuthorizationQuery();
  
}

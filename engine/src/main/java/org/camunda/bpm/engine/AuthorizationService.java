/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine;

import java.util.List;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;


/**
 * <p>The authorization service allows managing {@link Authorization Authorizations}.</p>
 * 
 * <h2>Creating an authorization</h2>
 * <p>An authorization is created between a user/group and a resource. It describes 
 * the user/group's <em>permissions</em> to access that resource. An authorization may 
 * express different permissions, such as the permission to READ, UPDATE, DELETE the 
 * resource. (See {@link Authorization} for details).</p>
 * 
 * <h2>Granting / revoking permissions</h2>
 * <p>In order to grant the permission to access a certain resource, an authorization 
 * object is created:
 * <pre>
 * Authorization auth = authorizationService.createNewAuthorization();
 * //... configure auth
 * authorizationService.saveAuthorization(auth);
 * </pre>
 * The authorization object can be configured either for a user or a group:
 * <pre>
 * auth.setUserId("john");
 *   -OR-
 * auth.setGroupId("management");
 * </pre>
 * and a resource:
 * <pre>
 * auth.setResource("processDefinition");
 * auth.setResourceId("2313");
 * </pre>
 * finally the permissions to access that resource can be assigned:
 * <pre>
 * auth.addPermission(Permissions.READ);
 * </pre>
 * and the authorization object is saved:
 * <pre>
 * authorizationService.saveAuthorization(auth);
 * </pre>
 * As a result, the given user or group will have permission to READ the referenced process definition. 
 * </p>
 *
 * @author Daniel Meyer
 * @since 7.0
 */
public interface AuthorizationService {
  
  // Authorization CRUD //////////////////////////////////////
  
  /** 
   * <p>Returns a new (transient) {@link Authorization} object. The Object is not 
   * yet persistent and must be saved using the {@link #saveAuthorization(Authorization)}
   * method.</p>
   *   
   * @param authorizationType the type of the authorization. Legal values: {@link Authorization#AUTH_TYPE_GLOBAL}, 
   * {@link Authorization#AUTH_TYPE_GRANT}, {@link Authorization#AUTH_TYPE_REVOKE}
   * @return an non-persistent Authorization object.
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permissions on {@link Resources#AUTHORIZATION}.
   */
  public Authorization createNewAuthorization(int authorizationType);
  
  /**
   * Allows saving an {@link Authorization} object. Use this method for persisting new 
   * transient {@link Authorization} objects obtained through {@link #createNewAuthorization(int)} or
   * for updating persistent objects.
   *  
   * @param authorization a Authorization object. 
   * @return the authorization object.
   * @throws ProcessEngineException in case an internal error occurs
   * @throws AuthorizationException if the user has no 
   *          {@link Permissions#CREATE} permissions (in case of persisting a transient object) or no 
   *          {@link Permissions#UPDATE} permissions (in case of updating a persistent object) 
   *          on {@link Resources#AUTHORIZATION}
   */
  public Authorization saveAuthorization(Authorization authorization);
  
  /**
   * Allows deleting a persistent {@link Authorization} object.
   *  
   * @param authorizationId the id of the Authorization object to delete. 
   * @throws ProcessEngineException if no such authorization exists or if an internal error occurs.
   * @throws AuthorizationException if the user has no {@link Permissions#DELETE} permissions on {@link Resources#AUTHORIZATION}.
   */
  public void deleteAuthorization(String authorizationId);
  
  /**
   *  Constructs an authorization query.
   */
  public AuthorizationQuery createAuthorizationQuery();
  
  // Authorization Checks ////////////////////////////////
  
  /** 
   * <p>Allows performing an authorization check.</p>
   * <p>Returns true if the given user has permissions for interacting with the resource is the 
   * requested way.</p>
   * 
   * <p>This method checks for the resource type, see {@link Authorization#ANY}</p>
   * 
   * @param userId the id of the user for which the check is performed.
   * @param groupIds a list of group ids the user is member of
   * @param permission the permission(s) to check for.
   * @param resource the resource for which the authorization is checked.
   * @throws BadUserRequestException when {@code resource} is a
   * {@link Resources#HISTORIC_TASK Historic Task} or {@link Resources#HISTORIC_PROCESS_INSTANCE
   * Historic Process Instance} and historic instance permissions are disabled.
   */
  public boolean isUserAuthorized(String userId, List<String> groupIds, Permission permission, Resource resource);
  
  /** 
   * <p>Allows performing an authorization check.</p>
   * <p>Returns true if the given user has permissions for interacting with the resource is the 
   * requested way.</p>
   *   
   * @param userId the id of the user for which the check is performed.
   * @param groupIds a list of group ids the user is member of
   * @param permission the permission(s) to check for.
   * @param resource the resource for which the authorization is checked.
   * @param resourceId the resource id for which the authorization check is performed.
   * @throws BadUserRequestException when {@code resource} is a
   * {@link Resources#HISTORIC_TASK Historic Task} or {@link Resources#HISTORIC_PROCESS_INSTANCE
   * Historic Process Instance} and historic instance permissions are disabled.
   */
  public boolean isUserAuthorized(String userId, List<String> groupIds, Permission permission, Resource resource, String resourceId);
  
}

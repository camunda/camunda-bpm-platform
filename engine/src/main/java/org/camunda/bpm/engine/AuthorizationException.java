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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.authorization.MissingAuthorization;

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
  protected final List<MissingAuthorization> missingAuthorizations;

  // these properties have been replaced by the list of missingAuthorizations
  // and are only left because this is a public API package and users might
  // have subclasses relying on these fields
  @Deprecated
  protected String resourceType;
  @Deprecated
  protected String permissionName;
  @Deprecated
  protected String resourceId;

  public AuthorizationException(String message) {
    super(message);
    this.userId = null;
    missingAuthorizations = new ArrayList<>();
  }

  public AuthorizationException(String userId, String permissionName, String resourceType, String resourceId) {
    this(userId, new MissingAuthorization(permissionName, resourceType, resourceId));
  }

  public AuthorizationException(String userId, MissingAuthorization exceptionInfo) {
    super(
        "The user with id '"+userId+
        "' does not have "+generateMissingAuthorizationMessage(exceptionInfo)+".");
    this.userId = userId;
    missingAuthorizations = new ArrayList<>();
    missingAuthorizations.add(exceptionInfo);

    this.resourceType = exceptionInfo.getResourceType();
    this.permissionName = exceptionInfo.getViolatedPermissionName();
    this.resourceId = exceptionInfo.getResourceId();
  }

  public AuthorizationException(String userId, List<MissingAuthorization> info) {
    super(generateExceptionMessage(userId, info));
    this.userId = userId;
    this.missingAuthorizations = info;
  }

  /**
   * @return the type of the resource if there
   * is only one {@link MissingAuthorization}, {@code null} otherwise
   *
   * @deprecated Use {@link #getMissingAuthorizations()} to get the type of the resource
   * of the {@link MissingAuthorization}(s). This method may be removed in future versions.
   */
  @Deprecated
  public String getResourceType() {
    String resourceType = null;
    if (missingAuthorizations.size() == 1) {
      resourceType = missingAuthorizations.get(0).getResourceType();
    }
    return resourceType;
  }

  /**
   * @return the type of the violated permission name if there
   * is only one {@link MissingAuthorization}, {@code null} otherwise
   *
   * @deprecated Use {@link #getMissingAuthorizations()} to get the violated permission name
   * of the {@link MissingAuthorization}(s). This method may be removed in future versions.
   */
  @Deprecated
  public String getViolatedPermissionName() {
    if (missingAuthorizations.size() == 1) {
      return missingAuthorizations.get(0).getViolatedPermissionName();
    }
    return null;
  }

  /**
   * @return id of the user in which context the request was made and who misses authorizations
   *  to perform it successfully
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @return the id of the resource if there
   * is only one {@link MissingAuthorization}, {@code null} otherwise
   *
   * @deprecated Use {@link #getMissingAuthorizations()} to get the id of the resource
   * of the {@link MissingAuthorization}(s). This method may be removed in future versions.
   */
  @Deprecated
  public String getResourceId() {
    if (missingAuthorizations.size() == 1) {
      return missingAuthorizations.get(0).getResourceId();
    }
    return null;
  }

  /**
   * @return Disjunctive list of {@link MissingAuthorization} from
   * which a user needs to have at least one for the authorization to pass
   */
  public List<MissingAuthorization> getMissingAuthorizations() {
    return Collections.unmodifiableList(missingAuthorizations);
  }

  /**
   * Generate exception message from the missing authorizations.
   *
   * @param userId to use
   * @param missingAuthorizations to use
   * @return The prepared exception message
   */
  private static String generateExceptionMessage(String userId, List<MissingAuthorization> missingAuthorizations) {
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append("The user with id '");
    sBuilder.append(userId);
    sBuilder.append("' does not have one of the following permissions: ");
    sBuilder.append(generateMissingAuthorizationsList(missingAuthorizations));

    return sBuilder.toString();
  }

  /**
   * Generate a String containing a list of missing authorizations.
   *
   * @param missingAuthorizations
   */
  public static String generateMissingAuthorizationsList(List<MissingAuthorization> missingAuthorizations) {
    StringBuilder sBuilder = new StringBuilder();
    boolean first = true;
    for(MissingAuthorization missingAuthorization: missingAuthorizations) {
      if (!first) {
        sBuilder.append(" or ");
      } else {
        first = false;
      }
      sBuilder.append(generateMissingAuthorizationMessage(missingAuthorization));
    }
    return sBuilder.toString();
  }

  /**
   * Generated exception message for the missing authorization.
   *
   * @param exceptionInfo to use
   */
  private static String generateMissingAuthorizationMessage(MissingAuthorization exceptionInfo) {
    StringBuilder builder = new StringBuilder();
    String permissionName = exceptionInfo.getViolatedPermissionName();
    String resourceType = exceptionInfo.getResourceType();
    String resourceId = exceptionInfo.getResourceId();
    builder.append("'");
    builder.append(permissionName);
    builder.append("' permission on resource '");
    builder.append((resourceId != null ? (resourceId+"' of type '") : "" ));
    builder.append(resourceType);
    builder.append("'");

    return builder.toString();
  }
}

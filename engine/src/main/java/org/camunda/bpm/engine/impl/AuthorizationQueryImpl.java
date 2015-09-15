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
package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationQueryImpl extends AbstractQuery<AuthorizationQuery, Authorization> implements AuthorizationQuery {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String[] userIds;
  protected String[] groupIds;
  protected int resourceType;
  protected String resourceId;
  protected int permission = 0;
  protected Integer authorizationType;
  protected boolean queryByPermission = false;
  protected boolean queryByResourceType = false;

  public AuthorizationQueryImpl() {
  }

  public AuthorizationQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public AuthorizationQuery authorizationId(String id) {
    this.id = id;
    return this;
  }

  public AuthorizationQuery userIdIn(String... userIdIn) {
    if(groupIds != null) {
      throw new ProcessEngineException("Cannot query for user and group authorizations at the same time.");
    }
    this.userIds = userIdIn;
    return this;
  }

  public AuthorizationQuery groupIdIn(String... groupIdIn) {
    if(userIds != null) {
      throw new ProcessEngineException("Cannot query for user and group authorizations at the same time.");
    }
    this.groupIds = groupIdIn;
    return this;
  }

  public AuthorizationQuery resourceType(Resource resource) {
    return resourceType(resource.resourceType());
  }

  public AuthorizationQuery resourceType(int resourceType) {
    this.resourceType = resourceType;
    queryByResourceType = true;
    return this;
  }

  public AuthorizationQuery resourceId(String resourceId) {
    this.resourceId = resourceId;
    return this;
  }

  public AuthorizationQuery hasPermission(Permission p) {
    queryByPermission = true;
    this.permission |= p.getValue();
    return this;
  }

  public AuthorizationQuery authorizationType(Integer type) {
    this.authorizationType = type;
    return this;
  }


  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getAuthorizationManager()
      .selectAuthorizationCountByQueryCriteria(this);
  }

  public List<Authorization> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getAuthorizationManager()
        .selectAuthorizationByQueryCriteria(this);
  }

  // getters ////////////////////////////

  public String getId() {
    return id;
  }

  public boolean isQueryByPermission() {
    return queryByPermission;
  }

  public String[] getUserIds() {
    return userIds;
  }

  public String[] getGroupIds() {
    return groupIds;
  }

  public int getResourceType() {
    return resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public int getPermission() {
    return permission;
  }

  public boolean isQueryByResourceType() {
    return queryByResourceType;
  }

  public AuthorizationQuery orderByResourceType() {
    orderBy(AuthorizationQueryProperty.RESOURCE_TYPE);
    return this;
  }

  public AuthorizationQuery orderByResourceId() {
    orderBy(AuthorizationQueryProperty.RESOURCE_ID);
    return this;
  }

}

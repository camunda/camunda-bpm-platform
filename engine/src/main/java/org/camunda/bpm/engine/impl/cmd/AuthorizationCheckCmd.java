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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;
import java.util.Objects;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;

/**
 * <p>Command allowing to perform an authorization check</p>
 * 
 * @author Daniel Meyer
 *
 */
public class AuthorizationCheckCmd implements Command<Boolean> {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected String userId;
  protected List<String> groupIds;
  protected Permission permission;
  protected Resource resource;
  protected String resourceId;

  public AuthorizationCheckCmd(String userId, List<String> groupIds, Permission permission, Resource resource, String resourceId) {
    this.userId = userId;
    this.groupIds = groupIds;
    this.permission = permission;
    this.resource = resource;
    this.resourceId = resourceId;
    validate(userId, groupIds, permission, resource);
  }

  public Boolean execute(CommandContext commandContext) {
    final AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    if (authorizationManager.isPermissionDisabled(permission)) {
      throw LOG.disabledPermissionException(permission.getName());
    }

    if (isHistoricInstancePermissionsDisabled(commandContext) && isHistoricInstanceResource()) {
      throw LOG.disabledHistoricInstancePermissionsException();
    }

    return authorizationManager.isAuthorized(userId, groupIds, permission, resource, resourceId);
  }

  protected void validate(String userId, List<String> groupIds, Permission permission, Resource resource) {
    ensureAtLeastOneNotNull("Authorization must have a 'userId' or/and a 'groupId'.", userId, groupIds);
    ensureNotNull("Invalid permission for an authorization", "authorization.getResource()", permission);
    ensureNotNull("Invalid resource for an authorization", "authorization.getResource()", resource);
  }

  protected boolean isHistoricInstancePermissionsDisabled(CommandContext commandContext) {
    return !commandContext.getProcessEngineConfiguration().isEnableHistoricInstancePermissions();
  }

  protected boolean isHistoricInstanceResource() {
    return Objects.equals(Resources.HISTORIC_TASK, resource) ||
        Objects.equals(Resources.HISTORIC_PROCESS_INSTANCE, resource);
  }

}

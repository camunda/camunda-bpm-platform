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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureWhitelistedResourceId;

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.IdentityOperationResult;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;

/**
 * @author Joram Barrez
 */
public class SaveUserCmd extends AbstractWritableIdentityServiceCmd<Void> implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected User user;
  protected boolean skipPasswordPolicy;

  public SaveUserCmd(User user) {
    this(user, false);
  }

  public SaveUserCmd(User user, boolean skipPasswordPolicy) {
    this.user = user;
    this.skipPasswordPolicy = skipPasswordPolicy;
  }

  protected Void executeCmd(CommandContext commandContext) {
    ensureNotNull("user", user);
    ensureWhitelistedResourceId(commandContext, "User", user.getId());

    if (user instanceof UserEntity) {
      validateUserEntity(commandContext);
    }

    IdentityOperationResult operationResult = commandContext
      .getWritableIdentityProvider()
      .saveUser(user);

    commandContext.getOperationLogManager().logUserOperation(operationResult, user.getId());

    return null;
  }

  private void validateUserEntity(CommandContext commandContext) {
    if(shouldCheckPasswordPolicy(commandContext)) {
      if(!((UserEntity) user).checkPasswordAgainstPolicy()) {
        throw new ProcessEngineException("Password does not match policy");
      }
    }
  }

  protected boolean shouldCheckPasswordPolicy(CommandContext commandContext) {
    return ((UserEntity) user).hasNewPassword() && !skipPasswordPolicy
        && commandContext.getProcessEngineConfiguration().isEnablePasswordPolicy();
  }
}

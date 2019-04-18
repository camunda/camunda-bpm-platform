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

import java.io.Serializable;

import org.camunda.bpm.engine.impl.identity.IdentityOperationResult;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class DeleteUserCmd extends AbstractWritableIdentityServiceCmd<Void> implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  String userId;
  
  public DeleteUserCmd(String userId) {
    this.userId = userId;
  }
  
  protected Void executeCmd(CommandContext commandContext) {
    ensureNotNull("userId", userId);

    // delete user picture
    new DeleteUserPictureCmd(userId).execute(commandContext);

    commandContext.getIdentityInfoManager()
      .deleteUserInfoByUserId(userId);

    IdentityOperationResult operationResult = commandContext
      .getWritableIdentityProvider()
      .deleteUser(userId);

    commandContext.getOperationLogManager().logUserOperation(operationResult, userId);

    return null;
  }
}

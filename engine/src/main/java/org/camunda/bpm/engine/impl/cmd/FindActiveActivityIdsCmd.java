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
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;


/**
 * @author Tom Baeyens
 */
public class FindActiveActivityIdsCmd implements Command<List<String>>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String executionId;

  public FindActiveActivityIdsCmd(String executionId) {
    this.executionId = executionId;
  }

  public List<String> execute(CommandContext commandContext) {
    ensureNotNull("executionId", executionId);

    // fetch execution
    ExecutionManager executionManager = commandContext.getExecutionManager();
    ExecutionEntity execution = executionManager.findExecutionById(executionId);
    ensureNotNull("execution " + executionId + " doesn't exist", "execution", execution);

    checkGetActivityIds(execution, commandContext);

    // fetch active activities
    return execution.findActiveActivityIds();
  }

  protected void checkGetActivityIds(ExecutionEntity execution, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessInstance(execution);
    }
  }
}

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
package org.camunda.bpm.engine.test.bpmn.event.conditional;

import static org.camunda.bpm.engine.test.bpmn.event.conditional.AbstractConditionalEventTestCase.TASK_WITH_CONDITION;
import static org.camunda.bpm.engine.test.bpmn.event.conditional.AbstractConditionalEventTestCase.VARIABLE_NAME;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class SetVariableOnConcurrentExecutionDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Task task = execution.getProcessEngineServices().getTaskService().createTaskQuery().taskName(TASK_WITH_CONDITION).singleResult();
    ((TaskEntity) task).getExecution().setVariableLocal(VARIABLE_NAME, 1);
    execution.setVariableLocal(VARIABLE_NAME+1, 1);
  }
}

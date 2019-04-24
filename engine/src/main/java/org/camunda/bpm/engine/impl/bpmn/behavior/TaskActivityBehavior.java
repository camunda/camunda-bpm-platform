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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;



/**
 * Parent class for all BPMN 2.0 task types such as ServiceTask, ScriptTask, UserTask, etc.
 *
 * When used on its own, it behaves just as a pass-through activity.
 *
 * @author Joram Barrez
 */
public class TaskActivityBehavior extends AbstractBpmnActivityBehavior {

  /**
   * Activity instance id before execution.
   */
  protected String activityInstanceId;

  /**
   * The method which will be called before the execution is performed.
   *
   * @param execution the execution which is used during execution
   * @throws Exception
   */
  protected void preExecution(ActivityExecution execution) throws Exception {
    activityInstanceId = execution.getActivityInstanceId();
  }

  /**
   * The method which should be overridden by the sub classes to perform an execution.
   *
   * @param execution the execution which is used during performing the execution
   * @throws Exception
   */
  protected void performExecution(ActivityExecution execution) throws Exception {
    leave(execution);
  }

  /**
   * The method which will be called after performing the execution.
   *
   * @param execution the execution
   * @throws Exception
   */
  protected void postExecution(ActivityExecution execution) throws Exception {
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    performExecution(execution);
  }



}

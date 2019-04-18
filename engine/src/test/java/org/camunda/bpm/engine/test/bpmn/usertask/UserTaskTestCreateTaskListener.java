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
package org.camunda.bpm.engine.test.bpmn.usertask;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;



/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Saeid Mirzaei
 */

/**
 * This is for test case UserTaskTest.testCompleteAfterParallelGateway
 *
 */

public class UserTaskTestCreateTaskListener implements TaskListener {

  private static final long serialVersionUID = 1L;
  private Expression expression;

  public void notify(DelegateTask delegateTask) {

    if (this.expression != null && this.expression.getValue(delegateTask) != null) {
      // get the expression variable
      String expression = this.expression.getValue(delegateTask).toString();

      // this expression will be evaluated when completing the task
      delegateTask.setVariableLocal("validationRule", expression);
    }

  }

}



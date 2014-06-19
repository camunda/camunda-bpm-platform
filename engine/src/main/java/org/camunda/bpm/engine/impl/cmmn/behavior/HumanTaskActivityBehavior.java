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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.task.TaskDefinition;

/**
 * @author Roman Smirnov
 *
 */
public class HumanTaskActivityBehavior extends AbstractCmmnActivityBehavior {

  protected TaskDecorator taskDecorator;

  protected void performActiveBehavior(CmmnActivityExecution execution, CmmnActivity activity) {
    TaskEntity task = TaskEntity.createAndInsert(execution);
    task.setCaseExecution(execution);

    taskDecorator.decorate(task, execution);

    // All properties set, now firing 'create' event
    task.fireEvent(TaskListener.EVENTNAME_CREATE);
  }

  // getters/setters /////////////////////////////////////////////////

  public TaskDecorator getTaskDecorator() {
    return taskDecorator;
  }

  public void setTaskDecorator(TaskDecorator taskDecorator) {
    this.taskDecorator = taskDecorator;
  }

  public TaskDefinition getTaskDefinition() {
    return taskDecorator.getTaskDefinition();
  }

  public ExpressionManager getExpressionManager() {
    return taskDecorator.getExpressionManager();
  }

}

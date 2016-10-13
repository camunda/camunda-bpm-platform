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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.Collection;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingUserTaskInstance;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParseContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.MigrationObserverBehavior;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.task.TaskDefinition;

/**
 * activity implementation for the user task.
 *
 * @author Joram Barrez
 * @author Roman Smirnov
 */
public class UserTaskActivityBehavior extends TaskActivityBehavior implements MigrationObserverBehavior {

  protected TaskDecorator taskDecorator;

  @Deprecated
  public UserTaskActivityBehavior(ExpressionManager expressionManager, TaskDefinition taskDefinition) {
    this.taskDecorator = new TaskDecorator(taskDefinition, expressionManager);
  }

  public UserTaskActivityBehavior(TaskDecorator taskDecorator) {
    this.taskDecorator = taskDecorator;
  }

  @Override
  public void performExecution(ActivityExecution execution) throws Exception {
    TaskEntity task = TaskEntity.createAndInsert(execution);

    taskDecorator.decorate(task, execution);

    Context.getCommandContext()
      .getHistoricTaskInstanceManager()
      .createHistoricTask(task);

    // All properties set, now firing 'create' event
    task.fireEvent(TaskListener.EVENTNAME_CREATE);
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }

  // migration

  @Override
  public void migrateScope(ActivityExecution scopeExecution) {
  }

  @Override
  public void onParseMigratingInstance(MigratingInstanceParseContext parseContext, MigratingActivityInstance migratingInstance) {
    ExecutionEntity execution = migratingInstance.resolveRepresentativeExecution();

    for (TaskEntity task : execution.getTasks()) {
      migratingInstance.addMigratingDependentInstance(new MigratingUserTaskInstance(task, migratingInstance));
      parseContext.consume(task);

      Collection<VariableInstanceEntity> variables = task.getVariablesInternal();

      if (variables != null) {
        for (VariableInstanceEntity variable : variables) {
          // we don't need to represent task variables in the migrating instance structure because
          // they are migrated by the MigratingTaskInstance as well
          parseContext.consume(variable);
        }
      }
    }

  }

  // getters

  public TaskDefinition getTaskDefinition() {
    return taskDecorator.getTaskDefinition();
  }

  public ExpressionManager getExpressionManager() {
    return taskDecorator.getExpressionManager();
  }

  public TaskDecorator getTaskDecorator() {
    return taskDecorator;
  }

}

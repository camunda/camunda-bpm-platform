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

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.PriorityProvider;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnExceptionHandler;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingExternalTaskInstance;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParseContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.MigrationObserverBehavior;

/**
 * Implements behavior of external task activities, i.e. all service-task-like
 * activities that have camunda:type="external".
 *
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public class ExternalTaskActivityBehavior extends AbstractBpmnActivityBehavior implements MigrationObserverBehavior {

  protected ParameterValueProvider topicNameValueProvider;
  protected ParameterValueProvider priorityValueProvider;

  public ExternalTaskActivityBehavior(ParameterValueProvider topicName, ParameterValueProvider paramValueProvider) {
    this.topicNameValueProvider = topicName;
    this.priorityValueProvider = paramValueProvider;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    PriorityProvider<ExternalTaskActivityBehavior> provider = Context.getProcessEngineConfiguration().getExternalTaskPriorityProvider();

    long priority = provider.determinePriority(executionEntity, this, null);
    String topic = (String) topicNameValueProvider.getValue(executionEntity);

    ExternalTaskEntity.createAndInsert(executionEntity, topic, priority);

  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }

  public ParameterValueProvider getPriorityValueProvider() {
    return priorityValueProvider;
  }

  /**
   * It's used to propagate the bpmn error from an external task.
   * @param error the error which should be propagated
   * @param execution the current activity execution
   * @throws Exception throws an exception if no handler was found
   */
  public void propagateBpmnError(BpmnError error, ActivityExecution execution) throws Exception {
    BpmnExceptionHandler.propagateBpmnError(error, execution);
  }

  @Override
  public void migrateScope(ActivityExecution scopeExecution) {
  }

  @Override
  public void onParseMigratingInstance(MigratingInstanceParseContext parseContext, MigratingActivityInstance migratingInstance) {
    ExecutionEntity execution = migratingInstance.resolveRepresentativeExecution();

    for (ExternalTaskEntity task : execution.getExternalTasks()) {
      MigratingExternalTaskInstance migratingTask = new MigratingExternalTaskInstance(task, migratingInstance);
      migratingInstance.addMigratingDependentInstance(migratingTask);
      parseContext.consume(task);
      parseContext.submit(migratingTask);
    }
  }
}

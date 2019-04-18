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
package org.camunda.bpm.engine.impl.migration.instance;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingVariableInstance implements MigratingInstance {

  protected VariableInstanceEntity variable;
  protected boolean isConcurrentLocalInParentScope;

  public MigratingVariableInstance(VariableInstanceEntity variable, boolean isConcurrentLocalInParentScope) {
    this.variable = variable;
    this.isConcurrentLocalInParentScope = isConcurrentLocalInParentScope;
  }

  @Override
  public boolean isDetached() {
    return variable.getExecutionId() == null;
  }

  @Override
  public void detachState() {
    variable.getExecution().removeVariableInternal(variable);
  }

  @Override
  public void attachState(MigratingScopeInstance owningActivityInstance) {
    ExecutionEntity representativeExecution = owningActivityInstance.resolveRepresentativeExecution();
    ScopeImpl currentScope = owningActivityInstance.getCurrentScope();

    ExecutionEntity newOwningExecution = representativeExecution;

    if (currentScope.isScope() && isConcurrentLocalInParentScope) {
      newOwningExecution = representativeExecution.getParent();
    }

    newOwningExecution.addVariableInternal(variable);
  }

  @Override
  public void attachState(MigratingTransitionInstance owningActivityInstance) {
    ExecutionEntity representativeExecution = owningActivityInstance.resolveRepresentativeExecution();

    representativeExecution.addVariableInternal(variable);
  }

  @Override
  public void migrateState() {
    migrateHistory();
  }

  protected void migrateHistory() {
    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();

    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE, this)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricVariableMigrateEvt(variable);
        }
      });
    }
  }

  @Override
  public void migrateDependentEntities() {
    // nothing to do
  }

  public String getVariableName() {
    return variable.getName();
  }

}

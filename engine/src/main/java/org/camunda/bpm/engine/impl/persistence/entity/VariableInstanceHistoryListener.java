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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;

/**
 * @author Thorben Lindhauer
 *
 */
public class VariableInstanceHistoryListener implements VariableInstanceLifecycleListener<VariableInstanceEntity> {

  public static final VariableInstanceHistoryListener INSTANCE = new VariableInstanceHistoryListener();

  @Override
  public void onCreate(VariableInstanceEntity variableInstance, AbstractVariableScope sourceScope) {
    if (getHistoryLevel().isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_CREATE, variableInstance)) {
      HistoryEvent evt = getHistoryEventProducer().createHistoricVariableCreateEvt(variableInstance, sourceScope);
      getHistoryEventHandler().handleEvent(evt);
    }
  }

  @Override
  public void onDelete(VariableInstanceEntity variableInstance, AbstractVariableScope sourceScope) {
    if (getHistoryLevel().isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_DELETE, variableInstance)) {
      HistoryEvent evt = getHistoryEventProducer().createHistoricVariableDeleteEvt(variableInstance, sourceScope);
      getHistoryEventHandler().handleEvent(evt);
    }
  }

  @Override
  public void onUpdate(VariableInstanceEntity variableInstance, AbstractVariableScope sourceScope) {
    if (getHistoryLevel().isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE, variableInstance)) {
      HistoryEvent evt = getHistoryEventProducer().createHistoricVariableUpdateEvt(variableInstance, sourceScope);
      getHistoryEventHandler().handleEvent(evt);
    }
  }

  protected HistoryLevel getHistoryLevel() {
    return Context.getProcessEngineConfiguration().getHistoryLevel();
  }

  protected HistoryEventProducer getHistoryEventProducer() {
    return Context.getProcessEngineConfiguration().getHistoryEventProducer();
  }

  protected HistoryEventHandler getHistoryEventHandler() {
    return Context.getProcessEngineConfiguration().getHistoryEventHandler();
  }

}

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
package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * <p>Factory for {@link HistoryEventProducer HistoryEventProducers}. This factory 
 * allows customizing the producers for {@link HistoryEvent HistoryEvents}. This factory 
 * allows you to customize the data acquisition aspect of the history 
 * (extracting data from the execution structures).</p>
 * 
 * <p>Custom implementations of this class can be wired through 
 * {@link ProcessEngineConfigurationImpl#setHistoryEventProducerFactory(HistoryEventProducerFactory)}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class HistoryEventProducerFactory {

  // process instance ///////////////////////////////
  
  /**
   * @return the {@link HistoryEventProducer} to be invoked for producing the {@link HistoricProcessInstanceEventEntity} 
   * when a process instance is <b>started</b>.
   */
  public HistoryEventProducer getHistoricProcessInstanceStartEventProducer() {
    return new HistoricProcessInstanceStartEventProducer();
  }
  
  /**
   * @return the {@link HistoryEventProducer} to be invoked for producing the {@link HistoricProcessInstanceEventEntity} 
   * when a process instance is <b>ended</b>.
   */
  public HistoryEventProducer getHistoricProcessInstanceEndEventProducer() {
    return new HistoricProcessInstanceEndEventProducer();
  }
  
  // activity instance ///////////////////////////////
  
  /**
   * @return the {@link HistoryEventProducer} to be invoked for producing the {@link HistoricActivityInstanceEventEntity} 
   * when an activity instance is <b>started</b>.
   */
  public HistoryEventProducer getHistoricActivityInstanceStartEventProducer() {
    return new HistoricActivityInstanceEventProducer(HistoricActivityInstanceEventEntity.ACTIVITY_EVENT_TYPE_START);
  }
  
  /**
   * @return the {@link HistoryEventProducer} to be invoked for producing the {@link HistoricActivityInstanceEventEntity} 
   * when an activity instance is <b>ended</b>.
   */
  public HistoryEventProducer getHistoricActivityInstanceEndEventProducer() {
    return new HistoricActivityInstanceEventProducer(HistoricActivityInstanceEventEntity.ACTIVITY_EVENT_TYPE_END);
  }
  
  // tasks instance ///////////////////////////////
  
  // variable instance ////////////////////////////
  
  // ... TODO
  

}

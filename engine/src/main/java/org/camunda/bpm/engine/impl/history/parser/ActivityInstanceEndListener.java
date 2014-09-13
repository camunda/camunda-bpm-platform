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
package org.camunda.bpm.engine.impl.history.parser;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;

/**
 * @author Daniel Meyer
 *
 */
public class ActivityInstanceEndListener extends HistoryExecutionListener {

  public ActivityInstanceEndListener(HistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    super(historyEventProducer, historyLevel);
  }

  protected HistoryEvent createHistoryEvent(DelegateExecution execution) {
    if(historyLevel.isHistoryEventProduced(HistoryEventTypes.ACTIVITY_INSTANCE_END, execution)) {
      return eventProducer.createActivityInstanceEndEvt(execution);
    } else {
      return null;
    }
  }

}

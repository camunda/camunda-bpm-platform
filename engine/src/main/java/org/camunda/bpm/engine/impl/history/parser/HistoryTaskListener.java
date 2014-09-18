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

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

/**
 * <p>A {@link TaskListener} implementation that delegates to a
 * {@link HistoryEventProducer}.
 *
 * @author Daniel Meyer
 *
 */
public abstract class HistoryTaskListener implements TaskListener {

  protected final HistoryEventProducer eventProducer;
  protected HistoryLevel historyLevel;

  public HistoryTaskListener(HistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    this.eventProducer = historyEventProducer;
    this.historyLevel = historyLevel;
  }

  public void notify(DelegateTask task) {

    // get the event handler
    final HistoryEventHandler historyEventHandler = Context.getProcessEngineConfiguration()
      .getHistoryEventHandler();

    ExecutionEntity execution = ((TaskEntity) task).getExecution();

    if (execution != null) {

      // delegate creation of the history event to the producer
      HistoryEvent historyEvent = createHistoryEvent(task, execution);

      if(historyEvent != null) {
        // pass the event to the handler
        historyEventHandler.handleEvent(historyEvent);
      }

    }

  }

  protected abstract HistoryEvent createHistoryEvent(DelegateTask task, ExecutionEntity execution);

}

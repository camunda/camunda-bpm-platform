/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.history.transformer;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.producer.CmmnHistoryEventProducer;

/**
 * @author Sebastian Menski
 */
public abstract class HistoryCaseExecutionListener implements CaseExecutionListener {

  protected CmmnHistoryEventProducer eventProducer;
  protected HistoryLevel historyLevel;

  public HistoryCaseExecutionListener(CmmnHistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    eventProducer = historyEventProducer;
    this.historyLevel = historyLevel;
  }

  public void notify(DelegateCaseExecution caseExecution) throws Exception {
    HistoryEvent historyEvent = createHistoryEvent(caseExecution);

    if (historyEvent != null) {
      Context.getProcessEngineConfiguration()
        .getHistoryEventHandler()
        .handleEvent(historyEvent);
    }

  }

  protected abstract HistoryEvent createHistoryEvent(DelegateCaseExecution caseExecution);

}

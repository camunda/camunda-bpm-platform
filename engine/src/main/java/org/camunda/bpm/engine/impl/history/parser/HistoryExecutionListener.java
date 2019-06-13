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
package org.camunda.bpm.engine.impl.history.parser;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;

/**
 * <p>An {@link ExecutionListener} implementation that delegates to a
 * {@link HistoryEventProducer}.
 *
 * <p>This allows plugging the history as an execution listener into process
 * execution and make sure history events are generated as we move through the
 * process.
 *
 * @author Daniel Meyer
 *
 */
public abstract class HistoryExecutionListener implements ExecutionListener {

  protected final HistoryEventProducer eventProducer;
  protected HistoryLevel historyLevel;

  public HistoryExecutionListener(HistoryEventProducer historyEventProducer) {
    this.eventProducer = historyEventProducer;
  }

  public void notify(DelegateExecution execution) throws Exception {

    // get the event handler
    final HistoryEventHandler historyEventHandler = Context.getProcessEngineConfiguration()
      .getHistoryEventHandler();

    // delegate creation of the history event to the producer
    HistoryEvent historyEvent = createHistoryEvent(execution);

    if(historyEvent != null) {
      // pass the event to the handler
      historyEventHandler.handleEvent(historyEvent);
    }

  }
  
  protected void ensureHistoryLevelInitialized() {
    if (historyLevel == null) {
      historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    }
  }

  protected abstract HistoryEvent createHistoryEvent(DelegateExecution execution);

}

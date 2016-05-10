/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.history.event;

import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;

/**
 * <p>The {@link HistoryEventProcessor} should be used to process an history event.</p>
 *
 * <p>The {@link HistoryEvent} will be created with the help of the {@link HistoryEventProducer}
 * from the {@link ProcessEngineConfiguration} and the given implementation of the
 * {@link HistoryEventCreator} which uses the producer object to create an
 * {@link HistoryEvent}. The {@link HistoryEvent} will be handled by the
 * {@link HistoryEventHandler} from the {@link ProcessEngineConfiguration}.</p>
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 * @since 7.5
 */
public class HistoryEventProcessor {

  /**
   * The {@link HistoryEventCreator} interface which is used to interchange the implementation
   * of the creation of different HistoryEvents.
   */
  public static class HistoryEventCreator {
    /**
     * Creates the {@link HistoryEvent} with the help off the given
     * {@link HistoryEventProducer}.
     *
     * @param producer the producer which is used for the creation
     * @return the created {@link HistoryEvent}
     */
    public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
      return null;
    }

    public List<HistoryEvent> createHistoryEvents(HistoryEventProducer producer) {
      return Collections.emptyList();
    }
  }


  /**
   * Process an {@link HistoryEvent} and handle them directly after creation.
   * The {@link HistoryEvent} is created with the help of the given
   * {@link HistoryEventCreator} implementation.
   *
   * @param creator the creator is used to create the {@link HistoryEvent} which should be thrown
   */
  public static void processHistoryEvents(HistoryEventCreator creator) {
    HistoryEventProducer historyEventProducer = Context.getProcessEngineConfiguration().getHistoryEventProducer();
    HistoryEventHandler historyEventHandler = Context.getProcessEngineConfiguration().getHistoryEventHandler();

    HistoryEvent singleEvent = creator.createHistoryEvent(historyEventProducer);
    if (singleEvent != null) {
      historyEventHandler.handleEvent(singleEvent);
    }

    List<HistoryEvent> eventList = creator.createHistoryEvents(historyEventProducer);
    historyEventHandler.handleEvents(eventList);
  }
}

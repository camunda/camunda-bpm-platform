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
package org.camunda.bpm.engine.impl.history.handler;

import java.util.List;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * <p>The interface for implementing an history event handler.</p>
 *
 * <p>The {@link HistoryEventHandler} is responsible for consuming the event. Many different
 * implementations of this interface can be imagined. Some implementations might persist the
 * event to a database, others might persist the event to a message queue and handle it
 * asynchronously.</p>
 *
 * <p>The default implementation of this interface is {@link DbHistoryEventHandler} which
 * persists events to a database.</p>
 *
 *
 * @author Daniel Meyer
 *
 */
public interface HistoryEventHandler {

  /**
   * Called by the process engine when an history event is fired.
   *
   * @param historyEvent the {@link HistoryEvent} that is about to be fired.
   */
  public void handleEvent(HistoryEvent historyEvent);

  /**
   * Called by the process engine when an history event is fired.
   *
   * @param historyEvents the {@link HistoryEvent} that is about to be fired.
   */
  public void handleEvents(List<HistoryEvent> historyEvents);

}

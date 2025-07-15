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
package org.camunda.bpm.engine.test.history;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

/**
 * @author Edoardo Patti
 */
public class TestEventHandler implements HistoryEventHandler {

  private final Queue<HistoryEvent> queue = new ArrayDeque<>(50);

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    queue.offer(historyEvent);
  }

  @Override
  public void handleEvents(List<HistoryEvent> historyEvents) {
    historyEvents.forEach(queue::offer);
  }

  public HistoryEvent poll() {
    return this.queue.poll();
  }

  public HistoryEvent peek() {
    return this.queue.peek();
  }
}

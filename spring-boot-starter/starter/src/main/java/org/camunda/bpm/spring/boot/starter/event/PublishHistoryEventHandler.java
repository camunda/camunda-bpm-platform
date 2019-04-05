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
package org.camunda.bpm.spring.boot.starter.event;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * Event handler publishing history events as Spring Events.
 */
public class PublishHistoryEventHandler implements HistoryEventHandler {

  private final ApplicationEventPublisher publisher;

  public PublishHistoryEventHandler(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    this.publisher.publishEvent(historyEvent);
  }

  @Override
  public void handleEvents(final List<HistoryEvent> eventList) {
    if (eventList != null) {
      eventList.forEach(this::handleEvent);
    }
  }
}

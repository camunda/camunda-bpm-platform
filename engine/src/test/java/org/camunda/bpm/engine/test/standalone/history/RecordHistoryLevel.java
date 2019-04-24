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
package org.camunda.bpm.engine.test.standalone.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;

public class RecordHistoryLevel implements HistoryLevel {

  protected List<HistoryEventType> recordedHistoryEventTypes = new ArrayList<HistoryEventType>();
  protected List<ProducedHistoryEvent> producedHistoryEvents = new ArrayList<ProducedHistoryEvent>();

  public RecordHistoryLevel() {
  }

  public RecordHistoryLevel(HistoryEventType... filterHistoryEventType) {
    Collections.addAll(this.recordedHistoryEventTypes, filterHistoryEventType);
  }

  public int getId() {
    return 42;
  }

  public String getName() {
    return "recordHistoryLevel";
  }

  public List<HistoryEventType> getRecordedHistoryEventTypes() {
    return recordedHistoryEventTypes;
  }

  public List<ProducedHistoryEvent> getProducedHistoryEvents() {
    return producedHistoryEvents;
  }

  public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
    if (recordedHistoryEventTypes.isEmpty() || recordedHistoryEventTypes.contains(eventType)) {
      producedHistoryEvents.add(new ProducedHistoryEvent(eventType, entity));
    }
    return true;
  }

  public static class ProducedHistoryEvent {

    public final HistoryEventType eventType;
    public final Object entity;

    public ProducedHistoryEvent(HistoryEventType eventType, Object entity) {

      this.eventType = eventType;
      this.entity = entity;
    }

  }

}

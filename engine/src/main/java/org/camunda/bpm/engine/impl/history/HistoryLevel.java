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
package org.camunda.bpm.engine.impl.history;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * <p>The history level controls what kind of data is logged to the history database.
 * More formally, it controls which history events are produced by the {@link HistoryEventProducer}.</p>
 *
 * <p><strong>Built-in history levels:</strong> The process engine provides a set of built-in history levels
 * as default configuration. The built-in history levels are:
 * <ul>
 *   <li>{@link #HISTORY_LEVEL_NONE}</li>
 *   <li>{@link #HISTORY_LEVEL_ACTIVITY}</li>
 *   <li>{@link #HISTORY_LEVEL_AUDIT}</li>
 *   <li>{@link #HISTORY_LEVEL_FULL}</li>
 * </ul>
 * This class provides singleton instances of these history levels as constants.
 * </p>
 *
 * <p><strong>Custom history levels:</strong>In order to implement a custom history level,
 * the following steps are necessary:
 * <ul>
 *   <li>Provide a custom implementation of this interface. Note: Make sure you choose unique values for
 *   {@link #getName()} and {@link #getId()}</li>
 *   <li>Add an instance of the custom implementation through
 *   {@link ProcessEngineConfigurationImpl#setCustomHistoryLevels(java.util.List)}</li>
 *   <li>use the name of your history level (as returned by {@link #getName()} as value for
 *   {@link ProcessEngineConfiguration#setHistory(String)}</li>
 * </ul>
 * </p>
 *
 * @author Daniel Meyer
 * @since 7.2
 */
public interface HistoryLevel {

  static HistoryLevel HISTORY_LEVEL_NONE = new HistoryLevelNone();
  static HistoryLevel HISTORY_LEVEL_ACTIVITY = new HistoryLevelActivity();
  static HistoryLevel HISTORY_LEVEL_AUDIT = new HistoryLevelAudit();
  static HistoryLevel HISTORY_LEVEL_FULL = new HistoryLevelFull();

  /** An unique id identifying the history level.
   * The id is used internally to uniquely identify the history level and also stored in the database.
   */
  int getId();

  /** An unique name identifying the history level.
   * The name of the history level can be used when configuring the process engine.
   * @see {@link ProcessEngineConfiguration#setHistory(String)}
   */
  String getName();

  /**
   * Returns true if a given history event should be produced.
   * @param eventType the type of the history event which is about to be produced
   * @param entity the runtime structure used to produce the history event. Examples {@link ExecutionEntity},
   * {@link TaskEntity}, {@link VariableInstanceEntity}, ... If a 'null' value is provided, the implementation
   * should return true if events of this type should be produced "in general".
   */
  boolean isHistoryEventProduced(HistoryEventType eventType, Object entity);

}

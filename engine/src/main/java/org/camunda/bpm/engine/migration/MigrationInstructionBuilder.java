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
package org.camunda.bpm.engine.migration;

/**
 * @author Thorben Lindhauer
 */
public interface MigrationInstructionBuilder extends MigrationPlanBuilder {

  /**
   * <p>If the current instruction maps between event-receiving flow nodes that rely on a persistent
   * event trigger, this method can be used to determine whether the event trigger should be
   * updated during migration
   *
   * <p>For example, when mapping a message catch event waiting for message <i>A</i>
   * to another message catch waiting for message <i>B</i>, using this option updates
   * the message trigger to <i>B</i> during migration. That means, after migration this
   * process instance can be correlated to using <i>B</i>. If this option is not used, then
   * the message trigger is not updated and <i>A</i> is the message to be received after migration.
   *
   * <p>Event-receiving flow nodes are:
   * <ul>
   *   <li> intermediate events (signal, message, timer)
   *   <li> boundary events (signal, message, timer)
   *   <li> start events (signal, message, timer)
   *   <li> receive tasks
   * </ul>
   *
   * <p>For other flow nodes, this option must not be used and if so, results in a validation exception
   * when the plan is created
   *
   * @return this builder
   */
  MigrationInstructionBuilder updateEventTrigger();
}

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
 * Represents an instruction to migrate instances of one activity to another activity.
 * Migration instructions are always contained in a {@link MigrationPlan}.
 *
 * @author Thorben Lindhauer
 */
public interface MigrationInstruction {

  /**
   * @return the id of the activity of the source process definition that this
   * instruction maps instances from
   */
  String getSourceActivityId();

  /**
   * @return the id of the activity of the target process definition that this
   * instruction maps instances to
   */
  String getTargetActivityId();

  /**
   * @return whether this flow node's event trigger is going to be updated during
   *   migration. Can only be true for flow nodes that define a persistent event trigger.
   *   See {@link MigrationInstructionBuilder#updateEventTrigger()} for details
   */
  boolean isUpdateEventTrigger();

}

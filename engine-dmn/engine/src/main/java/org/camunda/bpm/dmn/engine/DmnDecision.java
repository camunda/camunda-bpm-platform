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
package org.camunda.bpm.dmn.engine;

import java.util.Collection;

/**
 * A decision of the DMN Engine.
 *
 * <p>
 * Decisions can be implement in different ways. To check if the decision is implemented
 * as a Decision Table see {@link #isDecisionTable()}.
 * </p>
 */
public interface DmnDecision {

  /**
   * The unique identifier of the decision if exists.
   *
   * @return the identifier or null if not set
   */
  String getKey();

  /**
   * The human readable name of the decision if exists.
   *
   * @return the name or null if not set
   */
  String getName();

  /**
   * Checks if the decision logic is implemented as Decision Table.
   *
   * @return true if the decision logic is implement as Decision Table, otherwise false
   */
  boolean isDecisionTable();

  /**
   * Returns the decision logic of the decision (e.g., a decision table).
   *
   * @return the containing decision logic
   */
  DmnDecisionLogic getDecisionLogic();

  /**
   * Returns the required decisions of this decision.
   *
   * @return the required decisions or an empty collection if not exists.
   */
  Collection<DmnDecision> getRequiredDecisions();

}

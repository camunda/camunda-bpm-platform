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
import java.util.Set;

/**
 * Container of {@link DmnDecision}s which belongs to the same decision
 * requirements graph (i.e. DMN resource).
 */
public interface DmnDecisionRequirementsGraph {

  /**
   * The unique identifier of the diagram if exists.
   *
   * @return the identifier or null if not set
   */
  String getKey();

  /**
   * The human readable name of the diagram if exists.
   *
   * @return the name or null if not set
   */
  String getName();

  /**
   * Gets the containing decisions.
   *
   * @return the containing decisions
   */
  Collection<DmnDecision> getDecisions();

  /**
   * Gets the containing decision with the given key.
   *
   * @param key
   *          the identifier of the decision
   * @return the decision or null if not exists
   */
  DmnDecision getDecision(String key);

  /**
   * Get the keys of the containing decisions.
   *
   * @return the decision keys.
   */
  Set<String> getDecisionKeys();

}

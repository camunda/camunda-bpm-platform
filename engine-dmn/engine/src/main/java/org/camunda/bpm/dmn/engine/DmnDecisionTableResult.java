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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * The result of one decision table. Which is the list of its decision rule results (see
 * {@link DmnDecisionRuleResult}). This represents the output entries of all matching
 * decision rules.
 */
public interface DmnDecisionTableResult extends List<DmnDecisionRuleResult>, Serializable {

  /**
   * Returns the first {@link DmnDecisionRuleResult}.
   *
   * @return the first decision rule result or null if none exits
   */
  DmnDecisionRuleResult getFirstResult();

  /**
   * Returns the single {@link DmnDecisionRuleResult} of the result. Which asserts
   * that only one decision rule result exist.
   *
   * @return the single decision rule result or null if none exists
   *
   * @throws DmnEngineException
   *           if more than one decision rule result exists
   */
  DmnDecisionRuleResult getSingleResult();

  /**
   * Collects the entries for a output name. The list will contain entries for
   * the output name of every {@link DmnDecisionRuleResult}. Note that the list
   * may contains less entries than decision rule results if an output does not
   * contain a value for the output name.
   *
   * @param outputName
   *          the name of the output to collect
   * @param <T>
   *          the type of the rule result entry
   * @return the list of collected output values
   */
  <T> List<T> collectEntries(String outputName);

  /**
   * Returns the entries of all decision rule results. For every decision rule
   * result a map of the output names and corresponding entries is returned.
   *
   * @return the list of all entry maps
   *
   * @see DmnDecisionRuleResult#getEntryMap()
   */
  List<Map<String, Object>> getResultList();

  /**
   * Returns the value of the single entry of the decision rule result. Asserts that
   * only one decision rule result with a single entry exist.
   *
   * @param <T>
   *          the type of the result entry
   * @return the value of the single result entry or null if none exists
   *
   * @throws DmnEngineException
   *           if more than one decision rule result or more than one result entry
   *           exists
   *
   * @see #getSingleEntryTyped()
   */
  <T> T getSingleEntry();

  /**
   * Returns the typed value of the single entry of the decision rule result. Asserts
   * that only one decision rule result with a single entry exist.
   *
   * @param <T>
   *          the type of the result entry
   * @return the typed value of the single result entry or null if none exists
   *
   * @throws DmnEngineException
   *           if more than one decision rule result or more than one result entry
   *           exists
   *
   * @see #getSingleEntry()
   */
  <T extends TypedValue> T getSingleEntryTyped();

}

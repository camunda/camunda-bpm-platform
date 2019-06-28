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
import java.util.Map;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * The result of one decision rule. This represents the output entry
 * values of a matching decision rule. It is a mapping from the output
 * {@code name} attribute to the output value. If no {@code name}
 * was given the key is {@code null}.
 */
public interface DmnDecisionRuleResult extends Map<String, Object>, Serializable {

  /**
   * Returns the value of the first rule result entry.
   *
   * @param <T>
   *          the type of the rule result entry
   * @return the value of the first rule result entry or null if none exists
   *
   * @see #getFirstEntryTyped()
   */
  <T> T getFirstEntry();

  /**
   * Returns the typed value of the first rule result entry.
   *
   * @param <T>
   *          the type of the rule result entry
   * @return the typed value of the first rule result entry or null if none exists
   *
   * @see #getFirstEntry()
   */
  <T extends TypedValue> T getFirstEntryTyped();

  /**
   * Returns the value of the single entry of the decision rule result.
   * Which asserts that the decision rule result only has one entry.
   *
   * @param <T>
   *          the type of the rule result entry
   * @return the value of the single rule result entry or null if none exists
   *
   * @throws DmnEngineException
   *           if more than one rule result entry exists
   *
   * @see #getSingleEntryTyped()
   */
  <T> T getSingleEntry();

  /**
   * Returns the typed value of the single entry of the decision rule result.
   * Which asserts that the decision rule result only has one entry.
   *
   * @param <T>
   *          the type of the rule result entry
   * @return the typed value of the single rule result entry or null if none exists
   *
   * @throws DmnEngineException
   *           if more than one rule result entry exists
   *
   * @see #getSingleEntry()
   */
  <T extends TypedValue> T getSingleEntryTyped();

  /**
   * Returns the value of the rule result entry for a given output name.
   *
   * @param name
   *          the name of the output
   * @param <T>
   *          the type of the rule result entry
   * @return the value for the given name or null if no value exists for
   *         this name
   *
   * @see #getEntryTyped(String)
   */
  <T> T getEntry(String name);

  /**
   * Returns the typed value of the rule result entry for a given output name.
   *
   * @param name
   *          the name of the output
   * @param <T>
   *          the type of the rule result entry
   * @return the typed value for the given name or null if no value exists for
   *         this name
   *
   * @see #getEntry(String)
   */
  <T extends TypedValue> T getEntryTyped(String name);

  /**
   * Returns a map of the rule result entry values by output name.
   *
   * @return the values of the decision rule result entries
   *
   * @see #getEntryMapTyped()
   */
  Map<String, Object> getEntryMap();

  /**
   * Returns a map of the typed rule result entry values by output name.
   *
   * @return the typed values of the decision rule result entries
   *
   * @see #getEntryMap()
   */
  Map<String, TypedValue> getEntryMapTyped();

}

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
 * <p>
 * Represents the output entries (i.e., pairs of output name and value).
 * </p>
 *
 * <p>
 * In case of a decision with a decision table, the result contains the output
 * entries of a matched rule. Each output value is mapped to the output
 * {@code name} attribute. If no {@code name} was given then the entry key is
 * {@code null}.
 * </p>
 */
public interface DmnDecisionResultEntries extends Map<String, Object>, Serializable {

  /**
   * Returns the value of the first result entry.
   *
   * @param <T>
   *          the type of the result entry
   * @return the value of the first result entry or null if none exists
   *
   * @see #getFirstEntryTyped()
   */
  <T> T getFirstEntry();

  /**
   * Returns the typed value of the first result entry.
   *
   * @param <T>
   *          the type of the result entry
   * @return the typed value of the first result entry or null if none exists
   *
   * @see #getFirstEntry()
   */
  <T extends TypedValue> T getFirstEntryTyped();

  /**
   * Returns the value of the single entry of the decision result. Asserts that
   * the decision result only has one entry.
   *
   * @param <T>
   *          the type of the result entry
   * @return the value of the single result entry or null if none exists
   *
   * @throws DmnEngineException
   *           if more than one result entry exists
   *
   * @see #getSingleEntryTyped()
   */
  <T> T getSingleEntry();

  /**
   * Returns the typed value of the single entry of the decision result. Asserts
   * that the decision result only has one entry.
   *
   * @param <T>
   *          the type of the result entry
   * @return the typed value of the single result entry or null if none exists
   *
   * @throws DmnEngineException
   *           if more than one result entry exists
   *
   * @see #getSingleEntry()
   */
  <T extends TypedValue> T getSingleEntryTyped();

  /**
   * Returns the value of the result entry for a given output name.
   *
   * @param name
   *          the name of the output
   * @param <T>
   *          the type of the result entry
   * @return the value for the given name or null if no value exists for this
   *         name
   *
   * @see #getEntryTyped(String)
   */
  <T> T getEntry(String name);

  /**
   * Returns the typed value of the result entry for a given output name.
   *
   * @param name
   *          the name of the output
   * @param <T>
   *          the type of the result entry
   * @return the typed value for the given name or null if no value exists for
   *         this name
   *
   * @see #getEntry(String)
   */
  <T extends TypedValue> T getEntryTyped(String name);

  /**
   * Returns a map of the result entry values by output name.
   *
   * @return the values of the decision result entries
   *
   * @see #getEntryMapTyped()
   */
  Map<String, Object> getEntryMap();

  /**
   * Returns a map of the typed result entry values by output name.
   *
   * @return the typed values of the decision result entries
   *
   * @see #getEntryMap()
   */
  Map<String, TypedValue> getEntryMapTyped();

}

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
package org.camunda.bpm.engine.impl.management;

import java.util.Map;

/**
 * Represents an interface for the purge reporting.
 * Contains all information of the data which is deleted during the purge.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface PurgeReporting<T> {

  /**
   * Adds the key value pair as report information to the current purge report.
   *
   * @param key the report key
   * @param value the report value
   */
  void addPurgeInformation(String key, T value);

  /**
   * Returns the current purge report.
   *
   * @return the purge report
   */
  Map<String, T> getPurgeReport();

  /**
   * Transforms and returns the purge report to a string.
   *
   * @return the purge report as string
   */
  String getPurgeReportAsString();

  /**
   * Returns the value for the given key.
   *
   * @param key the key which exist in the current report
   * @return the corresponding value
   */
  T getReportValue(String key);

  /**
   * Returns true if the key is present in the current report.
   * @param key the key
   * @return true if the key is present
   */
  boolean containsReport(String key);

  /**
   * Returns true if the report is empty.
   *
   * @return true if the report is empty, false otherwise
   */
  boolean isEmpty();
}

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
package org.camunda.bpm.engine.history;

/**
 * <p>Represents a report result about duration of completed instances for a given period.</p>
 *
 * <p>The result must be interpreted in conjunction with the executed report.</p>
 *
 * @author Roman Smirnov
 *
 */
public interface DurationReportResult extends ReportResult {

  /**
   * <p>Returns the smallest duration of all completed instances,
   * which have been started in the given period.</p>
   */
  long getMinimum();

  /**
   * <p>Returns the greatest duration of all completed instances,
   * which have been started in the given period.</p>
   */
  long getMaximum();

  /**
   * <p>Returns the average duration of all completed instances,
   * which have been started in the given period.</p>
   */
  long getAverage();

}

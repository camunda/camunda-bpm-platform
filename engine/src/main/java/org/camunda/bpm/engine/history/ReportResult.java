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

import org.camunda.bpm.engine.query.PeriodUnit;

/**
 * This interface defines basic methods for resulting reports.
 *
 * @author Roman Smirnov
 *
 */
public interface ReportResult {

  /**
   * <p>Returns a period which specifies a time span within a year.</p>
   *
   * <p>The returned period must be interpreted in conjunction
   * with the returned {@link PeriodUnit} of {@link #getPeriodUnit()}.</p>
   *
   * </p>For example:</p>
   * <ul>
   *   <li>{@link #getPeriodUnit()} returns {@link PeriodUnit#MONTH}
   *   <li>{@link #getPeriod()} returns <code>3</code>
   * </ul>
   *
   * <p>The returned period <code>3</code> must be interpreted as
   * the third <code>month</code> of the year (i.e. it represents
   * the month March).</p>
   *
   * <p>If the {@link #getPeriodUnit()} returns {@link PeriodUnit#QUARTER},
   * then the returned period <code>3</code> must be interpreted as the third
   * <code>quarter</code> of the year.</p>
   *
   * @return an integer representing span of time within a year
   */
  int getPeriod();

  /**
   * <p>Returns the unit of the period.</p>
   *
   * @return a {@link PeriodUnit}
   *
   * @see #getPeriod()
   */
  PeriodUnit getPeriodUnit();

}

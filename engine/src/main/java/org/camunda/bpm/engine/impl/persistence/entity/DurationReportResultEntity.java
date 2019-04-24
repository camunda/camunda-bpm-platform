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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.history.DurationReportResult;

/**
 * @author Roman Smirnov
 *
 */
public class DurationReportResultEntity extends ReportResultEntity implements DurationReportResult {

  protected long minimum;
  protected long maximum;
  protected long average;

  public long getMinimum() {
    return minimum;
  }

  public void setMinimum(long minimum) {
    this.minimum = minimum;
  }

  public long getMaximum() {
    return maximum;
  }

  public void setMaximum(long maximum) {
    this.maximum = maximum;
  }

  public long getAverage() {
    return average;
  }

  public void setAverage(long average) {
    this.average = average;
  }

  public String toString() {
    return this.getClass().getSimpleName()
        + "[period=" + period
        + ", periodUnit=" + periodUnit
        + ", minimum=" + minimum
        + ", maximum=" + maximum
        + ", average=" + average
        + "]";
  }

}

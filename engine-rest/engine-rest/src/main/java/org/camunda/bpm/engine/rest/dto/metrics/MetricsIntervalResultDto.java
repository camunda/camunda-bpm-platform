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
package org.camunda.bpm.engine.rest.dto.metrics;

import java.util.Date;
import org.camunda.bpm.engine.management.MetricIntervalValue;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MetricsIntervalResultDto {

  protected Date timestamp;

  protected String name;

  protected String reporter;

  protected long value;

  public MetricsIntervalResultDto(MetricIntervalValue metric) {
    this.timestamp = metric.getTimestamp();
    this.name = metric.getName();
    this.reporter = metric.getReporter();
    this.value = metric.getValue();
  }

  public MetricsIntervalResultDto(Date timestamp, String name, String reporter, long value) {
    this.timestamp = timestamp;
    this.name = name;
    this.reporter = reporter;
    this.value = value;
  }

  public MetricsIntervalResultDto() {
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReporter() {
    return reporter;
  }

  public void setReporter(String reporter) {
    this.reporter = reporter;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

}

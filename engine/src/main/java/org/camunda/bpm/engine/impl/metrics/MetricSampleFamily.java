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
package org.camunda.bpm.engine.impl.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sample of metric values
 */
public abstract class MetricSampleFamily {

  public final String name;
  public final TYPE type;
  public final String help;
  public final List<String> labelNames;
  public final Map<Set<String>, MetricSample> samples;

  public MetricSampleFamily(String name, TYPE type, List<String> labelNames) {
    this(name, type, null, labelNames);
  }

  public MetricSampleFamily(String name, TYPE type, String help, List<String> labelNames) {
    this.name = name;
    this.type = type;
    this.help = help;
    this.labelNames = labelNames == null ? Collections.emptyList() : labelNames;
    this.samples = new HashMap<>();
  }

  public static enum TYPE {
    COUNTER,
    GAUGE
  }

  public abstract MetricSampleFamily addValue(List<String> labels, long value);

  public String getName() {
    return name;
  }

  public TYPE getType() {
    return type;
  }

  public String getHelp() {
    return help;
  }

  public List<String> getLabelNames() {
    return labelNames;
  }

  public Map<Set<String>, MetricSample> getSamples() {
    return samples;
  }


}

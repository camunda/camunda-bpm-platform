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
import java.util.HashSet;
import java.util.List;

/**
 * Sample of gauge values
 */
public class GaugeMetricSampleFamily extends MetricSampleFamily {

  public GaugeMetricSampleFamily(String name, List<String> labelNames) {
    super(name, TYPE.GAUGE, "", labelNames);
  }

  @Override
  public GaugeMetricSampleFamily addValue(List<String> labelsIn, double value) {
    List<String> labels = labelsIn == null ? Collections.emptyList() : labelsIn;
    if (labels.size() != labelNames.size()) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
    HashSet<String> samplesKey = new HashSet<>(labels);
    MetricSample currentSample = samples.get(samplesKey);
    if (currentSample == null) {
      currentSample = new MetricSample(name, labelNames, labels, value);
      samples.put(samplesKey, currentSample);
    } else {
      currentSample.setValue(currentSample.getValue() + value);
    }
    return this;
  }
}

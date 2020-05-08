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
package org.camunda.bpm.dmn.engine.impl.metrics;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationListener;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.spi.DmnEngineMetricCollector;

public class DmnEngineMetricCollectorWrapper implements DmnEngineMetricCollector, DmnDecisionEvaluationListener {

  protected final DmnEngineMetricCollector collector;

  public DmnEngineMetricCollectorWrapper(DmnEngineMetricCollector collector) {
    this.collector = collector;
  }

  @Override
  public void notify(DmnDecisionTableEvaluationEvent evaluationEvent) {
    // the wrapper listen for decision evaluation events
  }

  @Override
  public void notify(DmnDecisionEvaluationEvent evaluationEvent) {
    notifyCollector(evaluationEvent.getDecisionResult());

    for (DmnDecisionLogicEvaluationEvent event : evaluationEvent.getRequiredDecisionResults()) {
      notifyCollector(event);
    }
  }

  protected void notifyCollector(DmnDecisionLogicEvaluationEvent evaluationEvent) {
    if (evaluationEvent instanceof DmnDecisionTableEvaluationEvent) {
      collector.notify((DmnDecisionTableEvaluationEvent) evaluationEvent);
    }
    // ignore other evaluation events since the collector is implemented as decision table evaluation listener
  }

  @Override
  public long getExecutedDecisionInstances() {
    return collector.getExecutedDecisionInstances();
  }

  @Override
  public long getExecutedDecisionElements() {
    return collector.getExecutedDecisionElements();
  }

  @Override
  public long clearExecutedDecisionInstances() {
    return collector.clearExecutedDecisionInstances();
  }

  @Override
  public long clearExecutedDecisionElements() {
    return collector.clearExecutedDecisionElements();
  }

}

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

import java.util.concurrent.atomic.AtomicLong;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationListener;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.spi.DmnEngineMetricCollector;

public class DefaultEngineMetricCollector implements DmnEngineMetricCollector, DmnDecisionEvaluationListener {

  protected AtomicLong executedDecisionInstances = new AtomicLong();
  protected AtomicLong executedDecisionElements = new AtomicLong();

  public void notify(DmnDecisionTableEvaluationEvent evaluationEvent) {
    // collector is registered as decision evaluation listener
  }

  public void notify(DmnDecisionEvaluationEvent evaluationEvent) {
    long executedDecisionInstances = evaluationEvent.getExecutedDecisionInstances();
    long executedDecisionElements = evaluationEvent.getExecutedDecisionElements();
    this.executedDecisionInstances.getAndAdd(executedDecisionInstances);
    this.executedDecisionElements.getAndAdd(executedDecisionElements);
  }

  @Override
  public long getExecutedDecisionInstances() {
    return executedDecisionInstances.get();
  }

  @Override
  public long getExecutedDecisionElements() {
    return executedDecisionElements.get();
  }

  @Override
  public long clearExecutedDecisionInstances() {
    return executedDecisionInstances.getAndSet(0);
  }

  @Override
  public long clearExecutedDecisionElements() {
    return executedDecisionElements.getAndSet(0);
  }

}

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

import java.util.List;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationListener;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationListener;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.spi.DmnEngineMetricCollector;

/**
 * The configuration of a {@link DmnEngine}. It can be used
 * to build a new engine using {@link #buildEngine()}.
 *
 * <p>
 *   To create a new default DMN engine configuration the
 *   method {@link #createDefaultDmnEngineConfiguration()}
 *   can be used.
 * </p>
 *
 * <p>
 *   Please be aware that changes to the configuration can also
 *   influence the behavior of engines which were already created
 *   by this configuration instance.
 * </p>
 */
public abstract class DmnEngineConfiguration {

  /**
   * @return a new default dmn engine configuration
   */
  public static DmnEngineConfiguration createDefaultDmnEngineConfiguration() {
    return new DefaultDmnEngineConfiguration();
  }

  /**
   * @return the configured engine metric collector
   */
  public abstract DmnEngineMetricCollector getEngineMetricCollector();

  /**
   * Set the engine metric collector
   *
   * @param engineMetricCollector the engine metric collector to use
   */
  public abstract void setEngineMetricCollector(DmnEngineMetricCollector engineMetricCollector);

  /**
   * Set the engine metric collector
   *
   * @param engineMetricCollector the engine metric collector to use
   * @return this configuration
   */
  public abstract DmnEngineConfiguration engineMetricCollector(DmnEngineMetricCollector engineMetricCollector);

  /**
   * @return the list of custom pre decision table evaluation listeners
   */
  public abstract List<DmnDecisionTableEvaluationListener> getCustomPreDecisionTableEvaluationListeners();

  /**
   * Set the list of pre decision table evaluation listeners. They will be notified before
   * the default decision table evaluation listeners.
   *
   * @param decisionTableEvaluationListeners the list of pre decision table evaluation listeners
   */
  public abstract void setCustomPreDecisionTableEvaluationListeners(List<DmnDecisionTableEvaluationListener> decisionTableEvaluationListeners);

  /**
   * Set the list of pre decision table evaluation listeners. They will be notified before
   * the default decision table evaluation listeners.
   *
   * @param decisionTableEvaluationListeners the list of pre decision table evaluation listeners
   * @return this configuration
   */
  public abstract DmnEngineConfiguration customPreDecisionTableEvaluationListeners(List<DmnDecisionTableEvaluationListener> decisionTableEvaluationListeners);

  /**
   * @return the list of custom post decision table evaluation listeners
   */
  public abstract List<DmnDecisionTableEvaluationListener> getCustomPostDecisionTableEvaluationListeners();

  /**
   * Set the list of post decision table evaluation listeners. They will be notified after
   * the default decision table evaluation listeners.
   *
   * @param decisionTableEvaluationListeners the list of post decision table evaluation listeners
   */
  public abstract void setCustomPostDecisionTableEvaluationListeners(List<DmnDecisionTableEvaluationListener> decisionTableEvaluationListeners);

  /**
   * Set the list of post decision table evaluation listeners. They will be notified after
   * the default decision table evaluation listeners.
   *
   * @param decisionTableEvaluationListeners the list of post decision table evaluation listeners
   * @return this configuration
   */
  public abstract DmnEngineConfiguration customPostDecisionTableEvaluationListeners(List<DmnDecisionTableEvaluationListener> decisionTableEvaluationListeners);

  /**
   * @return the list of custom pre decision evaluation listeners
   */
  public abstract List<DmnDecisionEvaluationListener> getCustomPreDecisionEvaluationListeners();

  /**
   * Set the list of pre decision evaluation listeners. They will be notified before
   * the default decision evaluation listeners.
   *
   * @param decisionTableEvaluationListeners the list of pre decision table evaluation listeners
   */
  public abstract void setCustomPreDecisionEvaluationListeners(List<DmnDecisionEvaluationListener> decisionEvaluationListeners);

  /**
   * Set the list of pre decision evaluation listeners. They will be notified before
   * the default decision evaluation listeners.
   *
   * @param decisionEvaluationListeners the list of pre decision evaluation listeners
   * @return this configuration
   */
  public abstract DmnEngineConfiguration customPreDecisionEvaluationListeners(List<DmnDecisionEvaluationListener> decisionEvaluationListeners);

  /**
   * @return the list of custom post decision evaluation listeners
   */
  public abstract List<DmnDecisionEvaluationListener> getCustomPostDecisionEvaluationListeners();

  /**
   * Set the list of post decision evaluation listeners. They will be notified after
   * the default decision evaluation listeners.
   *
   * @param decisionEvaluationListeners the list of post decision table evaluation listeners
   */
  public abstract void setCustomPostDecisionEvaluationListeners(List<DmnDecisionEvaluationListener> decisionEvaluationListeners);

  /**
   * Set the list of post decision evaluation listeners. They will be notified after
   * the default decision evaluation listeners.
   *
   * @param decisionTableEvaluationListeners the list of post decision evaluation listeners
   * @return this configuration
   */

  public abstract DmnEngineConfiguration customPostDecisionEvaluationListeners(List<DmnDecisionEvaluationListener> decisionEvaluationListeners);

  /**
   * Create a {@link DmnEngine} with this configuration
   *
   * @return the created {@link DmnEngine}
   */
  public abstract DmnEngine buildEngine();

}

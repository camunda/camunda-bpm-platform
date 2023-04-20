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
package org.camunda.bpm.engine.impl.dmn.configuration;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationListener;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.el.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.impl.spi.el.ElProvider;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformer;
import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;
import org.camunda.bpm.engine.impl.dmn.transformer.DecisionDefinitionHandler;
import org.camunda.bpm.engine.impl.dmn.transformer.DecisionRequirementsDefinitionTransformHandler;
import org.camunda.bpm.engine.impl.history.parser.HistoryDecisionEvaluationListener;
import org.camunda.bpm.engine.impl.history.producer.DmnHistoryEventProducer;
import org.camunda.bpm.engine.impl.metrics.dmn.MetricsDecisionEvaluationListener;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;

/**
 * Modify the given DMN engine configuration so that the DMN engine can be used
 * from the process engine. Note that properties will not be overridden if they
 * are set on the configuration, except the transform handler for the decision table.
 *
 * @author Philipp Ossler
 */
public class DmnEngineConfigurationBuilder {

  protected final DefaultDmnEngineConfiguration dmnEngineConfiguration;

  protected DmnHistoryEventProducer dmnHistoryEventProducer;
  protected DmnScriptEngineResolver scriptEngineResolver;
  protected ElProvider elProvider;
  protected List<FeelCustomFunctionProvider> feelCustomFunctionProviders;

  /**
   * Creates a new builder to modify the given DMN engine configuration.
   */
  public DmnEngineConfigurationBuilder(DefaultDmnEngineConfiguration dmnEngineConfiguration) {
    ensureNotNull("dmnEngineConfiguration", dmnEngineConfiguration);

    this.dmnEngineConfiguration = dmnEngineConfiguration;
  }

  public DmnEngineConfigurationBuilder dmnHistoryEventProducer(DmnHistoryEventProducer dmnHistoryEventProducer) {
    this.dmnHistoryEventProducer = dmnHistoryEventProducer;

    return this;
  }

  public DmnEngineConfigurationBuilder scriptEngineResolver(DmnScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;

    return this;
  }

  public DmnEngineConfigurationBuilder elProvider(ElProvider elProvider) {
    this.elProvider = elProvider;

    return this;
  }

  public DmnEngineConfigurationBuilder feelCustomFunctionProviders(List<FeelCustomFunctionProvider> feelCustomFunctionProviders) {
    this.feelCustomFunctionProviders = feelCustomFunctionProviders;

    return this;
  }

  /**
   * Modify the given DMN engine configuration and return it.
   */
  public DefaultDmnEngineConfiguration build() {

    List<DmnDecisionEvaluationListener> decisionEvaluationListeners = createCustomPostDecisionEvaluationListeners();
    dmnEngineConfiguration.setCustomPostDecisionEvaluationListeners(decisionEvaluationListeners);

    // override the decision table handler
    DmnTransformer dmnTransformer = dmnEngineConfiguration.getTransformer();
    dmnTransformer.getElementTransformHandlerRegistry().addHandler(Definitions.class, new DecisionRequirementsDefinitionTransformHandler());
    dmnTransformer.getElementTransformHandlerRegistry().addHandler(Decision.class, new DecisionDefinitionHandler());

    // do not override the script engine resolver if set
    if (dmnEngineConfiguration.getScriptEngineResolver() == null) {
      ensureNotNull("scriptEngineResolver", scriptEngineResolver);

      dmnEngineConfiguration.setScriptEngineResolver(scriptEngineResolver);
    }

    // do not override the el provider if set
    if (dmnEngineConfiguration.getElProvider() == null) {
      ensureNotNull("elProvider", elProvider);
      dmnEngineConfiguration.setElProvider(elProvider);
    }

    if (dmnEngineConfiguration.getFeelCustomFunctionProviders() == null) {
      dmnEngineConfiguration.setFeelCustomFunctionProviders(feelCustomFunctionProviders);
    }

    return dmnEngineConfiguration;
  }

  protected List<DmnDecisionEvaluationListener> createCustomPostDecisionEvaluationListeners() {
    ensureNotNull("dmnHistoryEventProducer", dmnHistoryEventProducer);
    // note that the history level may be null - see CAM-5165

    HistoryDecisionEvaluationListener historyDecisionEvaluationListener = new HistoryDecisionEvaluationListener(dmnHistoryEventProducer);

    List<DmnDecisionEvaluationListener> customPostDecisionEvaluationListeners = dmnEngineConfiguration
        .getCustomPostDecisionEvaluationListeners();
    customPostDecisionEvaluationListeners.add(new MetricsDecisionEvaluationListener());
    customPostDecisionEvaluationListeners.add(historyDecisionEvaluationListener);

    return customPostDecisionEvaluationListeners;
  }

  public DmnEngineConfigurationBuilder enableFeelLegacyBehavior(boolean dmnFeelEnableLegacyBehavior) {
    dmnEngineConfiguration
        .enableFeelLegacyBehavior(dmnFeelEnableLegacyBehavior);
    return this;
  }

}

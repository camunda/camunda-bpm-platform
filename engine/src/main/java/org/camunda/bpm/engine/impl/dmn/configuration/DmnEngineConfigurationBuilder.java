/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformer;
import org.camunda.bpm.engine.impl.dmn.el.ProcessEngineElProvider;
import org.camunda.bpm.engine.impl.dmn.transformer.DecisionDefinitionHandler;
import org.camunda.bpm.engine.impl.dmn.transformer.DecisionRequirementsDefinitionTransformHandler;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
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

  protected HistoryLevel historyLevel;
  protected DmnHistoryEventProducer dmnHistoryEventProducer;
  protected DmnScriptEngineResolver scriptEngineResolver;
  protected ExpressionManager expressionManager;

  /**
   * Creates a new builder to modify the given DMN engine configuration.
   */
  public DmnEngineConfigurationBuilder(DefaultDmnEngineConfiguration dmnEngineConfiguration) {
    ensureNotNull("dmnEngineConfiguration", dmnEngineConfiguration);

    this.dmnEngineConfiguration = dmnEngineConfiguration;
  }

  public DmnEngineConfigurationBuilder historyLevel(HistoryLevel historyLevel) {
    this.historyLevel = historyLevel;

    return this;
  }

  public DmnEngineConfigurationBuilder dmnHistoryEventProducer(DmnHistoryEventProducer dmnHistoryEventProducer) {
    this.dmnHistoryEventProducer = dmnHistoryEventProducer;

    return this;
  }

  public DmnEngineConfigurationBuilder scriptEngineResolver(DmnScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;

    return this;
  }

  public DmnEngineConfigurationBuilder expressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;

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
      ensureNotNull("expressionManager", expressionManager);

      ProcessEngineElProvider elProvider = new ProcessEngineElProvider(expressionManager);
      dmnEngineConfiguration.setElProvider(elProvider);
    }

    return dmnEngineConfiguration;
  }

  protected List<DmnDecisionEvaluationListener> createCustomPostDecisionEvaluationListeners() {
    ensureNotNull("dmnHistoryEventProducer", dmnHistoryEventProducer);
    // note that the history level may be null - see CAM-5165

    HistoryDecisionEvaluationListener historyDecisionEvaluationListener = new HistoryDecisionEvaluationListener(dmnHistoryEventProducer, historyLevel);

    List<DmnDecisionEvaluationListener> customPostDecisionEvaluationListeners = dmnEngineConfiguration
        .getCustomPostDecisionEvaluationListeners();
    customPostDecisionEvaluationListeners.add(new MetricsDecisionEvaluationListener());
    customPostDecisionEvaluationListeners.add(historyDecisionEvaluationListener);

    return customPostDecisionEvaluationListeners;
  }

}

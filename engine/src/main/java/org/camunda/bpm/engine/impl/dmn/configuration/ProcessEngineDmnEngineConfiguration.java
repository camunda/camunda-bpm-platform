/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.el.DmnScriptEngineResolver;
import org.camunda.bpm.engine.impl.dmn.el.ProcessEngineElProvider;
import org.camunda.bpm.engine.impl.dmn.transformer.DecisionDefinitionHandler;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.history.parser.HistoryDecisionTableListener;
import org.camunda.bpm.engine.impl.metrics.dmn.MetricsDecisionTableListener;
import org.camunda.bpm.model.dmn.instance.DecisionTable;

public class ProcessEngineDmnEngineConfiguration extends DefaultDmnEngineConfiguration {

  protected ExpressionManager expressionManager;

  public ProcessEngineDmnEngineConfiguration(
      DmnScriptEngineResolver scriptEngineResolver,
      HistoryDecisionTableListener historyDecisionTableListener,
      ExpressionManager expressionManager) {

    this.customPostDecisionTableEvaluationListeners.add(new MetricsDecisionTableListener());
    this.customPostDecisionTableEvaluationListeners.add(historyDecisionTableListener);
    this.scriptEngineResolver = scriptEngineResolver;
    this.expressionManager = expressionManager;
    // override decision table handler
    this.transformer.getElementTransformHandlerRegistry().addHandler(DecisionTable.class, new DecisionDefinitionHandler());
  }

  protected void initElProvider() {
    if(elProvider == null) {
      elProvider = new ProcessEngineElProvider(expressionManager);
    }
  }

}

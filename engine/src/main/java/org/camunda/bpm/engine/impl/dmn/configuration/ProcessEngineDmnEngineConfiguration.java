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

import org.camunda.bpm.dmn.engine.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.dmn.el.ProcessEngineElProvider;
import org.camunda.bpm.engine.impl.dmn.handler.ProcessEngineDmnElementHandlerRegistry;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.history.parser.HistoryDecisionTableListener;
import org.camunda.bpm.engine.impl.metrics.dmn.MetricsDecisionTableListener;

public class ProcessEngineDmnEngineConfiguration extends DmnEngineConfigurationImpl {

  protected ExpressionManager expressionManager;

  public ProcessEngineDmnEngineConfiguration(
      DmnScriptEngineResolver scriptEngineResolver,
      HistoryDecisionTableListener historyDecisionTableListener,
      ExpressionManager expressionManager) {
    this.scriptEngineResolver = scriptEngineResolver;
    this.expressionManager = expressionManager;
    this.customPostDmnDecisionTableListeners.add(new MetricsDecisionTableListener());
  	this.customPostDmnDecisionTableListeners.add(historyDecisionTableListener);
  }

  protected void initElementHandlerRegistry() {
    if (elementHandlerRegistry == null) {
      elementHandlerRegistry = new ProcessEngineDmnElementHandlerRegistry();
    }
  }

  protected void initElProvider() {
    if(elProvider == null) {
      elProvider = new ProcessEngineElProvider(expressionManager);
    }
  }

}

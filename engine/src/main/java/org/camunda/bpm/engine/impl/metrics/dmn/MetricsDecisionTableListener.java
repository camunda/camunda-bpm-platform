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

package org.camunda.bpm.engine.impl.metrics.dmn;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.management.Metrics;

public class MetricsDecisionTableListener implements DmnDecisionTableEvaluationListener {

  public void notify(DmnDecisionTableEvaluationEvent evaluationEvent) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (processEngineConfiguration != null && processEngineConfiguration.isMetricsEnabled()) {
      processEngineConfiguration
        .getMetricsRegistry()
        .markOccurrence(Metrics.EXECUTED_DECISION_ELEMENTS, evaluationEvent.getExecutedDecisionElements());
    }
  }

}

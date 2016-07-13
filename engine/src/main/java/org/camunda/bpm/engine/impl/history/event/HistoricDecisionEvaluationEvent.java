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

package org.camunda.bpm.engine.impl.history.event;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Container for history entities which belongs to a decision evaluation. Only
 * the containing history entities should be persisted.
 */
public class HistoricDecisionEvaluationEvent extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  protected HistoricDecisionInstanceEntity rootHistoricDecisionInstance;

  protected Collection<HistoricDecisionInstanceEntity> requiredHistoricDecisionInstances = new ArrayList<HistoricDecisionInstanceEntity>();

  public HistoricDecisionInstanceEntity getRootHistoricDecisionInstance() {
    return rootHistoricDecisionInstance;
  }

  public void setRootHistoricDecisionInstance(HistoricDecisionInstanceEntity rootHistoricDecisionInstance) {
    this.rootHistoricDecisionInstance = rootHistoricDecisionInstance;
  }

  public Collection<HistoricDecisionInstanceEntity> getRequiredHistoricDecisionInstances() {
    return requiredHistoricDecisionInstances;
  }

  public void setRequiredHistoricDecisionInstances(Collection<HistoricDecisionInstanceEntity> requiredHistoricDecisionInstances) {
    this.requiredHistoricDecisionInstances = requiredHistoricDecisionInstances;
  }

}

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
package org.camunda.bpm.engine.impl.cfg.multitenancy;

import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.DecisionDefinition;

/**
 * Provides information about a historic decision instance to a {@link TenantIdProvider} implementation.
 *
 * @author Kristin Polenz
 * @since 7.5
 */
public class TenantIdProviderHistoricDecisionInstanceContext {

  protected DecisionDefinition decisionDefinition;

  protected DelegateExecution execution;

  protected DelegateCaseExecution caseExecution;

  public TenantIdProviderHistoricDecisionInstanceContext(DecisionDefinition decisionDefinition) {
    this.decisionDefinition = decisionDefinition;
  }

  public TenantIdProviderHistoricDecisionInstanceContext(DecisionDefinition decisionDefinition, DelegateExecution execution) {
    this(decisionDefinition);
    this.execution = execution;
  }

  public TenantIdProviderHistoricDecisionInstanceContext(DecisionDefinition decisionDefinition, DelegateCaseExecution caseExecution) {
    this(decisionDefinition);
    this.caseExecution = caseExecution;
  }

  /**
   * @return the decision definition of the historic decision instance which is being evaluated
   */
  public DecisionDefinition getDecisionDefinition() {
    return decisionDefinition;
  }

  /**
   * @return the execution. This method returns the execution of the process instance
   * which evaluated the decision definition.
   */
  public DelegateExecution getExecution() {
    return execution;
  }

  /**
   * @return the case execution. This method returns the case execution of the CMMN case task
   * which evaluated the decision definition.
   */
  public DelegateCaseExecution getCaseExecution() {
    return caseExecution;
  }

}

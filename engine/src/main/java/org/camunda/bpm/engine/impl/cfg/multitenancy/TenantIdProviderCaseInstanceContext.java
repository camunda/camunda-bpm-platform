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
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Provides information about a starting case instance to a {@link TenantIdProvider} implementation.
 *
 * @author Kristin Polenz
 * @since 7.5
 */
public class TenantIdProviderCaseInstanceContext {

  protected CaseDefinition caseDefinition;

  protected VariableMap variables;

  protected DelegateExecution superExecution;

  protected DelegateCaseExecution superCaseExecution;

  public TenantIdProviderCaseInstanceContext(CaseDefinition caseDefinition, VariableMap variables) {
    this.caseDefinition = caseDefinition;
    this.variables = variables;
  }

  public TenantIdProviderCaseInstanceContext(CaseDefinition caseDefinition, VariableMap variables, DelegateExecution superExecution) {
    this(caseDefinition, variables);
    this.superExecution = superExecution;
  }

  public TenantIdProviderCaseInstanceContext(CaseDefinition caseDefinition, VariableMap variables, DelegateCaseExecution superCaseExecution) {
    this(caseDefinition, variables);
    this.superCaseExecution = superCaseExecution;
  }

  /**
   * @return the case definition of the case instance which is being started
   */
  public CaseDefinition getCaseDefinition() {
    return caseDefinition;
  }

  /**
   * @return the variables which were passed to the starting case instance
   */
  public VariableMap getVariables() {
    return variables;
  }

  /**
   * @return the super execution. <code>null</code> if the starting case instance is a root process instance and not started using a call activity.
   * If the case instance is started using a call activity, this method returns the execution in the super process
   * instance executing the call activity.
   */
  public DelegateExecution getSuperExecution() {
    return superExecution;
  }

  /**
   * @return the super case execution. <code>null</code> if the starting case instance is not a sub case instance started using a CMMN case task.
   */
  public DelegateCaseExecution getSuperCaseExecution() {
    return superCaseExecution;
  }

}

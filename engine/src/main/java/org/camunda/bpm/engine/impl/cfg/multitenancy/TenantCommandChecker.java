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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.TenantManager;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * {@link CommandChecker} to ensure that commands are only executed for
 * entities which belongs to one of the authenticated tenants.
 */
public class TenantCommandChecker implements CommandChecker {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  @Override
  public void checkEvaluateDecision(DecisionDefinition decisionDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(decisionDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("evaluate the decision", decisionDefinition);
    }
  }

  @Override
  public void checkCreateProcessInstance(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the process definition", processDefinition);
    }
  }

  @Override
  public void checkReadProcessDefinition(ProcessDefinition processDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("get the process definition", processDefinition);
    }
  }

  @Override
  public void checkCreateCaseInstance(CaseDefinition caseDefinition) {
    if (!getTenantManager().isAuthenticatedTenant(caseDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("create an instance of the case definition", caseDefinition);
    }
  }

  protected TenantManager getTenantManager() {
    return Context.getCommandContext().getTenantManager();
  }

}

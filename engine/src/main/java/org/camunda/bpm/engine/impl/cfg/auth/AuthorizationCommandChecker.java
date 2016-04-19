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

package org.camunda.bpm.engine.impl.cfg.auth;

import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * {@link CommandChecker} that uses the {@link AuthorizationManager} to perform
 * authorization checks.
 */
public class AuthorizationCommandChecker implements CommandChecker {

  @Override
  public void checkEvaluateDecision(DecisionDefinition decisionDefinition) {
    getAuthorizationManager().checkAuthorization(CREATE_INSTANCE, DECISION_DEFINITION, decisionDefinition.getKey());
  }

  @Override
  public void checkCreateProcessInstance(ProcessDefinition processDefinition) {
    // necessary permissions:
    // - CREATE on PROCESS_INSTANCE
    // AND
    // - CREATE_INSTANCE on PROCESS_DEFINITION
    getAuthorizationManager().checkAuthorization(CREATE, PROCESS_INSTANCE);
    getAuthorizationManager().checkAuthorization(CREATE_INSTANCE, PROCESS_DEFINITION, processDefinition.getKey());
  }

  @Override
  public void checkReadProcessDefinition(ProcessDefinition processDefinition) {
    // TODO extract logic from manager and delete the method
    getAuthorizationManager().checkReadProcessDefinition(processDefinition.getKey());
  }

  @Override
  public void checkCreateCaseInstance(CaseDefinition caseDefinition) {
    // no authorization check for CMMN
  }

  protected AuthorizationManager getAuthorizationManager() {
    return Context.getCommandContext().getAuthorizationManager();
  }

}

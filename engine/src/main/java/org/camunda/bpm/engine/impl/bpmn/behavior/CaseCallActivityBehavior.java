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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.util.CallableElementUtil.getCaseDefinitionToCall;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingCalledCaseInstance;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParseContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.MigrationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Implementation to create a new {@link CaseInstance} using the BPMN 2.0 call activity
 *
 * @author Roman Smirnov
 *
 */
public class CaseCallActivityBehavior extends CallableElementActivityBehavior implements MigrationObserverBehavior {

  protected void startInstance(ActivityExecution execution, VariableMap variables, String businessKey) {
    CmmnCaseDefinition definition = getCaseDefinitionToCall(execution, getCallableElement());
    CmmnCaseInstance caseInstance = execution.createSubCaseInstance(definition, businessKey);
    caseInstance.create(variables);
  }

  @Override
  public void migrateScope(ActivityExecution scopeExecution) {
  }

  @Override
  public void onParseMigratingInstance(MigratingInstanceParseContext parseContext, MigratingActivityInstance migratingInstance) {
    ActivityImpl callActivity = (ActivityImpl) migratingInstance.getSourceScope();

    // A call activity is typically scope and since we guarantee stability of scope executions during migration,
    // the superExecution link does not have to be maintained during migration.
    // There are some exceptions, though: A multi-instance call activity is not scope and therefore
    // does not have a dedicated scope execution. In this case, the link to the super execution
    // must be maintained throughout migration
    if (!callActivity.isScope()) {
      ExecutionEntity callActivityExecution = migratingInstance.resolveRepresentativeExecution();
      CaseExecutionEntity calledCaseInstance = callActivityExecution.getSubCaseInstance();
      migratingInstance.addMigratingDependentInstance(new MigratingCalledCaseInstance(calledCaseInstance));
    }
  }

}

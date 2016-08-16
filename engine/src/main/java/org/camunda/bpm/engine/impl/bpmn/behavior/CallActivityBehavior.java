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

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingCalledProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParseContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.MigrationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.variable.VariableMap;

import static org.camunda.bpm.engine.impl.util.CallableElementUtil.getProcessDefinitionToCall;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 *
 * @author Joram Barrez
 * @author Roman Smirnov
 */
public class CallActivityBehavior extends CallableElementActivityBehavior implements MigrationObserverBehavior {

  public CallActivityBehavior() {
  }

  public CallActivityBehavior(String className) {
    super(className);
  }

  public CallActivityBehavior(Expression expression) {
    super(expression);
  }

  @Override
  protected void startInstance(ActivityExecution execution, VariableMap variables, String businessKey) {
    ProcessDefinitionImpl definition = getProcessDefinitionToCall(execution, getCallableElement());
    PvmProcessInstance processInstance = execution.createSubProcessInstance(definition, businessKey);
    processInstance.start(variables);
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
      ExecutionEntity calledProcessInstance = callActivityExecution.getSubProcessInstance();
      migratingInstance.addMigratingDependentInstance(new MigratingCalledProcessInstance(calledProcessInstance));
    }
  }

}

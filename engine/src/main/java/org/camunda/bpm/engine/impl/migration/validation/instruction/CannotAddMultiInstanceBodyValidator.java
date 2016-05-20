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
package org.camunda.bpm.engine.impl.migration.validation.instruction;

import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker.WalkCondition;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;

/**
 * Validates that the target process definition cannot add a migrating multi-instance body.
 *
 * @author Thorben Lindhauer
 */
public class CannotAddMultiInstanceBodyValidator implements MigrationInstructionValidator {

  @Override
  public void validate(ValidatingMigrationInstruction instruction, final ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {
    ActivityImpl targetActivity = instruction.getTargetActivity();

    FlowScopeWalker flowScopeWalker = new FlowScopeWalker(targetActivity.getFlowScope());
    MiBodyCollector miBodyCollector = new MiBodyCollector();
    flowScopeWalker.addPreVisitor(miBodyCollector);

    // walk until a target scope is found that is mapped
    flowScopeWalker.walkWhile(new WalkCondition<ScopeImpl>() {
      @Override
      public boolean isFulfilled(ScopeImpl element) {
        return element == null || !instructions.getInstructionsByTargetScope(element).isEmpty();
      }
    });

    if (miBodyCollector.firstMiBody != null) {
      report.addFailure("Target activity '" + targetActivity.getId() + "' is a descendant of multi-instance body '" +
        miBodyCollector.firstMiBody.getId() + "' that is not mapped from the source process definition.");
    }
  }

  public static class MiBodyCollector implements TreeVisitor<ScopeImpl> {

    protected ScopeImpl firstMiBody;

    @Override
    public void visit(ScopeImpl obj) {
      if (firstMiBody == null && obj != null && isMiBody(obj)) {
        firstMiBody = obj;
      }
    }

    protected boolean isMiBody(ScopeImpl scope) {
      return scope.getActivityBehavior() instanceof MultiInstanceActivityBehavior;
    }
  }

}

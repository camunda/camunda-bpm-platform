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

package org.camunda.bpm.engine.impl.migration.validation.instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CaseCallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

public class SameBehaviorInstructionValidator implements MigrationInstructionValidator {

  public static final List<Set<Class<?>>> EQUIVALENT_BEHAVIORS =
      new ArrayList<Set<Class<?>>>();

  static {
    EQUIVALENT_BEHAVIORS.add(CollectionUtil.<Class<?>>asHashSet(
      CallActivityBehavior.class, CaseCallActivityBehavior.class
    ));

    EQUIVALENT_BEHAVIORS.add(CollectionUtil.<Class<?>>asHashSet(
      SubProcessActivityBehavior.class, EventSubProcessActivityBehavior.class
    ));
  }

  protected Map<Class<?>, Set<Class<?>>> equivalentBehaviors = new HashMap<Class<?>, Set<Class<?>>>();

  public SameBehaviorInstructionValidator() {
    this(EQUIVALENT_BEHAVIORS);
  }

  public SameBehaviorInstructionValidator(List<Set<Class<?>>> equivalentBehaviors) {
    for (Set<Class<?>> equivalenceClass : equivalentBehaviors) {
      for (Class<?> clazz : equivalenceClass) {
        this.equivalentBehaviors.put(clazz, equivalenceClass);
      }
    }
  }

  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions, MigrationInstructionValidationReportImpl report) {
    ActivityImpl sourceActivity = instruction.getSourceActivity();
    ActivityImpl targetActivity = instruction.getTargetActivity();

    Class<?> sourceBehaviorClass = sourceActivity.getActivityBehavior().getClass();
    Class<?> targetBehaviorClass = targetActivity.getActivityBehavior().getClass();

    if (!sameBehavior(sourceBehaviorClass, targetBehaviorClass)) {
      report.addFailure("Activities have incompatible types "
          + "(" + sourceBehaviorClass.getSimpleName() + " is not compatible with " + targetBehaviorClass.getSimpleName() + ")");
    }
  }

  protected boolean sameBehavior(Class<?> sourceBehavior, Class<?> targetBehavior) {

    if (sourceBehavior == targetBehavior) {
      return true;
    }
    else {
      Set<Class<?>> equivalentBehaviors = this.equivalentBehaviors.get(sourceBehavior);
      if (equivalentBehaviors != null) {
        return equivalentBehaviors.contains(targetBehavior);
      }
      else {
        return false;
      }
    }
  }

}

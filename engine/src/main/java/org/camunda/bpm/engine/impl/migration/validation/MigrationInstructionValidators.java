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

package org.camunda.bpm.engine.impl.migration.validation;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

public class MigrationInstructionValidators {

  // Validators

  public static final MigrationInstructionValidator ACTIVITIES_CAN_BE_MIGRATED = new MigrationInstructionValidator() {
    public final List<MigrationActivityValidator> sourceActivityValidators = Arrays.asList(
      MigrationActivityValidators.SUPPORTED_ACTIVITY,
      MigrationActivityValidators.SUPPORTED_BOUNDARY_EVENT,
      MigrationActivityValidators.NOT_MULTI_INSTANCE_CHILD,
      MigrationActivityValidators.NOT_EVENT_SUB_PROCESS_CHILD,
      MigrationActivityValidators.HAS_NO_EVENT_SUB_PROCESS_CHILD
    );

    public final List<MigrationActivityValidator> targetActivityValidators = Arrays.asList(
      MigrationActivityValidators.SUPPORTED_ACTIVITY,
      MigrationActivityValidators.SUPPORTED_BOUNDARY_EVENT,
      MigrationActivityValidators.NOT_MULTI_INSTANCE_CHILD,
      MigrationActivityValidators.NOT_EVENT_SUB_PROCESS_CHILD,
      MigrationActivityValidators.HAS_NO_EVENT_SUB_PROCESS_CHILD
    );

    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return canActivitiesBeMigrated(instruction.getSourceActivityIds(), sourceProcessDefinition, sourceActivityValidators) &&
        canActivitiesBeMigrated(instruction.getTargetActivityIds(), targetProcessDefinition, targetActivityValidators);
    }

    public boolean isInstructionValid(MigrationInstruction instruction, List<MigrationInstruction> instructions, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition);
    }

    protected boolean canActivitiesBeMigrated(List<String> activityIds, ProcessDefinitionImpl processDefinition, List<MigrationActivityValidator> activityValidators) {
      for (String activityId : activityIds) {
        if (!canActivityBeMigrated(activityId, processDefinition, activityValidators)) {
          return false;
        }
      }
      return true;
    }

    protected boolean canActivityBeMigrated(String activityId, ProcessDefinitionImpl processDefinition, List<MigrationActivityValidator> activityValidators) {
      for (MigrationActivityValidator activityValidator : activityValidators) {
        if (!activityValidator.canBeMigrated(activityId, processDefinition)) {
          return false;
        }
      }
      return true;
    }

  };

  public static final MigrationInstructionValidator ONE_TO_ONE_VALIDATOR = new MigrationInstructionValidator() {
    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return instruction.getSourceActivityIds().size() == 1 && instruction.getTargetActivityIds().size() == 1;
    }

    public boolean isInstructionValid(MigrationInstruction instruction, List<MigrationInstruction> instructions, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition);
    }
  };

  public static final MigrationInstructionValidator SAME_ID_VALIDATOR = new MigrationInstructionValidator() {
    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return ONE_TO_ONE_VALIDATOR.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition) &&
        instruction.getSourceActivityIds().contains(instruction.getTargetActivityIds().get(0));
    }

    public boolean isInstructionValid(MigrationInstruction instruction, List<MigrationInstruction> instructions, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition);
    }
  };

  public static final MigrationInstructionValidator SAME_SCOPE = new MigrationInstructionValidator() {
    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return ONE_TO_ONE_VALIDATOR.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition) &&
        haveSameScope(instruction.getSourceActivityIds().get(0), instruction.getTargetActivityIds().get(0), sourceProcessDefinition, targetProcessDefinition);
    }

    public boolean isInstructionValid(MigrationInstruction instruction, List<MigrationInstruction> instructions, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition);
    }
  };

  public static final MigrationInstructionValidator SAME_EVENT_SCOPE = new MigrationInstructionValidator() {
    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return ONE_TO_ONE_VALIDATOR.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition) &&
        haveSameEventScope(instruction.getSourceActivityIds().get(0), instruction.getTargetActivityIds().get(0), sourceProcessDefinition, targetProcessDefinition);
    }

    public boolean isInstructionValid(MigrationInstruction instruction, List<MigrationInstruction> instructions, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return ONE_TO_ONE_VALIDATOR.isInstructionValid(instruction, instructions, sourceProcessDefinition, targetProcessDefinition) &&
        haveSameEventScope(instruction.getSourceActivityIds().get(0), instruction.getTargetActivityIds().get(0), instructions, sourceProcessDefinition, targetProcessDefinition);
    }
  };

  // Helper


  protected static boolean haveSameScope(String sourceActivityId, String targetActivityId, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    ScopeImpl sourceFlowScope = sourceProcessDefinition.findActivity(sourceActivityId).getFlowScope();
    ScopeImpl targetFlowScope = targetProcessDefinition.findActivity(targetActivityId).getFlowScope();

    return (isProcessDefinition(sourceFlowScope) && isProcessDefinition(targetFlowScope)) || sourceFlowScope.getId().equals(targetFlowScope.getId());
  }

  protected static boolean haveSameEventScope(String sourceActivityId, String targetActivityId, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    ScopeImpl sourceEventScope = sourceProcessDefinition.findActivity(sourceActivityId).getEventScope();
    ScopeImpl targetEventScope = targetProcessDefinition.findActivity(targetActivityId).getEventScope();

    return (sourceEventScope == null && targetEventScope == null) ||
      ((sourceEventScope != null && targetEventScope != null) && (sourceEventScope.getId().equals(targetEventScope.getId())));
  }

  protected static boolean haveSameEventScope(String sourceActivityId, String targetActivityId, List<MigrationInstruction> instructions, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    ScopeImpl sourceEventScope = sourceProcessDefinition.findActivity(sourceActivityId).getEventScope();
    ScopeImpl targetEventScope = targetProcessDefinition.findActivity(targetActivityId).getEventScope();

    if (sourceEventScope == null && targetEventScope == null) {
      return true;
    }
    else if (sourceEventScope != null) {
      String migratedSourceEventScopeId = resolveActivityIdMappingFromMigrationInstructions(sourceEventScope.getId(), instructions);
      return migratedSourceEventScopeId != null && migratedSourceEventScopeId.equals(targetEventScope.getId());
    }
    else {
      return false;
    }
  }

  protected static String resolveActivityIdMappingFromMigrationInstructions(String activityId, List<MigrationInstruction> instructions) {
    for (MigrationInstruction instruction : instructions) {
      // TODO: this could be more than one activity ID if instructions for n:m mappings are allowed
      if (activityId.equals(instruction.getSourceActivityIds().get(0))) {
        return instruction.getTargetActivityIds().get(0);
      }
    }
    // if the source activity is not migrated
    return null;
  }

  protected static boolean isProcessDefinition(ScopeImpl scope) {
    return scope.getProcessDefinition() == scope;
  }

}

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

import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.parser.Escalation;
import org.camunda.bpm.engine.impl.bpmn.parser.EscalationEventDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionHierarchyWalker;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionMappingCollector;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionTuple;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;

/**
 * The activity bahavior for an intermediate throwing escalation event and an escalation end event.
 *
 * @author Philipp Ossler
 *
 */
public class ThrowEscalationEventActivityBehavior extends AbstractBpmnActivityBehavior {

  protected final Escalation escalation;

  public ThrowEscalationEventActivityBehavior(Escalation escalation) {
    this.escalation = escalation;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {

    final EscalationEventDefinitionFinder escalationEventDefinitionFinder = new EscalationEventDefinitionFinder(escalation.getEscalationCode());
    ActivityExecutionMappingCollector activityExecutionMappingCollector = new ActivityExecutionMappingCollector(execution);

    ActivityExecutionHierarchyWalker walker = new ActivityExecutionHierarchyWalker(execution);
    walker.addScopePreVisitor(escalationEventDefinitionFinder);
    walker.addExecutionPreVisitor(activityExecutionMappingCollector);

    walker.walkUntil(new WalkCondition<ActivityExecutionTuple>() {

      @Override
      public boolean isFulfilled(ActivityExecutionTuple element) {
        return escalationEventDefinitionFinder.getEscalationEventDefinition() != null || element == null;
      }
    });

    EscalationEventDefinition escalationEventDefinition = escalationEventDefinitionFinder.getEscalationEventDefinition();
    if (escalationEventDefinition != null) {

      PvmActivity escalationHandler = escalationEventDefinition.getEscalationHandler();
      PvmScope escalationScope = getScopeForEscalation(escalationEventDefinition);
      ActivityExecution escalationExecution = activityExecutionMappingCollector.getExecutionForScope(escalationScope);

      escalationExecution.executeActivity(escalationHandler);
    }

    if (escalationEventDefinition == null || !escalationEventDefinition.isCancelActivity()) {
      leave(execution);
    }
  }

  protected PvmScope getScopeForEscalation(EscalationEventDefinition escalationEventDefinition) {
    PvmActivity escalationHandler = escalationEventDefinition.getEscalationHandler();
    if (escalationEventDefinition.isCancelActivity()) {
      return escalationHandler.getEventScope();
    } else {
      return escalationHandler.getFlowScope();
    }
  }

  public class EscalationEventDefinitionFinder implements TreeVisitor<PvmScope> {

    private EscalationEventDefinition escalationEventDefinition;

    private final String escalationCode;

    public EscalationEventDefinitionFinder(String escalationCode) {
      this.escalationCode = escalationCode;
    }

    @Override
    public void visit(PvmScope scope) {

      List<EscalationEventDefinition> escalationEventDefinitions = scope.getProperties().get(BpmnProperties.ESCALATION_EVENT_DEFINITIONS);
      this.escalationEventDefinition = findMatchingEscalationEventDefinition(escalationEventDefinitions);
    }

    protected EscalationEventDefinition findMatchingEscalationEventDefinition(List<EscalationEventDefinition> escalationEventDefinitions) {
      for (EscalationEventDefinition escalationEventDefinition : escalationEventDefinitions) {
        if (escalationEventDefinition.getEscalationCode() == null || escalationEventDefinition.getEscalationCode().equals(escalationCode)) {
          return escalationEventDefinition;
        }
      }
      return null;
    }

    public EscalationEventDefinition getEscalationEventDefinition() {
      return escalationEventDefinition;
    }

  }

}

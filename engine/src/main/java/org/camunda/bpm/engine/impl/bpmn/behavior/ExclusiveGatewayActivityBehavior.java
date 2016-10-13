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

import java.util.Iterator;

import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;


/**
 * implementation of the Exclusive Gateway/XOR gateway/exclusive data-based gateway
 * as defined in the BPMN specification.
 *
 * @author Joram Barrez
 */
public class ExclusiveGatewayActivityBehavior extends GatewayActivityBehavior {

  protected static BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  /**
   * The default behaviour of BPMN, taking every outgoing sequence flow
   * (where the condition evaluates to true), is not valid for an exclusive
   * gateway.
   *
   * Hence, this behaviour is overriden and replaced by the correct behavior:
   * selecting the first sequence flow which condition evaluates to true
   * (or which hasn't got a condition) and leaving the activity through that
   * sequence flow.
   *
   * If no sequence flow is selected (ie all conditions evaluate to false),
   * then the default sequence flow is taken (if defined).
   */
  @Override
  public void doLeave(ActivityExecution execution) {

    LOG.leavingActivity(execution.getActivity().getId());

    PvmTransition outgoingSeqFlow = null;
    String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
    Iterator<PvmTransition> transitionIterator = execution.getActivity().getOutgoingTransitions().iterator();
    while (outgoingSeqFlow == null && transitionIterator.hasNext()) {
      PvmTransition seqFlow = transitionIterator.next();

      Condition condition = (Condition) seqFlow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
      if ( (condition == null && (defaultSequenceFlow == null || !defaultSequenceFlow.equals(seqFlow.getId())) )
              || (condition != null && condition.evaluate(execution)) ) {

        LOG.outgoingSequenceFlowSelected(seqFlow.getId());
        outgoingSeqFlow = seqFlow;
      }
    }

    if (outgoingSeqFlow != null) {
      execution.leaveActivityViaTransition(outgoingSeqFlow);
    } else {

      if (defaultSequenceFlow != null) {
        PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
        if (defaultTransition != null) {
          execution.leaveActivityViaTransition(defaultTransition);
        } else {
          throw LOG.missingDefaultFlowException(execution.getActivity().getId(), defaultSequenceFlow);
        }
      } else {
        //No sequence flow could be found, not even a default one
        throw LOG.stuckExecutionException(execution.getActivity().getId());
      }
    }
  }

}

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
package org.camunda.bpm.engine.test.standalone.pvm.activities;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;

/**
 * @author Tom Baeyens
 */
public class ParallelGateway implements ActivityBehavior {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  public void execute(ActivityExecution execution) {
    PvmActivity activity = execution.getActivity();

    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();

    execution.inactivate();

    List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(activity);

    int nbrOfExecutionsToJoin = execution.getActivity().getIncomingTransitions().size();
    int nbrOfExecutionsJoined = joinedExecutions.size();

    if (nbrOfExecutionsJoined==nbrOfExecutionsToJoin) {
      LOG.debug("parallel gateway '"+activity.getId()+"' activates: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
      execution.leaveActivityViaTransitions(outgoingTransitions, joinedExecutions);

    } else {
      LOG.debug("parallel gateway '"+activity.getId()+"' does not activate: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
    }
  }
}

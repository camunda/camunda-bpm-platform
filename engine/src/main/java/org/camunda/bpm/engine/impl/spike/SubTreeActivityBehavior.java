/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.spike;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

public class SubTreeActivityBehavior extends AbstractBpmnActivityBehavior {

  public static final String LOOP_RANGE_START = "loopRangeStart";
  public static final String LOOP_RANGE_END = "loopRangeEnd";

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    int rangeStart = (int) execution.getVariable(LOOP_RANGE_START);
    int rangeEnd = (int) execution.getVariable(LOOP_RANGE_END);
    PvmActivity childActivity = execution.getActivity().getActivities().get(0);

    List<ActivityExecution> children = new ArrayList<>();
    for (int i = rangeStart; i < rangeEnd; i++) {
      ActivityExecution child = createConcurrentExecution(execution);
      children.add(child);
    }

    for (int i = rangeStart; i < rangeEnd; i++) {
      ActivityExecution child = children.get(i - rangeStart);
      performInstance(child, childActivity, i);
    }
  }


  protected void performInstance(ActivityExecution execution, PvmActivity activity, int loopCounter) {
    execution.setVariableLocal(MultiInstanceActivityBehavior.LOOP_COUNTER, loopCounter);
//    evaluateCollectionVariable(execution, loopCounter);
    execution.setEnded(false);
    execution.setActive(true);
    execution.executeActivity(activity);
  }


  protected ActivityExecution createConcurrentExecution(ActivityExecution scopeExecution) {
    ActivityExecution concurrentChild = scopeExecution.createExecution();
    scopeExecution.forceUpdate();
    concurrentChild.setConcurrent(true);
    concurrentChild.setScope(false);
    return concurrentChild;
  }
}

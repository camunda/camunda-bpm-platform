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
package org.camunda.bpm.engine.test.standalone.pvm.activities;

import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Tom Baeyens
 */
public class While implements ActivityBehavior {

  String variableName;
  int from;
  int to;

  public While(String variableName, int from, int to) {
    this.variableName = variableName;
    this.from = from;
    this.to = to;
  }

  public void execute(ActivityExecution execution) throws Exception {
    PvmTransition more = execution.getActivity().findOutgoingTransition("more");
    PvmTransition done = execution.getActivity().findOutgoingTransition("done");

    Integer value = (Integer) execution.getVariable(variableName);

    if (value==null) {
      execution.setVariable(variableName, from);
      execution.leaveActivityViaTransition(more);

    } else {
      value = value+1;

      if (value<to) {
        execution.setVariable(variableName, value);
        execution.leaveActivityViaTransition(more);

      } else {
        execution.leaveActivityViaTransition(done);
      }
    }
  }

}

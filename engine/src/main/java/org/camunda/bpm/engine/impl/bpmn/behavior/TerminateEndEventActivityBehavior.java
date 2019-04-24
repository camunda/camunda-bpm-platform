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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;

/**
 * <p>The BPMN terminate End Event.</p>
 *
 * <p>The start behavior of the terminate end event is {@link ActivityStartBehavior#INTERRUPT_FLOW_SCOPE}.
 * as a result, the current scope will be interrupted (all concurrent executions cancelled) and this
 * behavior is entered with the scope execution.</p>
 *
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
    // we are the last execution inside this scope: calling end() ends this scope.
    execution.end(true);
  }

}

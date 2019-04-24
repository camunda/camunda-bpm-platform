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
package org.camunda.bpm.engine.impl.cmmn.operation;

import static org.camunda.bpm.engine.delegate.CaseExecutionListener.CREATE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.util.ActivityBehaviorUtil.getActivityBehavior;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;

/**
 * @author Roman Smirnov
 *
 */
public class AtomicOperationCaseInstanceCreate extends AbstractCmmnEventAtomicOperation {

  public String getCanonicalName() {
    return "case-instance-create";
  }

  protected String getEventName() {
    return CREATE;
  }

  protected CmmnExecution eventNotificationsStarted(CmmnExecution execution) {
    // the case instance perform a transition directly
    // to state ACTIVE
    execution.setCurrentState(ACTIVE);

    return execution;
  }

  protected void postTransitionNotification(CmmnExecution execution) {
    // the case instance is associated with the
    // casePlanModel as activity
    CmmnActivityBehavior behavior = getActivityBehavior(execution);

    // perform start() on associated behavior
    // because the case instance is ACTIVE
    behavior.started(execution);
  }

}

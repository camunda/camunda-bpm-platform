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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmLogger;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.CompensationBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Daniel Meyer
 *
 */
public abstract class PvmAtomicOperationActivityInstanceEnd extends AbstractPvmEventAtomicOperation {

  private final static PvmLogger LOG = ProcessEngineLogger.PVM_LOGGER;

  @Override
  protected PvmExecutionImpl eventNotificationsStarted(PvmExecutionImpl execution) {
    execution.incrementSequenceCounter();

    // hack around execution tree structure not being in sync with activity instance concept:
    // if we end a scope activity, take remembered activity instance from parent and set on
    // execution before calling END listeners.
    PvmExecutionImpl parent = execution.getParent();
    PvmActivity activity = execution.getActivity();
    if (parent != null && execution.isScope() &&
        activity != null && activity.isScope() &&
        (activity.getActivityBehavior() instanceof CompositeActivityBehavior
            || (CompensationBehavior.isCompensationThrowing(execution))
              && !LegacyBehavior.isCompensationThrowing(execution))) {

      LOG.debugLeavesActivityInstance(execution, execution.getActivityInstanceId());

      // use remembered activity instance id from parent
      execution.setActivityInstanceId(parent.getActivityInstanceId());
      // make parent go one scope up.
      parent.leaveActivityInstance();


    }
    execution.setTransition(null);

    return execution;

  }

  @Override
  protected void eventNotificationsFailed(PvmExecutionImpl execution, Exception e) {
    execution.activityInstanceEndListenerFailure();
    super.eventNotificationsFailed(execution, e);
  }

  @Override
  protected boolean isSkipNotifyListeners(PvmExecutionImpl execution) {
    // listeners are skipped if this execution is not part of an activity instance.
    return execution.getActivityInstanceId() == null;
  }


}

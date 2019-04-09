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

import static org.camunda.bpm.engine.impl.util.ActivityBehaviorUtil.getActivityBehavior;

import org.camunda.bpm.engine.impl.bpmn.behavior.FlowNodeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmLogger;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public class PvmAtomicOperationActivityLeave implements PvmAtomicOperation {

  private final static PvmLogger LOG = PvmLogger.PVM_LOGGER;

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

  public void execute(PvmExecutionImpl execution) {

    execution.activityInstanceDone();

    ActivityBehavior activityBehavior = getActivityBehavior(execution);

    if (activityBehavior instanceof FlowNodeActivityBehavior) {
      FlowNodeActivityBehavior behavior = (FlowNodeActivityBehavior) activityBehavior;

      ActivityImpl activity = execution.getActivity();
      String activityInstanceId = execution.getActivityInstanceId();
      if(activityInstanceId != null) {
        LOG.debugLeavesActivityInstance(execution, activityInstanceId);
      }

      try {
        behavior.doLeave(execution);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new PvmException("couldn't leave activity <"+activity.getProperty("type")+" id=\""+activity.getId()+"\" ...>: "+e.getMessage(), e);
      }
    } else {
      throw new PvmException("Behavior of current activity is not an instance of " + FlowNodeActivityBehavior.class.getSimpleName() + ". Execution " + execution);
    }
  }

  public String getCanonicalName() {
    return "activity-leave";
  }

  public boolean isAsyncCapable() {
    return false;
  }
}

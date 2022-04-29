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

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmLogger;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class PvmAtomicOperationCreateScope implements PvmAtomicOperation {

  private final static PvmLogger LOG = PvmLogger.PVM_LOGGER;

  public void execute(PvmExecutionImpl execution) {

    // reset activity instance id before creating the scope
    execution.setActivityInstanceId(execution.getParentActivityInstanceId());

    PvmExecutionImpl propagatingExecution = null;
    PvmActivity activity = execution.getActivity();
    if (activity.isScope()) {
      propagatingExecution = execution.createExecution();
      propagatingExecution.setActivity(activity);
      propagatingExecution.setTransition(execution.getTransition());
      execution.setTransition(null);
      execution.setActive(false);
      execution.setActivity(null);
      LOG.createScope(execution, propagatingExecution);
      propagatingExecution.initialize();

    } else {
      propagatingExecution = execution;
    }


    scopeCreated(propagatingExecution);
  }

  /**
   * Called with the propagating execution
   */
  protected abstract void scopeCreated(PvmExecutionImpl execution);

}

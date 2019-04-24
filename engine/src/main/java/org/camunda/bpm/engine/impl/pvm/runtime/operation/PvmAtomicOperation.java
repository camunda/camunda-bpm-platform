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

import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public interface PvmAtomicOperation extends CoreAtomicOperation<PvmExecutionImpl>, AtomicOperation {

  PvmAtomicOperation PROCESS_START = new PvmAtomicOperationProcessStart();
  PvmAtomicOperation FIRE_PROCESS_START = new PvmAtomicOperationFireProcessStart();
  PvmAtomicOperation PROCESS_END = new PvmAtomicOperationProcessEnd();

  PvmAtomicOperation ACTIVITY_START = new PvmAtomicOperationActivityStart();
  PvmAtomicOperation ACTIVITY_START_CONCURRENT = new PvmAtomicOperationActivityStartConcurrent();
  PvmAtomicOperation ACTIVITY_START_CANCEL_SCOPE = new PvmAtomicOperationActivityStartCancelScope();
  PvmAtomicOperation ACTIVITY_START_INTERRUPT_SCOPE = new PvmAtomicOperationActivityStartInterruptEventScope();
  PvmAtomicOperation ACTIVITY_START_CREATE_SCOPE = new PvmAtomicOperationActivityStartCreateScope();
  PvmAtomicOperation ACTIVITY_INIT_STACK_NOTIFY_LISTENER_START = new PvmAtomicOperationActivityInitStackNotifyListenerStart();
  PvmAtomicOperation ACTIVITY_INIT_STACK_NOTIFY_LISTENER_RETURN = new PvmAtomicOperationActivityInitStackNotifyListenerReturn();
  PvmAtomicOperation ACTIVITY_INIT_STACK = new PvmAtomicOperationActivityInitStack(ACTIVITY_INIT_STACK_NOTIFY_LISTENER_START);
  PvmAtomicOperation ACTIVITY_INIT_STACK_AND_RETURN = new PvmAtomicOperationActivityInitStack(ACTIVITY_INIT_STACK_NOTIFY_LISTENER_RETURN);
  PvmAtomicOperation ACTIVITY_EXECUTE = new PvmAtomicOperationActivityExecute();
  PvmAtomicOperation ACTIVITY_NOTIFY_LISTENER_END = new PvmAtomicOperationActivityNotifyListenerEnd();
  PvmAtomicOperation ACTIVITY_END = new PvmAtomicOperationActivityEnd();
  PvmAtomicOperation FIRE_ACTIVITY_END = new PvmAtomicOperationFireActivityEnd();

  PvmAtomicOperation TRANSITION_NOTIFY_LISTENER_END = new PvmAtomicOperationTransitionNotifyListenerEnd();
  PvmAtomicOperation TRANSITION_DESTROY_SCOPE = new PvmAtomicOperationTransitionDestroyScope();
  PvmAtomicOperation TRANSITION_NOTIFY_LISTENER_TAKE = new PvmAtomicOperationTransitionNotifyListenerTake();
  PvmAtomicOperation TRANSITION_START_NOTIFY_LISTENER_TAKE = new PvmAtomicOperationStartTransitionNotifyListenerTake();
  PvmAtomicOperation TRANSITION_CREATE_SCOPE = new PvmAtomicOperationTransitionCreateScope();
  PvmAtomicOperation TRANSITION_INTERRUPT_FLOW_SCOPE = new PvmAtomicOperationsTransitionInterruptFlowScope();
  PvmAtomicOperation TRANSITION_NOTIFY_LISTENER_START = new PvmAtomicOperationTransitionNotifyListenerStart();

  PvmAtomicOperation DELETE_CASCADE = new PvmAtomicOperationDeleteCascade();
  PvmAtomicOperation DELETE_CASCADE_FIRE_ACTIVITY_END = new PvmAtomicOperationDeleteCascadeFireActivityEnd();

  PvmAtomicOperation ACTIVITY_LEAVE = new PvmAtomicOperationActivityLeave();
}

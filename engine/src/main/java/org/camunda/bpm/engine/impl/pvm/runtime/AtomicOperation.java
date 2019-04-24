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
package org.camunda.bpm.engine.impl.pvm.runtime;

import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;




/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
@Deprecated
public interface AtomicOperation extends CoreAtomicOperation<PvmExecutionImpl> {

  AtomicOperation PROCESS_START = PvmAtomicOperation.PROCESS_START;
  AtomicOperation PROCESS_START_INITIAL = PvmAtomicOperation.PROCESS_START_INITIAL;
  AtomicOperation PROCESS_END = PvmAtomicOperation.PROCESS_END;

  AtomicOperation ACTIVITY_START = PvmAtomicOperation.ACTIVITY_START;
  AtomicOperation ACTIVITY_START_CONCURRENT = PvmAtomicOperation.ACTIVITY_START_CONCURRENT;
  AtomicOperation ACTIVITY_START_CANCEL_SCOPE = PvmAtomicOperation.ACTIVITY_START_CANCEL_SCOPE;
  AtomicOperation ACTIVITY_EXECUTE = PvmAtomicOperation.ACTIVITY_EXECUTE;
  AtomicOperation ACTIVITY_END = PvmAtomicOperation.ACTIVITY_END;
  AtomicOperation FIRE_ACTIVITY_END = PvmAtomicOperation.FIRE_ACTIVITY_END;

  AtomicOperation TRANSITION_NOTIFY_LISTENER_END = PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END;
  AtomicOperation TRANSITION_DESTROY_SCOPE = PvmAtomicOperation.TRANSITION_DESTROY_SCOPE;
  AtomicOperation TRANSITION_NOTIFY_LISTENER_TAKE = PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE;
  AtomicOperation TRANSITION_CREATE_SCOPE = PvmAtomicOperation.TRANSITION_CREATE_SCOPE;
  AtomicOperation TRANSITION_NOTIFY_LISTENER_START = PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_START;

  AtomicOperation DELETE_CASCADE = PvmAtomicOperation.DELETE_CASCADE;
  AtomicOperation DELETE_CASCADE_FIRE_ACTIVITY_END = PvmAtomicOperation.DELETE_CASCADE_FIRE_ACTIVITY_END;

  boolean isAsyncCapable();
}

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
package org.camunda.bpm.engine.impl.pvm.runtime;



/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public interface AtomicOperation {

  AtomicOperation PROCESS_START = new AtomicOperationProcessStart();
  AtomicOperation PROCESS_START_INITIAL = new AtomicOperationProcessStartInitial();
  AtomicOperation PROCESS_END = new AtomicOperationProcessEnd();

  AtomicOperation ACTIVITY_START = new AtomicOperationActivityStart();
  AtomicOperation ACTIVITY_START_CONCURRENT = new AtomicOperationActivityStartConcurrent();
  AtomicOperation ACTIVITY_START_CANCEL_SCOPE = new AtomicOperationActivityStartCancelScope();
  AtomicOperation ACTIVITY_EXECUTE = new AtomicOperationActivityExecute();
  AtomicOperation ACTIVITY_END = new AtomicOperationActivityEnd();
  AtomicOperation FIRE_ACTIVITY_END = new AtomicOperationFireActivityEnd();

  AtomicOperation TRANSITION_NOTIFY_LISTENER_END = new AtomicOperationTransitionNotifyListenerEnd();
  AtomicOperation TRANSITION_DESTROY_SCOPE = new AtomicOperationTransitionDestroyScope();
  AtomicOperation TRANSITION_NOTIFY_LISTENER_TAKE = new AtomicOperationTransitionNotifyListenerTake();
  AtomicOperation TRANSITION_CREATE_SCOPE = new AtomicOperationTransitionCreateScope();
  AtomicOperation TRANSITION_CANCEL_SCOPE = new AtomicOperationTransitionCancelScope();
  AtomicOperation TRANSITION_NOTIFY_LISTENER_START = new AtomicOperationTransitionNotifyListenerStart();

  AtomicOperation DELETE_CASCADE = new AtomicOperationDeleteCascade();
  AtomicOperation DELETE_CASCADE_FIRE_ACTIVITY_END = new AtomicOperationDeleteCascadeFireActivityEnd();

  void execute(InterpretableExecution execution);

  boolean isAsync(InterpretableExecution execution);

  String getCanonicalName();
}

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
package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;

/**
 * <p>A fluent builder to specify a modification of process instance state in terms
 * of cancellation of activity instances and instantiations of activities and sequence flows.
 * Allows to specify an ordered set of instructions that are all executed within one
 * transaction. Individual instructions are executed in the order of their specification.</p>
 *
 * @author Thorben Lindhauer
 */
public interface ProcessInstanceModificationBuilder extends
  InstantiationBuilder<ProcessInstanceModificationInstantiationBuilder> {

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start before the specified activity. Instantiate the given activity
   * as a descendant of the given ancestor activity instance.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>Instantiate all activities between the ancestor activity and the activity to execute</li>
   *     <li>Instantiate and execute the given activity (respects the asyncBefore
   *       attribute of the activity)</li>
   *   </ul>
   * </p>
   *
   * @param activityId the activity to instantiate
   * @param ancestorActivityInstanceId the ID of an existing activity instance under which the new
   *   activity instance should be created
   */
  ProcessInstanceModificationInstantiationBuilder startBeforeActivity(String activityId, String ancestorActivityInstanceId);

  /**
   * Submits an instruction that behaves like {@link #startTransition(String,String)} and always instantiates
   * the single outgoing sequence flow of the given activity. Does not consider asyncAfter.
   *
   * @param activityId the activity for which the outgoing flow should be executed
   * @throws ProcessEngineException if the activity has 0 or more than 1 outgoing sequence flows
   */
  ProcessInstanceModificationInstantiationBuilder startAfterActivity(String activityId, String ancestorActivityInstanceId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start the specified sequence flow. Instantiate the given sequence flow
   * as a descendant of the given ancestor activity instance.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>Instantiate all activities between the ancestor activity and the activity to execute</li>
   *     <li>Execute the given transition (does not consider sequence flow conditions)</li>
   *   </ul>
   * </p>
   *
   * @param transitionId the sequence flow to execute
   * @param ancestorActivityInstanceId the ID of an existing activity instance under which the new
   *   transition should be executed
   */
  ProcessInstanceModificationInstantiationBuilder startTransition(String transitionId, String ancestorActivityInstanceId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel an activity instance in a process. If this instance has child activity instances
   * (e.g. in a subprocess instance), these children, their grandchildren, etc. are cancelled as well.</p>
   *
   * <p>Process instance cancellation will propagate upward, removing any parent process instances that are
   * only waiting on the cancelled process to complete.</p>
   *
   * @param activityInstanceId the id of the activity instance to cancel
   */
  ProcessInstanceModificationBuilder cancelActivityInstance(String activityInstanceId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel a transition instance (i.e. an async continuation) in a process.</p>
   *
   * @param transitionInstanceId the id of the transition instance to cancel
   */
  ProcessInstanceModificationBuilder cancelTransitionInstance(String transitionInstanceId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel all instances of the given activity in an arbitrary order, which are:
   * <ul>
   *   <li>activity instances of that activity
   *   <li>transition instances entering or leaving that activity
   * </ul></p>
   *
   * <p>Therefore behaves like {@link #cancelActivityInstance(String)} for each individual
   * activity instance and like {@link #cancelTransitionInstance(String)} for each
   * individual transition instance.</p>
   *
   * <p>The cancellation order of the instances is arbitrary</p>
   *
   * @param activityId the activity for which all instances should be cancelled
   */
  ProcessInstanceModificationBuilder cancelAllForActivity(String activityId);

  /**
   * Execute all instructions. Custom execution and task listeners, as well as task input output mappings
   * are executed.
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *          if the process instance will be delete and the user has no {@link Permissions#DELETE} permission
   *          on {@link Resources#PROCESS_INSTANCE} or no {@link Permissions#DELETE_INSTANCE} permission on
   *          {@link Resources#PROCESS_DEFINITION}.
   */
  void execute();

  /**
   * @param skipCustomListeners specifies whether custom listeners (task and execution)
   *   should be invoked when executing the instructions
   * @param skipIoMappings specifies whether input/output mappings for tasks should be invoked
   *   throughout the transaction when executing the instructions
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *          if the process instance will be delete and the user has no {@link Permissions#DELETE} permission
   *          on {@link Resources#PROCESS_INSTANCE} or no {@link Permissions#DELETE_INSTANCE} permission on
   *          {@link Resources#PROCESS_DEFINITION}.
   */
  void execute(boolean skipCustomListeners, boolean skipIoMappings);

  /**
   * Execute all instructions asynchronously. Custom execution and task listeners, as well as task input output mappings
   * are executed.
   *
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   *
   * @return a batch job to be executed by the executor
   */
  Batch executeAsync();

  /**
   * @param skipCustomListeners specifies whether custom listeners (task and execution)
   *   should be invoked when executing the instructions
   * @param skipIoMappings specifies whether input/output mappings for tasks should be invoked
   *   throughout the transaction when executing the instructions
   *
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   *
   * @return a batch job to be executed by the executor
   */
  Batch executeAsync(boolean skipCustomListeners, boolean skipIoMappings);

}

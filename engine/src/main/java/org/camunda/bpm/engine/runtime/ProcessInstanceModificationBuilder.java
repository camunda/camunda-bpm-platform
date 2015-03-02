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

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>A fluent builder to specify a modification of process instance state in terms
 * of cancellation of activity instances and instantiations of activities and sequence flows.
 * Allows to specify an ordered set of instructions that are all executed within one
 * transaction. Individual instructions are executed in the order of their specification.</p>
 *
 * @author Thorben Lindhauer
 */
public interface ProcessInstanceModificationBuilder {

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel an activity instance in a process. If this instance has child activity instances
   * (e.g. in a subprocess instance), these children, their grandchildren, etc. are cancelled as well.</p>
   *
   * @param activityInstanceId the id of the activity instance to cancel
   */
  ProcessInstanceModificationBuilder cancelActivityInstance(String activityInstanceId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * Cancel all instances of the given activity in an arbitrary order. Behaves like
   * {@link #cancelActivityInstance(String)} for each individual
   * instance.
   *
   * @param activityId the activity for which all instances should be cancelled
   */
  ProcessInstanceModificationBuilder cancelAllInActivity(String activityId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start before the specified activity.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>In the parent activity hierarchy, determine the closest existing ancestor activity instance</li>
   *     <li>Instantiate all parent activities up to the ancestor's activity</li>
   *     <li>Instantiate and execute the given activity (respects the asyncBefore
   *       attribute of the activity)</li>
   *   </ul>
   * </p>
   *
   * @param activityId the activity to instantiate
   * @throws ProcessEngineException if more than one possible ancestor activity instance exists
   */
  ProcessInstanceActivityInstantiationBuilder startBeforeActivity(String activityId);

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
  ProcessInstanceActivityInstantiationBuilder startBeforeActivity(String activityId, String ancestorActivityInstanceId);

  /**
   * Submits an instruction that behaves like {@link #startTransition(String)} and always instantiates
   * the single outgoing sequence flow of the given activity. Does not consider asyncAfter.
   *
   * @param activityId the activity for which the outgoing flow should be executed
   * @throws ProcessEngineException if the activity has 0 or more than 1 outgoing sequence flows
   */
  ProcessInstanceActivityInstantiationBuilder startAfterActivity(String activityId);

  /**
   * Submits an instruction that behaves like {@link #startTransition(String,String)} and always instantiates
   * the single outgoing sequence flow of the given activity. Does not consider asyncAfter.
   *
   * @param activityId the activity for which the outgoing flow should be executed
   * @throws ProcessEngineException if the activity has 0 or more than 1 outgoing sequence flows
   */
  ProcessInstanceActivityInstantiationBuilder startAfterActivity(String activityId, String ancestorActivityInstanceId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start a sequence flow.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>In the parent activity hierarchy, determine the closest existing ancestor activity instance</li>
   *     <li>Instantiate all parent activities up to the ancestor's activity</li>
   *     <li>Execute the given transition (does not consider sequence flow conditions)</li>
   *   </ul>
   * </p>
   *
   * @param transitionId the sequence flow to execute
   * @throws ProcessEngineException if more than one possible ancestor activity instance exists
   */
  ProcessInstanceActivityInstantiationBuilder startTransition(String transitionId);

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
  ProcessInstanceActivityInstantiationBuilder startTransition(String transitionId, String ancestorActivityInstanceId);

  /**
   * Execute all instructions. Custom execution and task listeners, as well as task input output mappings
   * are executed.
   */
  void execute();

  /**
   * @param skipCustomListeners specifies whether custom listeners (task and execution)
   *   should be invoked when executing the instructions
   * @param skipIoMappings specifies whether input/output mappings for tasks should be invoked
   *   throughout the transaction when executing the instructions
   */
  void execute(boolean skipCustomListeners, boolean skipIoMappings);



}

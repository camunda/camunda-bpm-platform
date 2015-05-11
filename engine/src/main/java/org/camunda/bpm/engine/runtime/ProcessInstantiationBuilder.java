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
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;


/**
 * @author Thorben Lindhauer
 *
 */
public interface ProcessInstantiationBuilder extends
  ActivityInstantiationBuilder<ProcessInstantiationBuilder>,
  InstantiationBuilder<ProcessInstantiationBuilder>
{
  /**
   * Set the business key for the process instance
   */
  ProcessInstantiationBuilder businessKey(String businessKey);

  /**
   * Associate a case instance with the process instance
   */
  ProcessInstantiationBuilder caseInstanceId(String caseInstanceId);

  /**
   * Start the process instance by executing all instructions in the order they are submitted.
   * Custom execution and task listeners, as well as task input output mappings are executed.
   *
   * @return the newly created process instance
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance execute();

  /**
   * Start the process instance by executing all instructions in the order they are submitted.
   *
   * @param skipCustomListeners specifies whether custom listeners (task and execution)
   *   should be invoked when executing the instructions
   * @param skipIoMappings specifies whether input/output mappings for tasks should be invoked
   *   throughout the transaction when executing the instructions
   * @return the newly created process instance
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}..
   */
  ProcessInstance execute(boolean skipCustomListeners, boolean skipIoMappings);
}

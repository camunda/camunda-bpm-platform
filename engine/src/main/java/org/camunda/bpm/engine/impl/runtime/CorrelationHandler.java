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

package org.camunda.bpm.engine.impl.runtime;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * @author Thorben Lindhauer
 */
public interface CorrelationHandler {

  /**
   * Correlate the given message and return the execution that matches it. 
   * Returns null, if no such execution exists.
   * 
   * @param commandContext
   * @param messageName
   * @param correlationSet any of its members may be <code>null</code>
   * @return
   */
  Execution correlateMessageToExecution(CommandContext commandContext, String messageName,
      CorrelationSet correlationSet);
  
  /**
   * Correlate the given message and return the process definition that has a matching start event. 
   * Returns null, if no such definition exists.
   * 
   * @param commandContext
   * @param messageName
   * @return
   */
  ProcessDefinition correlateMessageToProcessDefinition(CommandContext commandContext, String messageName);
}

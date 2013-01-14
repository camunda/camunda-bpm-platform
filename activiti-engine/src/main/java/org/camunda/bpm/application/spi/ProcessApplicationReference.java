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
package org.camunda.bpm.application.spi;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;

/**
 * A reference to a process application.
 * 
 * @author Daniel Meyer
 * 
 */
public interface ProcessApplicationReference {

  /**
   * 
   * @return the name of the process application
   */
  public String getName();

  /**
   * Get the process application.
   * 
   * @return the {@link ProcessApplication}
   * @throws ProcessApplicationUnavailableException
   *           if the process application is unavailable
   */
  public ProcessApplication getProcessApplication() throws ProcessApplicationUnavailableException;

  /**
   * Called by the process engine when the process engine is stopped and
   * releases the reference to the process application before the process
   * application is stopped.
   * 
   * @throws ProcessApplicationUnavailableException
   *           if the process application is unavailable
   */
  public void processEngineStopping(ProcessEngine processEngine) throws ProcessApplicationUnavailableException;

}

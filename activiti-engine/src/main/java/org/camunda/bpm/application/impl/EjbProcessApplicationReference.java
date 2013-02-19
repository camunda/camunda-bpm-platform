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
package org.camunda.bpm.application.impl;

import javax.naming.InitialContext;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;

/**
 * <p>A reference to an EJB process application.</p>
 * 
 * <p>An EJB process application is an EJB Session Bean that can be looked up in JNDI.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class EjbProcessApplicationReference implements ProcessApplicationReference {
  
  private final String processApplicationName;
  
  private final String processApplicationSessionObjectName;
  
  public EjbProcessApplicationReference(String processApplicationName, String processApplicationSessionObjectName) {
    this.processApplicationName = processApplicationName;
    this.processApplicationSessionObjectName = processApplicationSessionObjectName;
  }

  public String getName() {
    return processApplicationName;
  }

  public ProcessApplication getProcessApplication() throws ProcessApplicationUnavailableException {
    try {
      return InitialContext.doLookup(processApplicationSessionObjectName);
    } catch (Exception e) {
      throw new ProcessApplicationUnavailableException("Could not lookup process application using JNDI name '"+processApplicationSessionObjectName+"'", e);
    }
  }

  public void processEngineStopping(ProcessEngine processEngine) throws ProcessApplicationUnavailableException {
    // do nothing.   
  }

}

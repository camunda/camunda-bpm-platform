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

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngine;

/**
 * <p>A reference to an EJB process application.</p>
 * 
 * <p>An EJB process application is an EJB Session Bean that can be looked up in JNDI.</p>
 * 
 * @author Daniel Meyer
 * @author Andreas Drobisch
 *
 */
public class EjbProcessApplicationReference implements ProcessApplicationReference {

  public final EjbProcessApplication.NameLookup names;
  protected final String lookupName;
  protected final static String lookupScope = "java:global/";

  public EjbProcessApplicationReference() {
    names = null;
    lookupName = null;
  }

  public EjbProcessApplicationReference(EjbProcessApplication.NameLookup names) {
    this.names = names;
    this.lookupName = lookupScope + getSessionObjectName(names);
  }

  public String getName() {
    return names.processApplicationName;
  }

  public String getSessionObjectName (EjbProcessApplication.NameLookup names) {
    return names.processApplicationName + "/" + names.appClassName;
  }

  public ProcessApplicationInterface getProcessApplication() throws ProcessApplicationUnavailableException {
    try {
      // TODO: do lookup once and cache EJB proxy
      return InitialContext.doLookup(lookupName);
    } catch (Exception e) {
      throw new ProcessApplicationUnavailableException("Could not lookup process application using JNDI name '"+lookupName+"'", e);
    }
  }

  public void processEngineStopping(ProcessEngine processEngine) throws ProcessApplicationUnavailableException {
    // do nothing.
  }

}

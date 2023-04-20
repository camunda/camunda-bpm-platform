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
package org.camunda.bpm.application.impl;

import javax.ejb.EJBException;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * <p>A reference to an EJB process application.</p>
 *
 * <p>An EJB process application is an EJB Session Bean that can be looked up in JNDI.</p>
 *
 * @author Daniel Meyer
 *
 */
public class EjbProcessApplicationReference implements ProcessApplicationReference {

  private static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  /** this is an EjbProxy and can be cached */
  protected ProcessApplicationInterface selfReference;

  /** the name of the process application */
  protected String processApplicationName;

  public EjbProcessApplicationReference(ProcessApplicationInterface selfReference, String name) {
    this.selfReference = selfReference;
    this.processApplicationName = name;
  }

  @Override
  public String getName() {
    return processApplicationName;
  }

  @Override
  public ProcessApplicationInterface getProcessApplication() throws ProcessApplicationUnavailableException {
    try {
      // check whether process application is still deployed
      selfReference.getName();
    } catch (EJBException e) {
      throw LOG.processApplicationUnavailableException(processApplicationName, e);
    }
    return selfReference;
  }

  public void processEngineStopping(ProcessEngine processEngine) throws ProcessApplicationUnavailableException {
    // do nothing.
  }

}

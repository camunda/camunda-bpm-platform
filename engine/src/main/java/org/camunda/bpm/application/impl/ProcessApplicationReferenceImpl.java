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

import java.lang.ref.WeakReference;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * <p>A {@link ProcessApplicationReference} implementation using
 * {@link WeakReference}.</p>
 *
 * <p>As long as the process application is deployed, the container or the
 * application will hold a strong reference to the {@link AbstractProcessApplication}
 * object. This class holds a {@link WeakReference}. When the process
 * application is undeployed, the container or application releases all strong
 * references. Since we only pass {@link ProcessApplicationReference
 * ProcessApplicationReferences} to the process engine, it is guaranteed that
 * the {@link AbstractProcessApplication} object can be reclaimed by the garbage
 * collector, even if the undeployment and unregistration should fail for some
 * improbable reason.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationReferenceImpl implements ProcessApplicationReference {

  private static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  /** the weak reference to the process application */
  protected WeakReference<AbstractProcessApplication> processApplication;

  protected String name;

  public ProcessApplicationReferenceImpl(AbstractProcessApplication processApplication) {
    this.processApplication = new WeakReference<AbstractProcessApplication>(processApplication);
    this.name = processApplication.getName();
  }

  public String getName() {
    return name;
  }

  public AbstractProcessApplication getProcessApplication() throws ProcessApplicationUnavailableException {
    AbstractProcessApplication application = processApplication.get();
    if (application == null) {
      throw LOG.processApplicationUnavailableException(name);
    }
    else {
      return application;
    }
  }

  public void processEngineStopping(ProcessEngine processEngine) throws ProcessApplicationUnavailableException {
    // do nothing
  }

  public void clear() {
    processApplication.clear();
  }

}

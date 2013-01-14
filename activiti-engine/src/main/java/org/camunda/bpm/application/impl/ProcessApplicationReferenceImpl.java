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

import java.lang.ref.WeakReference;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.application.spi.ProcessApplication;
import org.camunda.bpm.application.spi.ProcessApplicationReference;

/**
 * <p>A {@link ProcessApplicationReference} implementation using
 * {@link WeakReference}.</p>
 * 
 * <p>As long as the process application is deployed, the container or the
 * application will hold a strong reference to the {@link ProcessApplication}
 * object. This class holds a {@link WeakReference}. When the process
 * application is undeployed, the container or application releases all strong
 * references. Since we only pass {@link ProcessApplicationReference
 * ProcessApplicationReferences} to the process engine, it is guaranteed that
 * the {@link ProcessApplication} object can be reclaimed by the garbage
 * collector, even if the undeployment and unregistration should fail for some
 * improbable reason.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessApplicationReferenceImpl implements ProcessApplicationReference {

  /** the weak reference to the process application */
  protected WeakReference<ProcessApplication> processApplication;

  private String name;

  public ProcessApplicationReferenceImpl(ProcessApplication processApplication) {
    this.processApplication = new WeakReference<ProcessApplication>(processApplication);
    this.name = processApplication.getName();
  }

  public String getName() {
    return name;
  }

  public ProcessApplication getProcessApplication() throws ProcessApplicationUnavailableException {
    ProcessApplication application = processApplication.get();
    if (application == null) {
      throw new ProcessApplicationUnavailableException();
    } else {
      return application;
    }
  }

  public void processEngineStopping(ProcessEngine processEngine) throws ProcessApplicationUnavailableException {
    ProcessApplication processApplication = getProcessApplication();
    processApplication.processEngineStopping(processEngine);
  }

  public void clear() {
    processApplication.clear();
  }

}

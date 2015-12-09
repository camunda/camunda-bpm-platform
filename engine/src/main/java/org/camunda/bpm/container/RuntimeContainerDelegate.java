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
package org.camunda.bpm.container;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.engine.ProcessEngine;

/**
 * <p>The {@link RuntimeContainerDelegate} in an SPI that allows the process engine to integrate with the
 * runtime container in which it is deployed. Examples of "runtime containers" are
 * <ul>
 *  <li>JBoss AS 7 (Module Service Container),</li>
 *  <li>The JMX Container,</li>
 *  <li>An OSGi Runtime,</li>
 *  <li>...</li>
 * </ul>
 *
 * <p>The current {@link RuntimeContainerDelegate} can be obtained through the static {@link #INSTANCE} field.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface RuntimeContainerDelegate {

  /** Holds the current {@link RuntimeContainerDelegate} instance */
  public static RuntimeContainerDelegateInstance INSTANCE = new RuntimeContainerDelegateInstance();

  /**
   * <p>Adds a managed {@link ProcessEngine} to the runtime container.</p>
   * <p>Process Engines registered through this method are returned by the {@link ProcessEngineService}.</p>
   */
  public void registerProcessEngine(ProcessEngine processEngine);

  /**
   * <p>Unregisters a managed {@link ProcessEngine} instance from the Runtime Container.</p>
   */
  public void unregisterProcessEngine(ProcessEngine processEngine);

  /**
   * Deploy a {@link AbstractProcessApplication} into the runtime container.
   *
   */
  public void deployProcessApplication(AbstractProcessApplication processApplication);

  /**
   * Undeploy a {@link AbstractProcessApplication} from the runtime container.
   *
   */
  public void undeployProcessApplication(AbstractProcessApplication processApplication);

  /**
   * @return the Container's {@link ProcessEngineService} implementation.
   */
  public ProcessEngineService getProcessEngineService();

  /**
   * @return the Container's {@link ProcessApplicationService} implementation
   */
  public ProcessApplicationService getProcessApplicationService();

  /**
   * @return the Runtime Container's {@link ExecutorService} implementation
   */
  public ExecutorService getExecutorService();

  /**
   * @return a reference to the process application with the given name if deployed; null otherwise
   */
  public ProcessApplicationReference getDeployedProcessApplication(String name);

  /**
   * Holder of the current {@link RuntimeContainerDelegate} instance.
   */
  public static class RuntimeContainerDelegateInstance {

    // hide
    private RuntimeContainerDelegateInstance() {}

    private RuntimeContainerDelegate delegate = new RuntimeContainerDelegateImpl();

    public RuntimeContainerDelegate get() {
      return delegate;
    }

    public void set(RuntimeContainerDelegate delegate) {
      this.delegate = delegate;
    }

  }

}

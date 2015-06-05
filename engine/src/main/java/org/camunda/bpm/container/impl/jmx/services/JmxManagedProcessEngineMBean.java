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
package org.camunda.bpm.container.impl.jmx.services;

import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * An MBean interface for the {@link ProcessEngine}.
 *
 * @author Daniel Meyer
 *
 */
public interface JmxManagedProcessEngineMBean {

  /**
   * @return the name of the {@link ProcessEngine}
   */
  public String getName();

  /**
   * If the engine's job executor is deloyment aware, these are the deployments it
   * acquires jobs for.
   *
   * @return all deployments that are registered with this {@link ProcessEngine}
   */
  public Set<String> getRegisteredDeployments();

  public void registerDeployment(String deploymentId);

  public void unregisterDeployment(String deploymentId);

  public void reportDbMetrics();
}

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
package org.camunda.bpm.engine.impl.cfg.multitenancy;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * SPI which can be implemented to assign tenant ids to process instances, case instances and historic decision instances.
 *<p>
 * The SPI is invoked if the process definition, case definition or decision definition does not have a tenant id or
 * execution does not have a tenant id.
 *<p>
 * An implementation of this SPI can be set on the {@link ProcessEngineConfigurationImpl}.
 *
 * @author Daniel Meyer
 * @since 7.5
 */
public interface TenantIdProvider {

  /**
   * Invoked when a process instance is started and the Process Definition does not have a tenant id.
   *<p>
   * Implementors can either return a tenant id or null. If null is returned the process instance is not assigned a tenant id.
   *
   * @param ctx holds information about the process instance which is about to be started.
   * @return a tenant id or null if case the implementation does not assign a tenant id to the process instance
   */
  String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx);

  /**
   * Invoked when a case instance is started and the Case Definition does not have a tenant id.
   *<p>
   * Implementors can either return a tenant id or null. If null is returned the case instance is not assigned a tenant id.
   *
   * @param ctx holds information about the case instance which is about to be started.
   * @return a tenant id or null if case the implementation does not assign a tenant id to case process instance
   */
  String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx);

  /**
   * Invoked when a historic decision instance is created and the Decision Definition or the Execution does not have a tenant id.
   *<p>
   * Implementors can either return a tenant id or null. If null is returned the historic decision instance is not assigned a tenant id.
   *
   * @param ctx holds information about the decision definition and the execution.
   * @return a tenant id or null if case the implementation does not assign a tenant id to the historic decision instance
   */
  String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx);

}

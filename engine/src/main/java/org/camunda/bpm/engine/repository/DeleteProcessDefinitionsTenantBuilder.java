/*
 * Copyright 2017 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.repository;

/**
 * Fluent builder to delete process definitions by a process definition key or process definition ids.
 *
 * @author Tassilo Weidner
 */
public interface DeleteProcessDefinitionsTenantBuilder extends DeleteProcessDefinitionsBuilder {

  /**
   * Process definitions which belong to no tenant will be removed.
   *
   * @return the builder
   */
  DeleteProcessDefinitionsBuilder withoutTenantId();

  /**
   * Process definitions which belong to the given tenant id will be removed.
   *
   * @param tenantId id which identifies the tenant
   * @return the builder
   */
  DeleteProcessDefinitionsBuilder withTenantId(String tenantId);

}

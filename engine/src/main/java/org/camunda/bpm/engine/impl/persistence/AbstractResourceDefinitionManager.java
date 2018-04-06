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
package org.camunda.bpm.engine.impl.persistence;

/**
 * @author: Johannes Heinemann
 */
public interface AbstractResourceDefinitionManager<T> {

  T findLatestDefinitionByKey(String key);

  T findLatestDefinitionById(String id);

  T findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId);

  T findDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId);

  T findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey);

  T getCachedResourceDefinitionEntity(String definitionId);

  T findDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId);

}

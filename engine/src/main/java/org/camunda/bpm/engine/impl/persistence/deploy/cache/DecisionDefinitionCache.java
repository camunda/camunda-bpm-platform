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
package org.camunda.bpm.engine.impl.persistence.deploy.cache;

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionManager;

/**
 * @author: Johannes Heinemann
 */
public class DecisionDefinitionCache extends ResourceDefinitionCache<DecisionDefinitionEntity> {

  DecisionCacheErrorChecker errorChecker;

  public DecisionDefinitionCache(CacheFactory factory, int cacheCapacity, CacheDeployer cacheDeployer) {
    super(factory, cacheCapacity, cacheDeployer);
  }

  @Override
  protected CacheErrorChecker<DecisionDefinitionEntity> createErrorChecker() {
    errorChecker = new DecisionCacheErrorChecker();
    return errorChecker;
  }

  @Override
  protected DefinitionManagerFactory<DecisionDefinitionEntity> createDefinitionManagerFactory() {
    return new DecisionDefinitionManagerFactory();
  }

  public DecisionDefinitionEntity findDeployedDefinitionByKeyAndVersion(String definitionKey, Integer definitionVersion) {
    DecisionDefinitionEntity definition = ((DecisionDefinitionManager) managerFactory.getManager())
        .findDecisionDefinitionByKeyAndVersion(definitionKey, definitionVersion);

    errorChecker.checkInvalidDefinitionByKeyAndVersion(definitionKey, definitionVersion, definition);
    definition = resolveDefinition(definition);
    return definition;
  }

}

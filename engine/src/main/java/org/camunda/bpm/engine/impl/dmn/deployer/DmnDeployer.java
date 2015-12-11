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
package org.camunda.bpm.engine.impl.dmn.deployer;

import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformer;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.AbstractDefinitionDeployer;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * {@link Deployer} responsible to parse DMN 1.0 XML files and create the
 * proper {@link DecisionDefinitionEntity}s.
 */
public class DmnDeployer extends AbstractDefinitionDeployer<DecisionDefinitionEntity> {

  public static final String[] DMN_RESOURCE_SUFFIXES = new String[] { "dmn11.xml", "dmn" };

  protected DmnTransformer transformer;

  protected String[] getResourcesSuffixes() {
    return DMN_RESOURCE_SUFFIXES;
  }

  protected List<DecisionDefinitionEntity> transformDefinitions(DeploymentEntity deployment, ResourceEntity resource, Properties properties) {
    byte[] bytes = resource.getBytes();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

    try {
      return transformer.createTransform().modelInstance(inputStream).transformDecisions();
    }
    catch (Exception e) {
      throw new ProcessEngineException("Unable to transform DMN resource '" + resource.getName() + "'", e);
    }
  }

  protected DecisionDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return getDecisionDefinitionManager().findDecisionDefinitionByDeploymentAndKey(deploymentId, definitionKey);
  }

  protected DecisionDefinitionEntity findLatestDefinitionByKey(String definitionKey) {
    return getDecisionDefinitionManager().findLatestDecisionDefinitionByKey(definitionKey);
  }

  protected void persistDefinition(DecisionDefinitionEntity definition) {
    getDecisionDefinitionManager().insertDecisionDefinition(definition);
  }

  protected void addDefinitionToDeploymentCache(DeploymentCache deploymentCache, DecisionDefinitionEntity definition) {
    deploymentCache.addDecisionDefinition(definition);
  }

  // context ///////////////////////////////////////////////////////////////////////////////////////////

  protected DecisionDefinitionManager getDecisionDefinitionManager() {
    return getCommandContext().getDecisionDefinitionManager();
  }

  // getters/setters ///////////////////////////////////////////////////////////////////////////////////

  public DmnTransformer getTransformer() {
    return transformer;
  }

  public void setTransformer(DmnTransformer transformer) {
    this.transformer = transformer;
  }

}

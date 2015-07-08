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
package org.camunda.bpm.engine.impl.cmmn.deployer;

import org.camunda.bpm.engine.impl.AbstractDefinitionDeployer;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionManager;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformer;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;

import java.util.List;

/**
 * {@link Deployer} responsible to parse CMMN 1.0 XML files and create the
 * proper {@link CaseDefinitionEntity}s.
 *
 * @author Roman Smirnov
 * @author Simon Zambrovski
 *
 */
public class CmmnDeployer extends AbstractDefinitionDeployer<CaseDefinitionEntity> {

  public static final String[] CMMN_RESOURCE_SUFFIXES = new String[] { "cmmn10.xml", "cmmn" };

  protected ExpressionManager expressionManager;
  protected CmmnTransformer transformer;

  protected String[] getResourcesSuffixes() {
    return CMMN_RESOURCE_SUFFIXES;
  }

  protected List<CaseDefinitionEntity> transformDefinitions(DeploymentEntity deployment, ResourceEntity resource) {
    return transformer.createTransform().deployment(deployment).resource(resource).transform();
  }

  protected CaseDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return getCaseDefinitionManager().findCaseDefinitionByDeploymentAndKey(deploymentId, definitionKey);
  }

  protected CaseDefinitionEntity findLatestDefinitionByKey(String definitionKey) {
    return getCaseDefinitionManager().findLatestCaseDefinitionByKey(definitionKey);
  }

  protected void persistDefinition(CaseDefinitionEntity definition) {
    getCaseDefinitionManager().insertCaseDefinition(definition);
  }

  protected void addDefinitionToDeploymentCache(DeploymentCache deploymentCache, CaseDefinitionEntity definition) {
    deploymentCache.addCaseDefinition(definition);
  }

  // context ///////////////////////////////////////////////////////////////////////////////////////////

  protected CaseDefinitionManager getCaseDefinitionManager() {
    return getCommandContext().getCaseDefinitionManager();
  }

  // getters/setters ///////////////////////////////////////////////////////////////////////////////////

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public CmmnTransformer getTransformer() {
    return transformer;
  }

  public void setTransformer(CmmnTransformer transformer) {
    this.transformer = transformer;
  }

}

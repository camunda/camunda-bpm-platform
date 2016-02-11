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

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.impl.AbstractDefinitionDeployer;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionManager;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransform;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformer;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.core.model.PropertyMapKey;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.management.JobDefinition;

/**
 * {@link Deployer} responsible to parse CMMN 1.0 XML files and create the
 * proper {@link CaseDefinitionEntity}s.
 *
 * @author Roman Smirnov
 * @author Simon Zambrovski
 *
 */
public class CmmnDeployer extends AbstractDefinitionDeployer<CaseDefinitionEntity> {

  public static final String[] CMMN_RESOURCE_SUFFIXES = new String[] { "cmmn11.xml", "cmmn10.xml", "cmmn" };

  protected static final PropertyMapKey<String, List<JobDeclaration<?, ?>>> JOB_DECLARATIONS_PROPERTY =
          new PropertyMapKey<String, List<JobDeclaration<?, ?>>>("JOB_DECLARATIONS_PROPERTY");

  protected ExpressionManager expressionManager;
  protected CmmnTransformer transformer;

  @Override
  protected String[] getResourcesSuffixes() {
    return CMMN_RESOURCE_SUFFIXES;
  }

  @Override
  protected List<CaseDefinitionEntity> transformDefinitions(DeploymentEntity deployment, ResourceEntity resource, Properties properties) {
    CmmnTransform cmmnTransform = transformer.createTransform();

    List<CaseDefinitionEntity> definitions = cmmnTransform.deployment(deployment).resource(resource).transform();

    if(!properties.contains(JOB_DECLARATIONS_PROPERTY)){
      properties.set(JOB_DECLARATIONS_PROPERTY, new HashMap<String, List<JobDeclaration<?, ?>>>());
    }
    properties.get(JOB_DECLARATIONS_PROPERTY).putAll(cmmnTransform.getJobDeclarations());

    return definitions;
  }

  @Override
  protected CaseDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return getCaseDefinitionManager().findCaseDefinitionByDeploymentAndKey(deploymentId, definitionKey);
  }

  @Override
  protected CaseDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    return getCaseDefinitionManager().findLatestCaseDefinitionByKeyAndTenantId(definitionKey, tenantId);
  }

  @Override
  protected void persistDefinition(CaseDefinitionEntity definition) {
    getCaseDefinitionManager().insertCaseDefinition(definition);
  }

  @Override
  protected void addDefinitionToDeploymentCache(DeploymentCache deploymentCache, CaseDefinitionEntity definition) {
    deploymentCache.addCaseDefinition(definition);
  }

  protected JobDefinitionManager getJobDefinitionManager() {
    return getCommandContext().getJobDefinitionManager();
  }

  @Override
  protected void definitionAddedToDeploymentCache(DeploymentEntity deployment, CaseDefinitionEntity definition, Properties properties) {
    List<JobDeclaration<?, ?>> declarations = properties.get(JOB_DECLARATIONS_PROPERTY).get(definition.getKey());
    if(declarations != null && !declarations.isEmpty()) {
      updateJobDeclarations(declarations, definition, deployment.isNew());
    }
  }

  protected void updateJobDeclarations(List<JobDeclaration<?, ?>> jobDeclarations, CaseDefinitionEntity definition, boolean isNewDeployment) {
    if(jobDeclarations == null || jobDeclarations.isEmpty()) {
      return;
    }

   final JobDefinitionManager jobDefinitionManager = getJobDefinitionManager();

    if(isNewDeployment) {
      for(JobDeclaration<?,?> jobDeclaration:jobDeclarations){
        createJobDefinition(definition, jobDeclaration);
      }
    } else {
      List<JobDefinitionEntity> existingDefinitions = jobDefinitionManager.findByCaseDefinitionId(definition.getId());
      for (JobDeclaration<?, ?> jobDeclaration : jobDeclarations) {
        boolean jobDefinitionExists = false;
        for (JobDefinition jobDefinitionEntity : existingDefinitions) {
          // <!> Assumption: there can be only one job definition per activity and type
          if(jobDeclaration.getActivityId().equals(jobDefinitionEntity.getActivityId()) &&
                  jobDeclaration.getJobHandlerType().equals(jobDefinitionEntity.getJobType())) {
            jobDeclaration.setJobDefinitionId(jobDefinitionEntity.getId());
            jobDefinitionExists = true;
            break;
          }
        }
        if(!jobDefinitionExists) {
          // not found: create new definition
          createJobDefinition(definition, jobDeclaration);
        }

      }

    }
  }

  protected void createJobDefinition(CaseDefinitionEntity definition, JobDeclaration<?,?> jobDeclaration) {
    JobDefinitionEntity jobDefinitionEntity = new JobDefinitionEntity(jobDeclaration);

    jobDefinitionEntity.setCaseDefinitionId(definition.getId());
    jobDefinitionEntity.setCaseDefinitionKey(definition.getKey());
    jobDefinitionEntity.setTenantId(definition.getTenantId());
    jobDefinitionEntity.setActivityId(jobDeclaration.getActivityId());

    JobDefinitionManager jobDefinitionManager = getJobDefinitionManager();
    jobDefinitionManager.insert(jobDefinitionEntity);

    jobDeclaration.setJobDefinitionId(jobDefinitionEntity.getId());
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

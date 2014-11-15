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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionManager;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformer;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;

/**
 * {@link Deployer} responsible to parse CMMN 1.0 XML files and create the
 * proper {@link CaseDefinitionEntity}s.
 *
 * @author Roman Smirnov
 * @author Simon Zambrovski
 *
 */
public class CmmnDeployer implements Deployer {

  private static final Logger LOG = Logger.getLogger(CmmnDeployer.class.getName());

  public static final String[] CMMN_RESOURCE_SUFFIXES = new String[] { "cmmn10.xml", "cmmn" };
  public static final String[] DIAGRAM_SUFFIXES = new String[] { "png", "jpg", "gif", "svg" };

  protected ExpressionManager expressionManager;
  protected CmmnTransformer transformer;
  protected IdGenerator idGenerator;

  public void deploy(DeploymentEntity deployment) {
    List<CaseDefinitionEntity> caseDefinitions = new ArrayList<CaseDefinitionEntity>();
    Map<String, ResourceEntity> resources = deployment.getResources();

    // iterates the list of resources
    for (ResourceEntity resource : resources.values()) {

      // if the current resource is a cmmn resource
      // then start to transform the resource
      if (isCmmnResource(resource)) {
        List<CaseDefinitionEntity> result = transformResource(deployment, resource);
        caseDefinitions.addAll(result);
      }
    }

    // check if there are case definitions with the same
    // key to prevent database unique index violation
    List<String> keys = new ArrayList<String>();
    for (CaseDefinitionEntity currentCaseDefinition : caseDefinitions) {

      String key = currentCaseDefinition.getKey();

      if (keys.contains(key)) {
        throw new ProcessEngineException("The deployment contains case definitions with the same key '" + key + "' (case id attribute), this is not allowed");
      }

      keys.add(key);

    }

    if (!deployment.isNew()) {
      // it the current deployment is not a new one,
      // then load the already existing case definitions
      loadCaseDefinitions(deployment, caseDefinitions);
    } else {
      // otherwise persist the new case definitions
      persistCaseDefinitions(deployment, caseDefinitions);
    }
  }

  protected boolean isCmmnResource(ResourceEntity resource) {
    String resourceName = resource.getName();

    for (String suffix : CMMN_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

  protected List<CaseDefinitionEntity> transformResource(DeploymentEntity deployment, ResourceEntity resource) {

    List<CaseDefinitionEntity> caseDefinitions = transformer
      .createTransform()
      .deployment(deployment)
      .resource(resource)
      .transform();

    for (CaseDefinitionEntity caseDefinition: caseDefinitions) {
      String resourceName = resource.getName();
      caseDefinition.setResourceName(resourceName);

      String diagramResourceName = getDiagramResourceForCase(resourceName, caseDefinition.getKey(), deployment.getResources());
      if (diagramResourceName != null) {
        caseDefinition.setDiagramResourceName(diagramResourceName);
        LOG.fine("Setting diagram to " + diagramResourceName);
      }

    }

    return caseDefinitions;
  }

  /**
   * Returns the default name of the image resource for a certain case.
   *
   * It will first look for an image resource which matches the case
   * specifically, before resorting to an image resource which matches the CMMN
   * 1.0 xml file resource.
   *
   * Example: if the deployment contains a CMMN 1.0 xml resource called
   * 'abc.cmmn' containing only one case with key 'myProcess', then
   * this method will look for an image resources called 'abc.myProcess.png'
   * (or .jpg, or .gif, etc.) or 'abc.png' if the previous one wasn't found.
   *
   * Example 2: if the deployment contains a CMMN 1.0 xml resource called
   * 'abc.cmmn' containing three cases (with keys a, b and c),
   * then this method will first look for an image resource called 'abc.a.png'
   * before looking for 'abc.png' (likewise for b and c).
   * Note that if abc.a.png, abc.b.png and abc.c.png don't exist, all
   * casees will have the same image: abc.png.
   *
   * @return null if no matching image resource is found.
   */
  protected String getDiagramResourceForCase(String cmmnFileResource, String caseKey, Map<String, ResourceEntity> resources) {
    for (String diagramSuffix: DIAGRAM_SUFFIXES) {
      String diagramForBpmnFileResource = getCmmnFileImageResourceName(cmmnFileResource, diagramSuffix);
      String caseDiagramResource = getCaseImageResourceName(cmmnFileResource, caseKey, diagramSuffix);
      if (resources.containsKey(caseDiagramResource)) {
        return caseDiagramResource;
      } else if (resources.containsKey(diagramForBpmnFileResource)) {
        return diagramForBpmnFileResource;
      }
    }
    return null;
  }

  protected String getCaseImageResourceName(String cmmnFileResource, String caseKey, String diagramSuffix) {
    String cmmnFileResourceBase = stripCmmnFileSuffix(cmmnFileResource);
    return cmmnFileResourceBase + caseKey + "." + diagramSuffix;
  }

  protected String getCmmnFileImageResourceName(String cmmnFileResource, String diagramSuffix) {
    String cmmnFileResourceBase = stripCmmnFileSuffix(cmmnFileResource);
    return cmmnFileResourceBase + diagramSuffix;
  }

  protected int getNextVersion(CaseDefinitionEntity newCaseDefinition, CaseDefinitionEntity latestCaseDefinition) {
    int result = 1;
    if (latestCaseDefinition != null) {
      int latestVersion = latestCaseDefinition.getVersion();
      result = latestVersion + 1;
    }
    return result;
  }

  protected String generateCaseDefinitionId(CaseDefinitionEntity caseDefinition) {
    String nextId = idGenerator.getNextId();

    String caseDefinitionKey = caseDefinition.getKey();
    int caseDefinitionVersion = caseDefinition.getVersion();

    String caseDefinitionId = caseDefinitionKey
      + ":" + caseDefinitionVersion
      + ":" + nextId;

    if (caseDefinitionId.length() > 64) {
      caseDefinitionId = nextId;
    }
    return caseDefinitionId;
  }

  protected void loadCaseDefinitions(DeploymentEntity deployment, Collection<CaseDefinitionEntity> caseDefinitions) {
    CaseDefinitionManager caseDefinitionManager = Context
        .getCommandContext()
        .getCaseDefinitionManager();

    for (CaseDefinitionEntity caseDefinition : caseDefinitions) {

      String key = caseDefinition.getKey();

      String deploymentId = deployment.getId();
      caseDefinition.setDeploymentId(deploymentId);

      CaseDefinitionEntity persistedCaseDefinition = caseDefinitionManager.findCaseDefinitionByDeploymentAndKey(deploymentId, key);

      String caseDefinitionId = persistedCaseDefinition.getId();
      caseDefinition.setId(caseDefinitionId);

      int version = persistedCaseDefinition.getVersion();
      caseDefinition.setVersion(version);

      // Add to cache
      addCaseDefinitionToDeploymentCache(caseDefinition);

      // Add to deployment for further usage
      deployment.addDeployedArtifact(caseDefinition);
    }
  }

  protected void persistCaseDefinitions(DeploymentEntity deployment, Collection<CaseDefinitionEntity> caseDefinitions) {
    CaseDefinitionManager caseDefinitionManager = Context
        .getCommandContext()
        .getCaseDefinitionManager();

    for (CaseDefinitionEntity caseDefinition : caseDefinitions) {

      String deploymentId = deployment.getId();
      caseDefinition.setDeploymentId(deploymentId);

      String key = caseDefinition.getKey();
      CaseDefinitionEntity latestCaseDefinition = caseDefinitionManager.findLatestCaseDefinitionByKey(key);

      int version = getNextVersion(caseDefinition, latestCaseDefinition);
      caseDefinition.setVersion(version);

      String caseDefinitionId = generateCaseDefinitionId(caseDefinition);
      caseDefinition.setId(caseDefinitionId);

      caseDefinitionManager.insertCaseDefinition(caseDefinition);

      // Add to cache
      addCaseDefinitionToDeploymentCache(caseDefinition);

      // Add to deployment for further usage
      deployment.addDeployedArtifact(caseDefinition);
    }
  }

  protected void addCaseDefinitionToDeploymentCache(CaseDefinitionEntity caseDefinition) {
    Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .addCaseDefinition(caseDefinition);
  }

  // getters/setters /////////////////////////////////////////////////////////////

  protected String stripCmmnFileSuffix(final String cmmnFileResource) {
    for (final String suffix : CMMN_RESOURCE_SUFFIXES) {
      if (cmmnFileResource.endsWith(suffix)) {
        return cmmnFileResource.substring(0, cmmnFileResource.length() - suffix.length());
      }
    }
    return cmmnFileResource;
  }

  // getters/setters
  // /////////////////////////////////////////////////////////////

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public CmmnTransformer getTransformer() {
    return transformer;
  }

  public void setTransformer(CmmnTransformer transformer) {
    this.transformer = transformer;
  }

}

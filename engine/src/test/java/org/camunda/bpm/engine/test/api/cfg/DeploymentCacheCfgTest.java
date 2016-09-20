package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Johannes Heinemann
 */
public class DeploymentCacheCfgTest extends ResourceProcessEngineTestCase {


  public DeploymentCacheCfgTest() {
    super("org/camunda/bpm/engine/test/api/cfg/customized.cacheFactory.camunda.cfg.xml");
  }

  @Test
  @Deployment(resources =
      {"org/camunda/bpm/engine/test/api/cfg/MaxCacheSizeCfgTest.testDefaultCacheRemovesLRUElementWhenMaxSizeIsExceeded.bpmn20.xml"})
  public void testPlugInOwnCacheImplementation() {
    // The 'customized.cacheFactory.camunda.cfg.xml' sets a customized cache factory that uses the
    // default cache implementation, but limits the maximum number of elements within the cache to 2.
    // According to the least recently used principle of the default implementation should the first
    // process not be contained in the cache anymore.

    // given
    String processDefinitionIdOne = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("one")
        .singleResult()
        .getId();
    String processDefinitionIdTwo = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("two")
        .singleResult()
        .getId();
    String processDefinitionIdThree = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("three")
        .singleResult()
        .getId();


    // when
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // then
    assertNotNull(deploymentCache.getProcessDefinitionCache().get(processDefinitionIdTwo));
    assertNotNull(deploymentCache.getProcessDefinitionCache().get(processDefinitionIdThree));

    assertNull(deploymentCache.getProcessDefinitionCache().get(processDefinitionIdOne));
  }

}
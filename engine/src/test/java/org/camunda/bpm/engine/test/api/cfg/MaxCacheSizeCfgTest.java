package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Johannes Heinemann
 */
public class MaxCacheSizeCfgTest extends ResourceProcessEngineTestCase {

  public MaxCacheSizeCfgTest() {
    super("org/camunda/bpm/engine/test/api/cfg/cacheSize.reduced.camunda.cfg.xml");
  }

  @Test
  @Deployment
  public void testDefaultCacheRemovesLRUElementWhenMaxSizeIsExceeded() {
    // The 'cacheSize.reduced.camunda.cfg.xml' sets the maximum number of elements of the
    // to 2. Accordingly, one process should not be contained in the cache anymore.

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
    int numberOfProcessesInCache = 0;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdOne) == null ? 0 : 1;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdTwo) == null ? 0 : 1;
    numberOfProcessesInCache +=
        deploymentCache.getProcessDefinitionCache().get(processDefinitionIdThree) == null ? 0 : 1;

    assertTrue(numberOfProcessesInCache == 2);
  }


}

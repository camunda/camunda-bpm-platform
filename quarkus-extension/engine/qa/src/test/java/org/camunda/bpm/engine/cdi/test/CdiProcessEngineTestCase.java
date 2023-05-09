/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.cdi.test;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableInstance;
import io.quarkus.arc.InstanceHandle;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.junit.After;
import org.junit.Before;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.HashSet;
import java.util.Set;

public class CdiProcessEngineTestCase {

  protected String deploymentId;

  protected BeanManager beanManager;

  protected ProcessEngine processEngine;
  protected FormService formService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected AuthorizationService authorizationService;
  protected FilterService filterService;
  protected ExternalTaskService externalTaskService;
  protected CaseService caseService;
  protected DecisionService decisionService;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected Set<InstanceHandle<?>> beanInstanceHandles = new HashSet<>();

  @Before
  public void before() {
    Set<String> processEngineNames = BpmPlatform.getProcessEngineService()
        .getProcessEngineNames();
    if (processEngineNames.size() > 1) throw new RuntimeException("More than one process engines registered");
    processEngine =
        BpmPlatform.getProcessEngineService().getProcessEngine(processEngineNames.stream().findFirst().get());
    Arc.container().requestContext().activate();
    beanManager = Arc.container().beanManager();
    processEngineConfiguration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    processEngineConfiguration.setEnableExpressionsInAdhocQueries(true);
    formService = processEngine.getFormService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    authorizationService = processEngine.getAuthorizationService();
    filterService = processEngine.getFilterService();
    externalTaskService = processEngine.getExternalTaskService();
    caseService = processEngine.getCaseService();
    decisionService = processEngine.getDecisionService();
  }

  @After
  public void after() {
    Arc.container().requestContext().deactivate();

    beanInstanceHandles.forEach(bean -> {
      try {
        bean.destroy();
      } catch (UnsupportedOperationException ignored) {
        // Eagerly destroying InjectableBusinessProcessContext is unsupported
        // See https://jira.camunda.com/browse/CAM-13755
      }
    });

    beanInstanceHandles.clear();

    if (deploymentId != null) {
      repositoryService.deleteDeployment(deploymentId, true, true, true);
      deploymentId = null;
    }

    beanManager = null;
    processEngineConfiguration = null;
    formService = null;
    historyService = null;
    identityService = null;
    managementService = null;
    repositoryService = null;
    runtimeService = null;
    taskService = null;
    authorizationService = null;
    filterService = null;
    externalTaskService = null;
    caseService = null;
    decisionService = null;
  }

  protected <T> T getBeanInstance(Class<T> clazz) {
    InjectableInstance<T> select = Arc.container().select(clazz);
    InstanceHandle<T> handle = select.getHandle();
    beanInstanceHandles.add(handle);
    return handle.get();
  }

  protected Object getBeanInstance(String name) {
    InstanceHandle<Object> instance = Arc.container().instance(name);
    beanInstanceHandles.add(instance);
    return instance.get();
  }

  public void deploy(Class<?> testClass, String methodName, String[] resources) {
    if (resources != null) {
      deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, resources, testClass, methodName);
    }
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    TestHelper.waitForJobExecutorToProcessAllJobs(processEngineConfiguration, maxMillisToWait, intervalMillis);
  }

  @ApplicationScoped
  static class EngineConfigurer {

    @Produces
    public QuarkusProcessEngineConfiguration customEngineConfig() {

      QuarkusProcessEngineConfiguration engineConfig = new QuarkusProcessEngineConfiguration();
      engineConfig.setJobExecutorActivate(false);

      return engineConfig;
    }

  }

}

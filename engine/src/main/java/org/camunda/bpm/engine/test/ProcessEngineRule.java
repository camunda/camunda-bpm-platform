/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test;

import java.io.FileNotFoundException;
import java.util.Date;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Convenience for ProcessEngine and services initialization in the form of a
 * JUnit rule.
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * public class YourTest {
 *
 *   &#64;Rule
 *   public ProcessEngineRule processEngineRule = new ProcessEngineRule();
 *
 *   ...
 * }
 * </pre>
 * <p>
 * The ProcessEngine and the services will be made available to the test class
 * through the getters of the processEngineRule. The processEngine will be
 * initialized by default with the camunda.cfg.xml resource on the classpath. To
 * specify a different configuration file, pass the resource location in
 * {@link #ProcessEngineRule(String) the appropriate constructor}. Process
 * engines will be cached statically. Right before the first time the setUp is
 * called for a given configuration resource, the process engine will be
 * constructed.
 * </p>
 * <p>
 * You can declare a deployment with the {@link Deployment} annotation. This
 * base class will make sure that this deployment gets deployed before the setUp
 * and {@link RepositoryService#deleteDeployment(String, boolean) cascade
 * deleted} after the tearDown.
 * </p>
 * <p>
 * The processEngineRule also lets you
 * {@link ProcessEngineRule#setCurrentTime(Date) set the current time used by
 * the process engine}. This can be handy to control the exact time that is used
 * by the engine in order to verify e.g. e.g. due dates of timers. Or start, end
 * and duration times in the history service. In the tearDown, the internal
 * clock will automatically be reset to use the current system time rather then
 * the time that was set during a test method. In other words, you don't have to
 * clean up your own time messing mess ;-)
 * </p>
 *
 * @author Tom Baeyens
 */
public class ProcessEngineRule extends TestWatcher implements ProcessEngineServices {

  protected String configurationResource = "camunda.cfg.xml";
  protected String configurationResourceCompat = "activiti.cfg.xml";
  protected String deploymentId = null;

  protected boolean ensureCleanAfterTest = false;

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected FormService formService;
  protected FilterService filterService;
  protected AuthorizationService authorizationService;
  protected CaseService caseService;
  protected ExternalTaskService externalTaskService;

  public ProcessEngineRule() {
    this(false);
  }

  public ProcessEngineRule(boolean ensureCleanAfterTest) {
    this.ensureCleanAfterTest = ensureCleanAfterTest;
  }

  public ProcessEngineRule(String configurationResource) {
    this(configurationResource, false);
  }

  public ProcessEngineRule(String configurationResource, boolean ensureCleanAfterTest) {
    this.configurationResource = configurationResource;
    this.ensureCleanAfterTest = ensureCleanAfterTest;
  }

  public ProcessEngineRule(ProcessEngine processEngine) {
    this(processEngine, false);
  }

  public ProcessEngineRule(ProcessEngine processEngine, boolean ensureCleanAfterTest) {
    this.processEngine = processEngine;
    this.ensureCleanAfterTest = ensureCleanAfterTest;
  }

  @Override
  public void starting(Description description) {
    if (processEngine == null) {
      initializeProcessEngine();
    }

    initializeServices();

    deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, description.getTestClass(), description.getMethodName(),
        description.getAnnotation(Deployment.class));
  }

  protected void initializeProcessEngine() {
    try {
      processEngine = TestHelper.getProcessEngine(configurationResource);
    } catch (RuntimeException ex) {
      if (ex.getCause() != null && ex.getCause() instanceof FileNotFoundException) {
        processEngine = TestHelper.getProcessEngine(configurationResourceCompat);
      } else {
        throw ex;
      }
    }
  }

  protected void initializeServices() {
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
    formService = processEngine.getFormService();
    authorizationService = processEngine.getAuthorizationService();
    caseService = processEngine.getCaseService();
    filterService = processEngine.getFilterService();
    externalTaskService = processEngine.getExternalTaskService();
  }

  protected void clearServiceReferences() {
    repositoryService = null;
    runtimeService = null;
    taskService = null;
    formService = null;
    historyService = null;
    identityService = null;
    managementService = null;
    authorizationService = null;
    caseService = null;
    filterService = null;
    externalTaskService = null;
  }

  @Override
  public void finished(Description description) {
    TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, description.getTestClass(), description.getMethodName());
    if (ensureCleanAfterTest) {
      TestHelper.assertAndEnsureCleanDbAndCache(processEngine);
    }

    ClockUtil.reset();

    clearServiceReferences();
  }

  public void setCurrentTime(Date currentTime) {
    ClockUtil.setCurrentTime(currentTime);
  }

  public String getConfigurationResource() {
    return configurationResource;
  }

  public void setConfigurationResource(String configurationResource) {
    this.configurationResource = configurationResource;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  @Override
  public RuntimeService getRuntimeService() {
    return runtimeService;
  }

  public void setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  @Override
  public TaskService getTaskService() {
    return taskService;
  }

  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }

  @Override
  public HistoryService getHistoryService() {
    return historyService;
  }

  public void setHistoryService(HistoryService historyService) {
    this.historyService = historyService;
  }

  /**
   * @see #setHistoryService(HistoryService)
   * @param historicService
   *          the historiy service instance
   */
  public void setHistoricDataService(HistoryService historicService) {
    this.setHistoryService(historicService);
  }

  @Override
  public IdentityService getIdentityService() {
    return identityService;
  }

  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }

  @Override
  public ManagementService getManagementService() {
    return managementService;
  }

  @Override
  public AuthorizationService getAuthorizationService() {
    return authorizationService;
  }

  public void setAuthorizationService(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @Override
  public CaseService getCaseService() {
    return caseService;
  }

  public void setCaseService(CaseService caseService) {
    this.caseService = caseService;
  }

  @Override
  public FormService getFormService() {
    return formService;
  }

  public void setFormService(FormService formService) {
    this.formService = formService;
  }

  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }

  @Override
  public FilterService getFilterService() {
    return filterService;
  }

  public void setFilterService(FilterService filterService) {
    this.filterService = filterService;
  }

  @Override
  public ExternalTaskService getExternalTaskService() {
    return externalTaskService;
  }

  public void setExternalTaskService(ExternalTaskService externalTaskService) {
    this.externalTaskService = externalTaskService;
  }

}

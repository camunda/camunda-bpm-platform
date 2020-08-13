package org.camunda.bpm.engine.test;

import static java.util.Arrays.stream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessEngineExtension
    implements ProcessEngineServices, TestWatcher, TestInstancePostProcessor, 
    AfterTestExecutionCallback, BeforeTestExecutionCallback, ParameterResolver {
  
  private static final Logger LOG = LoggerFactory.getLogger(ProcessEngineExtension.class);

  protected String configurationResource = "camunda.cfg.xml";
  protected String configurationResourceCompat = "activiti.cfg.xml";
  protected String deploymentId = null;
  protected List<String> additionalDeployments = new ArrayList<>();

  protected ProcessEngine processEngine;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
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
  protected DecisionService decisionService;

  
  /*
   * protected boolean ensureCleanAfterTest = false;
   * 
   * public ProcessEngineExtension() { this(false); }
   * 
   * public ProcessEngineExtension(boolean ensureCleanAfterTest) {
   * this.ensureCleanAfterTest = ensureCleanAfterTest; }
   * 
   * public ProcessEngineExtension(String configurationResource) {
   * this(configurationResource, false); }
   * 
   * public ProcessEngineExtension(String configurationResource, boolean
   * ensureCleanAfterTest) { this.configurationResource = configurationResource;
   * this.ensureCleanAfterTest = ensureCleanAfterTest; }
   * 
   * public ProcessEngineExtension(ProcessEngine processEngine) {
   * this(processEngine, false); }
   * 
   * public ProcessEngineExtension(ProcessEngine processEngine, boolean
   * ensureCleanAfterTest) { this.processEngine = processEngine;
   * this.ensureCleanAfterTest = ensureCleanAfterTest; }
   */

  public static ProcessEngineExtension builder() {
    return new ProcessEngineExtension();
  }
  
  public ProcessEngineExtension configurationResource(String configurationResource) {
    this.setConfigurationResource(configurationResource);
    return this;
  }
  
  ///
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

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
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

  @Override
  public DecisionService getDecisionService() {
    return decisionService;
  }

  public void setDecisionService(DecisionService decisionService) {
    this.decisionService = decisionService;
  }

  public void manageDeployment(org.camunda.bpm.engine.repository.Deployment deployment) {
    this.additionalDeployments.add(deployment.getId());
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    if (processEngine == null) {
      initializeProcessEngine();
    }
    stream(testInstance.getClass().getDeclaredFields())
      .filter(field -> field.getType() == ProcessEngine.class)
      .forEach(field -> inject(testInstance, field));
  }

  private void inject(Object instance, Field field) {
    field.setAccessible(true);
    try {
      field.set(instance, processEngine);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }
  
  protected void clearServiceReferences() {
    processEngineConfiguration = null;
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
    decisionService = null;
  }

  @Override
  public void afterTestExecution(ExtensionContext context) throws Exception {
    identityService.clearAuthentication();
    processEngine.getProcessEngineConfiguration().setTenantCheckEnabled(true);

    TestHelper.annotationDeploymentTearDown(processEngine, deploymentId,
        context.getTestClass().get(), context.getTestMethod().get().getName()
        );
    for (String additionalDeployment : additionalDeployments) {
      TestHelper.deleteDeployment(processEngine, additionalDeployment);
    }

//    if (ensureCleanAfterTest) {
//      TestHelper.assertAndEnsureCleanDbAndCache(processEngine);
//    }

    TestHelper.resetIdGenerator(processEngineConfiguration);
    ClockUtil.reset();

    clearServiceReferences(); 
    
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) throws Exception {
    
    deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, context.getTestClass().get(), 
        context.getTestMethod().get().getName(), context.getElement().get().getAnnotation(Deployment.class));

    //apply
    if (processEngine == null) {
      initializeProcessEngine();
    }

    initializeServices();

//    final boolean hasRequiredHistoryLevel = TestHelper.annotationRequiredHistoryLevelCheck(processEngine, description);
//    final boolean runsWithRequiredDatabase = TestHelper.annotationRequiredDatabaseCheck(processEngine, description);
//    return new Statement() {
//
//      @Override
//      public void evaluate() throws Throwable {
//        Assume.assumeTrue("ignored because the current history level is too low", hasRequiredHistoryLevel);
//        Assume.assumeTrue("ignored because the database doesn't match the required ones", runsWithRequiredDatabase);
//        ProcessEngineRule.super.apply(base, description).evaluate();
//      }
//    };
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
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
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
    decisionService = processEngine.getDecisionService();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    LOG.info("supportsParameter: {}, {}", parameterContext.getParameter(), extensionContext);
    if (parameterContext.getParameter().getType().equals(ProcessEngine.class)) {
      LOG.info("supporting only ProcessEngine parameter");
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    LOG.info("resolveParameter: {}, {}", parameterContext.getParameter(), extensionContext);
    if (ProcessEngine.class.equals(parameterContext.getParameter().getType())) {
      LOG.info("return the processEngine");
      return getProcessEngine();
    } else {
      return null;
    }
  }
  
  public ProcessEngineExtension build() {
    if (processEngine == null) {
      initializeProcessEngine();
    }
    return this;
  }
}

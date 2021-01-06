package org.camunda.bpm.extension.junit5.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer;
import org.camunda.bpm.engine.impl.dmn.deployer.DecisionDefinitionDeployer;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.jupiter.api.Assumptions;
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

/**
 * Junit 5 Extension to create and inject a process engine into the test class.
 * <p>
 * Used as 
 * <code>@ExtendWith(ProcessEngineExtension.class)</code>
 * the process engine is build from the configuration file
 * {@code camunda.cfg.xml} found on the classpath.
 * <p>
 * Used as 
 * <pre>{@code  @RegisterExtension
 * ProcessEngineExtension extension = ProcessEngineExtension.builder()
 *    .configurationResource("myConfigurationFile.xml")
 *    .build();}</pre>
 * you can provide a different configuration file. 
 * <p>
 * This extension injects the process engine into a given field in the test class.
 * 
 * @author Ingo Richtsmeier
 *
 */
public class ProcessEngineExtension implements TestWatcher, 
    TestInstancePostProcessor, BeforeTestExecutionCallback, AfterTestExecutionCallback, 
    ParameterResolver {

  private static Supplier<IllegalStateException> illegalStateException(String msg) {
    return () -> new IllegalStateException(msg);
  }

  private static final Logger LOG = LoggerFactory.getLogger(ProcessEngineExtension.class);
  protected ProcessEngine processEngine;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  
  protected String configurationResource = "camunda.cfg.xml";
  
  private String deploymentId;

  public final static List<String> RESOURCE_SUFFIXES = new ArrayList<>();

  static {
    RESOURCE_SUFFIXES.addAll(Arrays.asList(BpmnDeployer.BPMN_RESOURCE_SUFFIXES));
    RESOURCE_SUFFIXES.addAll(Arrays.asList(CmmnDeployer.CMMN_RESOURCE_SUFFIXES));
    RESOURCE_SUFFIXES.addAll(Arrays.asList(DecisionDefinitionDeployer.DMN_RESOURCE_SUFFIXES));
  }

  public static ProcessEngineExtension builder() {
    return new ProcessEngineExtension();
  }

  public ProcessEngineExtension configurationResource(String configurationResource) {
    this.configurationResource = configurationResource;
    return this;
  }
  
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  
  public String getConfigurationResource() {
    return configurationResource;
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
    if (processEngine == null) {
      initializeProcessEngine();
    }
    Arrays.stream(testInstance.getClass().getDeclaredFields())
      .filter(field -> field.getType() == ProcessEngine.class)
      .forEach(field -> inject(testInstance, field));
  }

  protected void initializeProcessEngine() {
    processEngine = TestHelper.getProcessEngine(configurationResource);
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
  }

  private void inject(Object instance, Field field) {
    field.setAccessible(true);
    try {
      field.set(instance, processEngine);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    LOG.debug("beforeTestExecution: {}", context.getDisplayName());
    
    final Method testMethod = context.getTestMethod().orElseThrow(illegalStateException("testMethod not set"));
    final Class<?> testClass = context.getTestClass().orElseThrow(illegalStateException("testClass not set"));
    
    doDeployment(testMethod, testClass);
    
    checkRequiredHistoryLevel(testMethod);
  }

  private void doDeployment(Method testMethod, Class<?> testClass) {
    DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService()
        .createDeployment()
        .name(testClass.getSimpleName()+"."+testMethod.getName());
    
    Deployment methodAnnotation = testMethod.getAnnotation(Deployment.class);
    if (methodAnnotation != null) {
      getDeploymentResources(testClass, testMethod.getName(), methodAnnotation, deploymentBuilder);
      LOG.info("annotation @Deployment creates deployment for {}.{}", testClass.getName(), testMethod.getName());
      deploymentId = deploymentBuilder
          .deploy()
          .getId();
    } else {
      Deployment classAnnotation = testClass.getAnnotation(Deployment.class);
      if (classAnnotation != null) {
        getDeploymentResources(testClass, null, classAnnotation, deploymentBuilder);
        LOG.info("annotation @Deployment creates deployment for {}.{}", testClass.getName(), testMethod.getName());
        deploymentId = deploymentBuilder
            .deploy()
            .getId();
      } else {
        Class<?> lookForAnnotationClass = testClass.getSuperclass();
        while (lookForAnnotationClass != Object.class) {
          classAnnotation = lookForAnnotationClass.getAnnotation(Deployment.class);
          if (classAnnotation == null) {
            lookForAnnotationClass = lookForAnnotationClass.getSuperclass();
          } else {
            break;
          }
        }
        if (classAnnotation != null) {
          getDeploymentResources(lookForAnnotationClass, null, classAnnotation, deploymentBuilder);
          LOG.info("annotation @Deployment creates deployment for {}.{}", testClass.getName(), testMethod.getName());
          deploymentId = deploymentBuilder
              .deploy()
              .getId();
        }
      }
    }
  }

  private void getDeploymentResources(Class<?> testClass, String testMethodName, Deployment annotation,
      DeploymentBuilder deploymentBuilder) {
    String[] resources = annotation.resources();
    if (resources.length == 0) {
      deploymentBuilder.addClasspathResource(TestHelper.getBpmnProcessDefinitionResource(
          testClass, testMethodName));
    } else {
      for (String resource : resources) {
        deploymentBuilder.addClasspathResource(resource);
      }
    }
  }
  
  private void checkRequiredHistoryLevel(Method testMethod) {
    RequiredHistoryLevel annotation = testMethod.getAnnotation(RequiredHistoryLevel.class);
    if (annotation != null) {
      HistoryLevel currentHistoryLevel = getProcessEngineConfiguration().getHistoryLevel();
      String requiredHistoryLevelName = annotation.value();
      int requiredHistoryLevel = 0; 
      for (HistoryLevel level : getProcessEngineConfiguration().getHistoryLevels()) {
        if (level.getName().equalsIgnoreCase(requiredHistoryLevelName)) {
          requiredHistoryLevel = level.getId();
        }
      }
      Assumptions.assumeTrue(
          currentHistoryLevel.getId() >= requiredHistoryLevel, 
          "ignored because the current history level is too low");
    }
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    final Method testMethod = context.getTestMethod().orElseThrow(illegalStateException("testMethod not set"));
    final Class<?> testClass = context.getTestClass().orElseThrow(illegalStateException("testClass not set"));

    if (deploymentId != null) {
      LOG.info("annotation @Deployment deletes deployment for {}.{}", testClass.getName(), testMethod.getName());     
      processEngine.getRepositoryService().deleteDeployment(deploymentId, true, true, true);
      deploymentId = null;
    }  
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(ProcessEngine.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    if (ProcessEngine.class.equals(parameterContext.getParameter().getType())) {
      LOG.debug("resolve the processEngine as parameter");
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

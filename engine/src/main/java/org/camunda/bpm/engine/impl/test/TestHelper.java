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
package org.camunda.bpm.engine.impl.test;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoryLevelSetupCommand;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.behavior.CaseControlRuleImpl;
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer;
import org.camunda.bpm.engine.impl.db.DbIdGenerator;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.dmn.deployer.DecisionDefinitionDeployer;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.management.DatabasePurgeReport;
import org.camunda.bpm.engine.impl.management.PurgeReport;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.CachePurgeReport;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public abstract class TestHelper {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  public static final String EMPTY_LINE = "                                                                                           ";

  public static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY",
    "ACT_GE_SCHEMA_LOG"
  );

  static Map<String, ProcessEngine> processEngines = new HashMap<>();

  public final static List<String> RESOURCE_SUFFIXES = new ArrayList<>();

  static {
    RESOURCE_SUFFIXES.addAll(Arrays.asList(BpmnDeployer.BPMN_RESOURCE_SUFFIXES));
    RESOURCE_SUFFIXES.addAll(Arrays.asList(CmmnDeployer.CMMN_RESOURCE_SUFFIXES));
    RESOURCE_SUFFIXES.addAll(Arrays.asList(DecisionDefinitionDeployer.DMN_RESOURCE_SUFFIXES));
  }

  /**
   * use {@link ProcessEngineAssert} instead.
   */
  @Deprecated
  public static void assertProcessEnded(ProcessEngine processEngine, String processInstanceId) {
    ProcessEngineAssert.assertProcessEnded(processEngine, processInstanceId);
  }

  public static String annotationDeploymentSetUp(ProcessEngine processEngine, Class<?> testClass, String methodName,
      Deployment deploymentAnnotation, Class<?>... parameterTypes) {
    Method method = null;
    boolean onMethod = true;

    try {
      method = getMethod(testClass, methodName, parameterTypes);
    } catch (Exception e) {
      if (deploymentAnnotation == null) {
        // we have neither the annotation, nor can look it up from the method
        return null;
      }
    }

    if (deploymentAnnotation == null) {
      deploymentAnnotation = method.getAnnotation(Deployment.class);
    }
    // if not found on method, try on class level
    if (deploymentAnnotation == null) {
      onMethod = false;
      Class<?> lookForAnnotationClass = testClass;
      while (lookForAnnotationClass != Object.class) {
        deploymentAnnotation = lookForAnnotationClass.getAnnotation(Deployment.class);
        if (deploymentAnnotation != null) {
          testClass = lookForAnnotationClass;
          break;
        }
        lookForAnnotationClass = lookForAnnotationClass.getSuperclass();
      }
    }

    if (deploymentAnnotation != null) {
      String[] resources = deploymentAnnotation.resources();
      LOG.debug("annotation @Deployment creates deployment for {}.{}", ClassNameUtil.getClassNameWithoutPackage(testClass), methodName);
      return annotationDeploymentSetUp(processEngine, resources, testClass, onMethod, methodName);

    } else {
      return null;

    }
  }

  public static String annotationDeploymentSetUp(ProcessEngine processEngine, String[] resources, Class<?> testClass, String methodName) {
    return annotationDeploymentSetUp(processEngine, resources, testClass, true, methodName);
  }

  public static String annotationDeploymentSetUp(ProcessEngine processEngine, String[] resources, Class<?> testClass,
      boolean onMethod, String methodName) {
    if (resources != null) {
      if (resources.length == 0 && methodName != null) {
        String name = onMethod ? methodName : null;
        String resource = getBpmnProcessDefinitionResource(testClass, name);
        resources = new String[]{resource};
      }

      DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService()
        .createDeployment()
        .name(ClassNameUtil.getClassNameWithoutPackage(testClass)+"."+methodName);

      for (String resource: resources) {
        deploymentBuilder.addClasspathResource(resource);
      }

      return deploymentBuilder.deploy().getId();
    }

    return null;
  }

  public static String annotationDeploymentSetUp(ProcessEngine processEngine, Class<?> testClass, String methodName, Class<?>... parameterTypes) {
    return annotationDeploymentSetUp(processEngine, testClass, methodName, (Deployment) null, parameterTypes);
  }

  public static void annotationDeploymentTearDown(ProcessEngine processEngine, String deploymentId, Class<?> testClass, String methodName) {
    LOG.debug("annotation @Deployment deletes deployment for {}.{}", ClassNameUtil.getClassNameWithoutPackage(testClass), methodName);
    deleteDeployment(processEngine, deploymentId);
  }

  public static void deleteDeployment(ProcessEngine processEngine, String deploymentId) {
    if(deploymentId != null) {
      processEngine.getRepositoryService().deleteDeployment(deploymentId, true, true, true);
    }
  }

  /**
   * get a resource location by convention based on a class (type) and a
   * relative resource name. The return value will be the full classpath
   * location of the type, plus a suffix built from the name parameter:
   * <code>BpmnDeployer.BPMN_RESOURCE_SUFFIXES</code>.
   * The first resource matching a suffix will be returned.
   */
  public static String getBpmnProcessDefinitionResource(Class< ? > type, String name) {
    for (String suffix : RESOURCE_SUFFIXES) {
      String resource = createResourceName(type, name, suffix);
      InputStream inputStream = ReflectUtil.getResourceAsStream(resource);
      if (inputStream == null) {
        continue;
      } else {
        return resource;
      }
    }
    return createResourceName(type, name, BpmnDeployer.BPMN_RESOURCE_SUFFIXES[0]);
  }

  private static String createResourceName(Class< ? > type, String name, String suffix) {
    StringBuilder r = new StringBuilder(type.getName().replace('.', '/'));
    if (name != null) {
      r.append("." + name);
    }
    return r.append("." + suffix).toString();
  }

  public static boolean annotationRequiredHistoryLevelCheck(ProcessEngine processEngine, RequiredHistoryLevel annotation, Class<?> testClass, String methodName) {

    if (annotation != null) {
      return historyLevelCheck(processEngine, annotation);

    } else {
      return annotationRequiredHistoryLevelCheck(processEngine, testClass, methodName);
    }
  }

  private static boolean historyLevelCheck(ProcessEngine processEngine, RequiredHistoryLevel annotation) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    HistoryLevel requiredHistoryLevel = getHistoryLevelForName(processEngineConfiguration.getHistoryLevels(), annotation.value());
    HistoryLevel currentHistoryLevel = processEngineConfiguration.getHistoryLevel();

    return currentHistoryLevel.getId() >= requiredHistoryLevel.getId();
  }

  private static HistoryLevel getHistoryLevelForName(List<HistoryLevel> historyLevels, String name) {
    for (HistoryLevel historyLevel : historyLevels) {

      if (historyLevel.getName().equalsIgnoreCase(name)) {
        return historyLevel;
      }
    }
    throw new IllegalArgumentException("Unknown history level: " + name);
  }

  public static boolean annotationRequiredHistoryLevelCheck(ProcessEngine processEngine, Class<?> testClass, String methodName, Class<?>... parameterTypes) {
    RequiredHistoryLevel annotation = getAnnotation(processEngine, testClass, methodName, RequiredHistoryLevel.class, parameterTypes);

    if (annotation != null) {
      return historyLevelCheck(processEngine, annotation);
    } else {
      return true;
    }
  }

  public static boolean annotationRequiredDatabaseCheck(ProcessEngine processEngine, RequiredDatabase annotation, Class<?> testClass, String methodName, Class<?>... parameterTypes) {

    if (annotation != null) {
      return databaseCheck(processEngine, annotation);

    } else {
      return annotationRequiredDatabaseCheck(processEngine, testClass, methodName, parameterTypes);
    }
  }

  public static boolean annotationRequiredDatabaseCheck(ProcessEngine processEngine, Class<?> testClass, String methodName, Class<?>... parameterTypes) {
    RequiredDatabase annotation = getAnnotation(processEngine, testClass, methodName, RequiredDatabase.class, parameterTypes);

    if (annotation != null) {
      return databaseCheck(processEngine, annotation);
    } else {
      return true;
    }
  }

  private static boolean databaseCheck(ProcessEngine processEngine, RequiredDatabase annotation) {

    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    String actualDbType = processEngineConfiguration.getDbSqlSessionFactory().getDatabaseType();

    String[] excludes = annotation.excludes();

    if (excludes != null) {
      for (String exclude : excludes) {
        if (exclude.equals(actualDbType)) {
          return false;
        }
      }
    }

    String[] includes = annotation.includes();

    if (includes != null && includes.length > 0) {
      for (String include : includes) {
        if (include.equals(actualDbType)) {
          return true;
        }
      }

      return false;
    } else {
      return true;
    }

  }


  private static <T extends Annotation> T getAnnotation(ProcessEngine processEngine, Class<?> testClass,
      String methodName, Class<T> annotationClass, Class<?>... parameterTypes) {
    Method method = null;
    T annotation = null;

    try {
      method = getMethod(testClass, methodName, parameterTypes);
      annotation = method.getAnnotation(annotationClass);
    } catch (Exception e) {
      // - ignore if we cannot access the method
      // - just try again with the class
      // => can for example be the case for parameterized tests where methodName does not correspond to the actual method name
      //    (note that method-level annotations still work in this
      //     scenario due to Description#getAnnotation in annotationRequiredHistoryLevelCheck)
    }

    // if not found on method, try on class level
    if (annotation == null) {
      annotation = testClass.getAnnotation(annotationClass);
    }
    return annotation;
  }

  protected static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException {
    try {
      return clazz.getDeclaredMethod(methodName, parameterTypes);

    } catch (NoSuchMethodException e) {
      if (!clazz.equals(Object.class)) {
        return getMethod(clazz.getSuperclass(), methodName, parameterTypes);

      } else {
        throw e;

      }
    }
  }

  /**
   * Ensures that the deployment cache and database is clean after a test. If not the cache
   * and database will be cleared.
   *
   * @param processEngine the {@link ProcessEngine} to test
   * @throws AssertionError if the deployment cache or database was not clean
   */
  public static void assertAndEnsureCleanDbAndCache(ProcessEngine processEngine) {
    assertAndEnsureCleanDbAndCache(processEngine, true);
  }

  /**
   * Ensures that the deployment cache and database is clean after a test. If not the cache
   * and database will be cleared.
   *
   * @param processEngine the {@link ProcessEngine} to test
   * @param fail if true the method will throw an {@link AssertionError} if the deployment cache or database is not clean
   * @throws AssertionError if the deployment cache or database was not clean
   */
  public static String assertAndEnsureCleanDbAndCache(ProcessEngine processEngine, boolean fail) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();

    // clear user operation log in case some operations are
    // executed with an authenticated user
    clearUserOperationLog(processEngineConfiguration);

    LOG.debug("verifying that db is clean after test");
    PurgeReport purgeReport = ((ManagementServiceImpl) processEngine.getManagementService()).purge();

    String paRegistrationMessage = assertAndEnsureNoProcessApplicationsRegistered(processEngine);

    StringBuilder message = new StringBuilder();
    CachePurgeReport cachePurgeReport = purgeReport.getCachePurgeReport();
    if (!cachePurgeReport.isEmpty()) {
      message.append("Deployment cache is not clean:\n")
             .append(cachePurgeReport.getPurgeReportAsString());
    } else {
      LOG.debug("Deployment cache was clean.");
    }
    DatabasePurgeReport databasePurgeReport = purgeReport.getDatabasePurgeReport();
    if (!databasePurgeReport.isEmpty()) {
      message.append("Database is not clean:\n")
             .append(databasePurgeReport.getPurgeReportAsString());
    } else {
      LOG.debug(
          purgeReport.getDatabasePurgeReport().isDbContainsLicenseKey() ? "Database contains license key but is considered clean." : "Database was clean.");
    }
    if (paRegistrationMessage != null) {
      message.append(paRegistrationMessage);
    }

    if (fail && message.length() > 0) {
      fail(message.toString());
    }

    return message.toString();
  }

  protected static void fail(String message) {
    if (message == null) {
        throw new AssertionError();
    }
    throw new AssertionError(message);
  }

  /**
   * Ensures that the deployment cache is empty after a test. If not the cache
   * will be cleared.
   *
   * @param processEngine the {@link ProcessEngine} to test
   * @throws AssertionError if the deployment cache was not clean
   */
  public static void assertAndEnsureCleanDeploymentCache(ProcessEngine processEngine) {
    assertAndEnsureCleanDeploymentCache(processEngine, true);
  }

  /**
   * Ensures that the deployment cache is empty after a test. If not the cache
   * will be cleared.
   *
   * @param processEngine the {@link ProcessEngine} to test
   * @param fail if true the method will throw an {@link AssertionError} if the deployment cache is not clean
   * @return the deployment cache summary if fail is set to false or null if deployment cache was clean
   * @throws AssertionError if the deployment cache was not clean and fail is set to true
   */
  public static String assertAndEnsureCleanDeploymentCache(ProcessEngine processEngine, boolean fail) {
    StringBuilder outputMessage = new StringBuilder();
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    CachePurgeReport cachePurgeReport = processEngineConfiguration.getDeploymentCache().purgeCache();

    outputMessage.append(cachePurgeReport.getPurgeReportAsString());
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "Deployment cache not clean:\n");
      LOG.error(outputMessage.toString());

      if (fail) {
        fail(outputMessage.toString());
      }

      return outputMessage.toString();
    }
    else {
      LOG.debug("Deployment cache was clean");
      return null;
    }
  }


  public static String assertAndEnsureNoProcessApplicationsRegistered(ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    ProcessApplicationManager processApplicationManager = engineConfiguration.getProcessApplicationManager();

    if (processApplicationManager.hasRegistrations()) {
      processApplicationManager.clearRegistrations();
      return "There are still process applications registered";
    }
    else {
      return null;
    }

  }

  public static void waitForJobExecutorToProcessAllJobs(ProcessEngineConfigurationImpl processEngineConfiguration, long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable(processEngineConfiguration);
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public static boolean areJobsAvailable(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return !processEngineConfiguration
      .getManagementService()
      .createJobQuery()
      .executable()
      .list()
      .isEmpty();
  }

  public static void resetIdGenerator(ProcessEngineConfigurationImpl processEngineConfiguration) {
    IdGenerator idGenerator = processEngineConfiguration.getIdGenerator();

    if (idGenerator instanceof DbIdGenerator) {
      ((DbIdGenerator) idGenerator).reset();
    }
  }

  private static class InteruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InteruptTask(Thread thread) {
      this.thread = thread;
    }
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    @Override
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

  public static ProcessEngine getProcessEngine(String configurationResource) {
    ProcessEngine processEngine = processEngines.get(configurationResource);
    if (processEngine==null) {
      LOG.debug("==== BUILDING PROCESS ENGINE ========================================================================");
      processEngine = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource(configurationResource)
        .buildProcessEngine();
      LOG.debug("==== PROCESS ENGINE CREATED =========================================================================");
      processEngines.put(configurationResource, processEngine);
    }
    return processEngine;
  }

  public static void closeProcessEngines() {
    for (ProcessEngine processEngine: processEngines.values()) {
      processEngine.close();
    }
    processEngines.clear();
  }

  public static void createSchema(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> {

          commandContext.getSession(PersistenceSession.class).dbSchemaCreate();
          return null;
        });
  }

  public static void dropSchema(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> {
          commandContext.getDbSqlSession().dbSchemaDrop();
          return null;
        });
  }

  public static void createOrUpdateHistoryLevel(final ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(commandContext -> {
        DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
        PropertyEntity historyLevelProperty = dbEntityManager.selectById(PropertyEntity.class, "historyLevel");
        if (historyLevelProperty != null) {
          if (processEngineConfiguration.getHistoryLevel().getId() != new Integer(historyLevelProperty.getValue())) {
            historyLevelProperty.setValue(Integer.toString(processEngineConfiguration.getHistoryLevel().getId()));
            dbEntityManager.merge(historyLevelProperty);
          }
        } else {
          HistoryLevelSetupCommand.dbCreateHistoryLevel(commandContext);
        }
        return null;
      });
  }

  public static void deleteHistoryLevel(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(commandContext -> {
        DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
        PropertyEntity historyLevelProperty = dbEntityManager.selectById(PropertyEntity.class, "historyLevel");
        if (historyLevelProperty != null) {
          dbEntityManager.delete(historyLevelProperty);
        }
        return null;
      });
  }

  public static void clearUserOperationLog(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (processEngineConfiguration.getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
      HistoryService historyService = processEngineConfiguration.getHistoryService();
      List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().list();
      for (UserOperationLogEntry log : logs) {
        historyService.deleteUserOperationLogEntry(log.getId());
      }
    }
  }

  public static void deleteTelemetryProperty(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(commandContext -> {
        DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
        PropertyEntity telemetryProperty = dbEntityManager.selectById(PropertyEntity.class, "camunda.telemetry.enabled");
        if (telemetryProperty != null) {
          dbEntityManager.delete(telemetryProperty);
        }
        return null;
      });
  }

  public static void deleteInstallationId(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(commandContext -> {
        DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
        PropertyEntity installationIdProperty = dbEntityManager.selectById(PropertyEntity.class, "camunda.installation.id");
        if (installationIdProperty != null) {
          dbEntityManager.delete(installationIdProperty);
        }
        return null;
      });
  }

  public static Object defaultManualActivation() {
    Expression expression = new FixedValue(true);
    CaseControlRuleImpl caseControlRule = new CaseControlRuleImpl(expression);
    return caseControlRule;
  }
}

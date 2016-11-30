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

package org.camunda.bpm.engine.impl.test;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.db.DbIdGenerator;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.dmn.deployer.DecisionDefinitionDeployer;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.commons.utils.cache.Cache;
import org.junit.Assert;
import org.junit.runner.Description;
import org.slf4j.Logger;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;


/**
 * @author Tom Baeyens
 */
public abstract class TestHelper {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  public static final String EMPTY_LINE = "                                                                                           ";

  public static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY"
  );

  static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>();

  public final static List<String> RESOURCE_SUFFIXES = new ArrayList<String>();

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

  public static String annotationDeploymentSetUp(ProcessEngine processEngine, Class<?> testClass, String methodName, Deployment deploymentAnnotation) {
    String deploymentId = null;
    Method method = null;
    boolean onMethod = true;

    try {
      method = getMethod(testClass, methodName);
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
      LOG.debug("annotation @Deployment creates deployment for {}.{}", ClassNameUtil.getClassNameWithoutPackage(testClass), methodName);
      String[] resources = deploymentAnnotation.resources();
      if (resources.length == 0 && method != null) {
        String name = onMethod ? method.getName() : null;
        String resource = getBpmnProcessDefinitionResource(testClass, name);
        resources = new String[]{resource};
      }

      DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService()
        .createDeployment()
        .name(ClassNameUtil.getClassNameWithoutPackage(testClass)+"."+methodName);

      for (String resource: resources) {
        deploymentBuilder.addClasspathResource(resource);
      }

      deploymentId = deploymentBuilder.deploy().getId();
    }

    return deploymentId;
  }

  public static String annotationDeploymentSetUp(ProcessEngine processEngine, Class<?> testClass, String methodName) {
    return annotationDeploymentSetUp(processEngine, testClass, methodName, null);
  }

  public static void annotationDeploymentTearDown(ProcessEngine processEngine, String deploymentId, Class<?> testClass, String methodName) {
    LOG.debug("annotation @Deployment deletes deployment for {}.{}", ClassNameUtil.getClassNameWithoutPackage(testClass), methodName);
    deleteDeployment(processEngine, deploymentId);
  }

  public static void deleteDeployment(ProcessEngine processEngine, String deploymentId) {
    if(deploymentId != null) {
      processEngine.getRepositoryService().deleteDeployment(deploymentId, true);
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
    StringBuffer r = new StringBuffer(type.getName().replace('.', '/'));
    if (name != null) {
      r.append("." + name);
    }
    return r.append("." + suffix).toString();
  }

  public static boolean annotationRequiredHistoryLevelCheck(ProcessEngine processEngine, Description description) {
    RequiredHistoryLevel annotation = description.getAnnotation(RequiredHistoryLevel.class);

    if (annotation != null) {
      return historyLevelCheck(processEngine, annotation);

    } else {
      return annotationRequiredHistoryLevelCheck(processEngine, description.getTestClass(), description.getMethodName());
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

  public static boolean annotationRequiredHistoryLevelCheck(ProcessEngine processEngine, Class<?> testClass, String methodName) {
    RequiredHistoryLevel annotation = getAnnotation(processEngine, testClass, methodName, RequiredHistoryLevel.class);

    if (annotation != null) {
      return historyLevelCheck(processEngine, annotation);
    } else {
      return true;
    }
  }

  private static <T extends Annotation> T getAnnotation(ProcessEngine processEngine, Class<?> testClass, String methodName, Class<T> annotationClass) {
    Method method = null;
    T annotation = null;

    try {
      method = getMethod(testClass, methodName);
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

  protected static Method getMethod(Class<?> clazz, String methodName) throws SecurityException, NoSuchMethodException {
    return clazz.getMethod(methodName, (Class<?>[]) null);
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
  public static void assertAndEnsureCleanDbAndCache(ProcessEngine processEngine, boolean fail) {
    String cacheMessage = assertAndEnsureCleanDeploymentCache(processEngine, false);
    String dbMessage = assertAndEnsureCleanDb(processEngine, false);
    String paRegistrationMessage = assertAndEnsureNoProcessApplicationsRegistered(processEngine);

    StringBuilder message = new StringBuilder();
    if (cacheMessage != null) {
      message.append(cacheMessage);
    }
    if (dbMessage != null) {
      message.append(dbMessage);
    }
    if (paRegistrationMessage != null) {
      message.append(paRegistrationMessage);
    }

    if (fail && message.length() > 0) {
      Assert.fail(message.toString());
    }
  }

  /**
   * Ensures that the deployment cache is empty after a test. If not the cache
   * will be cleared.
   *
   * @param processEngine the {@link ProcessEngine} to test
   * @throws AssertionError if the deployment cache was not clean
   */
  public static void assertAndEnsureCleanDeploymentCache(ProcessEngine processEngine) {
    assertAndEnsureCleanDb(processEngine, true);
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
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    Cache<String, ProcessDefinitionEntity> processDefinitionCache = deploymentCache.getProcessDefinitionCache();

    if (!processDefinitionCache.isEmpty()) {
      outputMessage.append("\tProcess Definition Cache: ").append(processDefinitionCache.keySet()).append("\n");
      processDefinitionCache.clear();
    }

    Cache<String, BpmnModelInstance> bpmnModelInstanceCache = deploymentCache.getBpmnModelInstanceCache();

    if (!bpmnModelInstanceCache.isEmpty()) {
      outputMessage.append("\tBPMN Model Instance Cache: ").append(bpmnModelInstanceCache.keySet()).append("\n");
      bpmnModelInstanceCache.clear();
    }

    Cache<String, CaseDefinitionEntity> caseDefinitionCache = deploymentCache.getCaseDefinitionCache();

    if (!caseDefinitionCache.isEmpty()) {
      outputMessage.append("\tCase Definition Cache: ").append(caseDefinitionCache.keySet()).append("\n");
      caseDefinitionCache.clear();
    }

    Cache<String, CmmnModelInstance> cmmnModelInstanceCache = deploymentCache.getCmmnModelInstanceCache();

    if (!cmmnModelInstanceCache.isEmpty()) {
      outputMessage.append("\tCMMN Model Instance Cache: ").append(cmmnModelInstanceCache.keySet()).append("\n");
      cmmnModelInstanceCache.clear();
    }

    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "Deployment cache not clean:\n");
      LOG.error(outputMessage.toString());

      if (fail) {
        Assert.fail(outputMessage.toString());
      }

      return outputMessage.toString();
    }
    else {
      LOG.debug("Deployment cache was clean");
      return null;
    }
  }

  /**
   * Ensures that the database is clean after the test. This means the test has to remove
   * all resources it entered to the database.
   * If the DB is not clean, it is cleaned by performing a create a drop.
   *
   * @param processEngine the {@link ProcessEngine} to check
   * @throws AssertionError if the database was not clean
   */
  public static void assertAndEnsureCleanDb(ProcessEngine processEngine) {
    assertAndEnsureCleanDb(processEngine, true);
  }

  /**
   * Ensures that the database is clean after the test. This means the test has to remove
   * all resources it entered to the database.
   * If the DB is not clean, it is cleaned by performing a create a drop.
   *
   * @param processEngine the {@link ProcessEngine} to check
   * @param fail if true the method will throw an {@link AssertionError} if the database is not clean
   * @return the database summary if fail is set to false or null if database was clean
   * @throws AssertionError if the database was not clean and fail is set to true
   */
  public static String assertAndEnsureCleanDb(ProcessEngine processEngine, boolean fail) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    String databaseTablePrefix = processEngineConfiguration.getDatabaseTablePrefix().trim();

    // clear user operation log in case some operations are
    // executed with an authenticated user
    clearUserOperationLog(processEngineConfiguration);

    LOG.debug("verifying that db is clean after test");
    Map<String, Long> tableCounts = processEngine.getManagementService().getTableCount();

    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(databaseTablePrefix, "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count!=0L) {
          outputMessage.append("\t").append(tableName).append(": ").append(count).append(" record(s)\n");
        }
      }
    }

    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      LOG.error(outputMessage.toString());

      /** skip drop and recreate if a table prefix is used */
      if (databaseTablePrefix.isEmpty()) {
        LOG.error("Dropping and recreating database");

        processEngineConfiguration
          .getCommandExecutorSchemaOperations()
          .execute(new Command<Object>() {
            public Object execute(CommandContext commandContext) {
              PersistenceSession persistenceSession = commandContext.getSession(PersistenceSession.class);
              persistenceSession.dbSchemaDrop();
              persistenceSession.dbSchemaCreate();
              SchemaOperationsProcessEngineBuild.dbCreateHistoryLevel(commandContext.getDbEntityManager());
              return null;
            }
          });
      }
      else {
        LOG.info("Skipping recreating of database as a table prefix is used");
      }

      if (fail) {
        Assert.fail(outputMessage.toString());
      }
      else {
        return outputMessage.toString();
      }

    } else {
      LOG.debug("Database was clean");
    }
    return null;
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
        .execute(new Command<Object>() {
          public Object execute(CommandContext commandContext) {

            commandContext.getSession(PersistenceSession.class).dbSchemaCreate();
            return null;
          }
        });
  }

  public static void dropSchema(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(new Command<Object>() {
         public Object execute(CommandContext commandContext) {
           commandContext.getDbSqlSession().dbSchemaDrop();
           return null;
         }
        });
  }

  public static void createOrUpdateHistoryLevel(final ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
       public Object execute(CommandContext commandContext) {
         DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
         PropertyEntity historyLevelProperty = dbEntityManager.selectById(PropertyEntity.class, "historyLevel");
         if (historyLevelProperty != null) {
           if (processEngineConfiguration.getHistoryLevel().getId() != new Integer(historyLevelProperty.getValue())) {
             historyLevelProperty.setValue(Integer.toString(processEngineConfiguration.getHistoryLevel().getId()));
             dbEntityManager.merge(historyLevelProperty);
           }
         } else {
           SchemaOperationsProcessEngineBuild.dbCreateHistoryLevel(dbEntityManager);
         }
         return null;
       }
      });
  }

  public static void deleteHistoryLevel(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
       public Object execute(CommandContext commandContext) {
         DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
         PropertyEntity historyLevelProperty = dbEntityManager.selectById(PropertyEntity.class, "historyLevel");
         if (historyLevelProperty != null) {
           dbEntityManager.delete(historyLevelProperty);
         }
         return null;
       }
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




}

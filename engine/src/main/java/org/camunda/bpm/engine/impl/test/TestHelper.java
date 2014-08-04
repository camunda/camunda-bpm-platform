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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public abstract class TestHelper {

  private static Logger log = Logger.getLogger(TestHelper.class.getName());

  public static final String EMPTY_LINE = "                                                                                           ";

  public static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY"
  );

  static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>();

  /**
   * use {@link ProcessEngineAssert} instead.
   */
  @Deprecated
  public static void assertProcessEnded(ProcessEngine processEngine, String processInstanceId) {
    ProcessEngineAssert.assertProcessEnded(processEngine, processInstanceId);
  }

  public static String annotationDeploymentSetUp(ProcessEngine processEngine, Class<?> testClass, String methodName) {
    String deploymentId = null;
    Method method = null;
    try {
      method = testClass.getDeclaredMethod(methodName, (Class<?>[])null);
    } catch (Exception e) {
      return null;
    }
    Deployment deploymentAnnotation = method.getAnnotation(Deployment.class);
    if (deploymentAnnotation != null) {
      log.fine("annotation @Deployment creates deployment for "+ClassNameUtil.getClassNameWithoutPackage(testClass)+"."+methodName);
      String[] resources = deploymentAnnotation.resources();
      if (resources.length == 0) {
        String name = method.getName();
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

  public static void annotationDeploymentTearDown(ProcessEngine processEngine, String deploymentId, Class<?> testClass, String methodName) {
    log.fine("annotation @Deployment deletes deployment for "+ClassNameUtil.getClassNameWithoutPackage(testClass)+"."+methodName);
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
    for (String suffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      String resource = type.getName().replace('.', '/') + "." + name + "." + suffix;
      InputStream inputStream = ReflectUtil.getResourceAsStream(resource);
      if (inputStream == null) {
        continue;
      } else {
        return resource;
      }
    }
    return type.getName().replace('.', '/') + "." + name + "." + BpmnDeployer.BPMN_RESOURCE_SUFFIXES[0];
  }

  /** Each test is assumed to clean up all DB content it entered.
   * After a test method executed, this method scans all tables to see if the DB is completely clean.
   * It throws AssertionFailed in case the DB is not clean.
   * If the DB is not clean, it is cleaned by performing a create a drop. */
  public static void assertAndEnsureCleanDb(ProcessEngine processEngine) {
    log.fine("verifying that db is clean after test");
    Map<String, Long> tableCounts = processEngine.getManagementService().getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableName)) {
        Long count = tableCounts.get(tableName);
        if (count!=0L) {
          outputMessage.append("  "+tableName + ": " + count + " record(s) ");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.severe(EMPTY_LINE);
      log.severe(outputMessage.toString());

      ((ProcessEngineImpl)processEngine)
        .getProcessEngineConfiguration()
        .getCommandExecutorTxRequired()
        .execute(new Command<Object>() {
          public Object execute(CommandContext commandContext) {
            DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
            dbSqlSession.dbSchemaDrop();
            dbSqlSession.dbSchemaCreate();
            dbSqlSession.dbCreateHistoryLevel();
            return null;
          }
        });

      throw new AssertionError(outputMessage.toString());
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

  private static class InteruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InteruptTask(Thread thread) {
      this.thread = thread;
    }
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

  public static ProcessEngine getProcessEngine(String configurationResource) {
    ProcessEngine processEngine = processEngines.get(configurationResource);
    if (processEngine==null) {
      log.fine("==== BUILDING PROCESS ENGINE ========================================================================");
      processEngine = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource(configurationResource)
        .buildProcessEngine();
      log.fine("==== PROCESS ENGINE CREATED =========================================================================");
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
            DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
            dbSqlSession.executeMandatorySchemaResource("create", "engine");
            dbSqlSession.executeMandatorySchemaResource("create", "history");
            dbSqlSession.executeMandatorySchemaResource("create", "identity");
            dbSqlSession.executeMandatorySchemaResource("create", "case.engine");
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
         DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
         PropertyEntity historyLevelProperty = dbSqlSession.selectById(PropertyEntity.class, "historyLevel");
         if (historyLevelProperty != null) {
           if (processEngineConfiguration.getHistoryLevel() != new Integer(historyLevelProperty.getValue())) {
             historyLevelProperty.setValue(Integer.toString(processEngineConfiguration.getHistoryLevel()));
             dbSqlSession.merge(historyLevelProperty);
           }
         } else {
           commandContext.getDbSqlSession().dbCreateHistoryLevel();
         }
         return null;
       }
      });
  }

  public static void deleteHistoryLevel(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
       public Object execute(CommandContext commandContext) {
         DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
         PropertyEntity historyLevelProperty = dbSqlSession.selectById(PropertyEntity.class, "historyLevel");
         if (historyLevelProperty != null) {
           dbSqlSession.delete(historyLevelProperty);
         }
         return null;
       }
      });
  }

}

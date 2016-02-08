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
package org.camunda.qa.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugin;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.IoUtil;

/**
 * @author Daniel Meyer
 *
 */
public class EnsureCleanDbPlugin implements BpmPlatformPlugin {

  public static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY",
    "ACT_RU_METER_LOG"
  );

  private AtomicInteger counter = new AtomicInteger();

  @Override
  public void postProcessApplicationDeploy(ProcessApplicationInterface processApplication) {
    counter.incrementAndGet();
  }

  @SuppressWarnings("resource")
  @Override
  public void postProcessApplicationUndeploy(ProcessApplicationInterface processApplication) {

    // some tests deploy multiple PAs. => only check for clean DB after last PA is undeployed
    if(counter.decrementAndGet() == 0) {

      final ProcessEngine defaultProcessEngine = BpmPlatform.getDefaultProcessEngine();
      try {
        System.out.println("Ensure cleanup after integration test ");

        String cacheMessage = TestHelper.assertAndEnsureCleanDeploymentCache(defaultProcessEngine, false);
        String dbMessage = assertAndEnsureCleanDb(defaultProcessEngine, false);
        String paRegistrationMessage = TestHelper.assertAndEnsureNoProcessApplicationsRegistered(defaultProcessEngine);


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

        if(message.length() > 0) {
          System.err.println(message);
          File tmpFile = File.createTempFile("engine-IT-db-not-clean-", ".txt");
          if(!tmpFile.exists()) {
            tmpFile.createNewFile();
          }

          FileOutputStream fos = new FileOutputStream(tmpFile);
          BufferedOutputStream out = new BufferedOutputStream(fos);
          out.write(message.toString().getBytes());
          out.flush();
          out.close();
        }

      }
      catch(Throwable e) {
        System.err.println("Could not clean DB:");
        e.printStackTrace();
      }
    }

  }

  public static String assertAndEnsureCleanDb(ProcessEngine processEngine, boolean fail) {
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    String databaseTablePrefix = processEngineConfiguration.getDatabaseTablePrefix().trim();

    // clear user operation log in case some operations are
    // executed with an authenticated user
    TestHelper.clearUserOperationLog(processEngineConfiguration);

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
    }

    return outputMessage.toString();
  }

}

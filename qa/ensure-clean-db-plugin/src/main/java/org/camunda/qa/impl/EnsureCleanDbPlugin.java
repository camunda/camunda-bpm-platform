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
package org.camunda.qa.impl;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugin;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.management.DatabasePurgeReport;
import org.camunda.bpm.engine.impl.management.PurgeReport;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.CachePurgeReport;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel Meyer
 * @author Christopher Zell
 */
public class EnsureCleanDbPlugin implements BpmPlatformPlugin {

  protected static final String DATABASE_NOT_CLEAN = "Database was not clean!\n";
  protected static final String CACHE_IS_NOT_CLEAN = "Cache was not clean!\n";
  protected Logger logger = Logger.getLogger(EnsureCleanDbPlugin.class.getName());

  private AtomicInteger counter = new AtomicInteger();

  @Override
  public void postProcessApplicationDeploy(ProcessApplicationInterface processApplication) {
    counter.incrementAndGet();
  }

  @SuppressWarnings("resource")
  @Override
  public void postProcessApplicationUndeploy(ProcessApplicationInterface processApplication) {
    // some tests deploy multiple PAs. => only clean DB after last PA is undeployed
    // if the deployment fails for example during parsing the deployment counter was not incremented
    // so we have to check if the counter is already zero otherwise we go into the negative values
    // best example is TestWarDeploymentWithBrokenBpmnXml in integration-test-engine test suite
    if(counter.get() == 0 || counter.decrementAndGet() == 0) {

      final ProcessEngine defaultProcessEngine = BpmPlatform.getDefaultProcessEngine();
      try {
        logger.log(Level.INFO, "=== Ensure Clean Database ===");
        ManagementServiceImpl managementService = (ManagementServiceImpl) defaultProcessEngine.getManagementService();
        PurgeReport report = managementService.purge();

        if (report.isEmpty()) {
          logger.log(Level.INFO, "Clean DB and cache.");
        } else {
          StringBuilder builder = new StringBuilder();

          DatabasePurgeReport databasePurgeReport = report.getDatabasePurgeReport();
          if (!databasePurgeReport.isEmpty()) {
            builder.append(DATABASE_NOT_CLEAN).append(databasePurgeReport.getPurgeReportAsString());
          }

          CachePurgeReport cachePurgeReport = report.getCachePurgeReport();
          if (!cachePurgeReport.isEmpty()) {
            builder.append(CACHE_IS_NOT_CLEAN).append(cachePurgeReport.getPurgeReportAsString());
          }
          logger.log(Level.INFO, builder.toString());
        }
      }
      catch(Throwable e) {
        logger.log(Level.SEVERE, "Could not clean DB:", e);
      }
    }

  }
}

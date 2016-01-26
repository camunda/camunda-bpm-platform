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
package org.camunda.bpm.cockpit.plugin.test;

import java.util.List;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.impl.DefaultCockpitRuntimeDelegate;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.LogUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

/**
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractCockpitPluginTest {

  private static TestCockpitRuntimeDelegate RUNTIME_DELEGATE = new TestCockpitRuntimeDelegate();

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();

    // this ensures that mybatis uses the jdk logging
    LogFactory.useJdkLogging();
    // with an upgrade of mybatis, this might have to become org.mybatis.generator.logging.LogFactory.forceJavaLogging();
  }

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @BeforeClass
  public static void beforeClass() {
    Cockpit.setCockpitRuntimeDelegate(RUNTIME_DELEGATE);
  }

  @AfterClass
  public static void afterClass() {
    Cockpit.setCockpitRuntimeDelegate(null);
  }

  @Before
  public void before() {
    RUNTIME_DELEGATE.ENGINE = getProcessEngine();
  }

  @After
  public void after() {
    RUNTIME_DELEGATE.ENGINE = null;
  }

  public ProcessEngine getProcessEngine() {
    return processEngineRule.getProcessEngine();
  }

  protected CommandExecutor getCommandExecutor() {
    return Cockpit.getCommandExecutor("default");
  }

  protected QueryService getQueryService() {
    return Cockpit.getQueryService("default");
  }

  public void executeAvailableJobs() {
    ManagementService managementService = getProcessEngine().getManagementService();
    List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();

    if (jobs.isEmpty()) {
      return;
    }

    for (Job job : jobs) {
      try {
        managementService.executeJob(job.getId());
      } catch (Exception e) {};
    }

    executeAvailableJobs();
  }

  private static class TestCockpitRuntimeDelegate extends DefaultCockpitRuntimeDelegate {

    public ProcessEngine ENGINE;

    @Override
    public ProcessEngine getProcessEngine(String processEngineName) {

      // always return default engine for plugin tests
      return ENGINE;
    }
  }
}

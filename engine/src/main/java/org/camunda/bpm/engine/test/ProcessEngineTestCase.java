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
package org.camunda.bpm.engine.test;

import java.io.FileNotFoundException;
import java.util.Date;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.test.ProcessEngineAssert;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;

import junit.framework.TestCase;


/** Convenience for ProcessEngine and services initialization in the form of a JUnit base class.
 *
 * <p>Usage: <code>public class YourTest extends ProcessEngineTestCase</code></p>
 *
 * <p>The ProcessEngine and the services available to subclasses through protected member fields.
 * The processEngine will be initialized by default with the camunda.cfg.xml resource
 * on the classpath.  To specify a different configuration file, override the
 * {@link #getConfigurationResource()} method.
 * Process engines will be cached statically.  The first time the setUp is called for a given
 * configuration resource, the process engine will be constructed.</p>
 *
 * <p>You can declare a deployment with the {@link Deployment} annotation.
 * This base class will make sure that this deployment gets deployed in the
 * setUp and {@link RepositoryService#deleteDeploymentCascade(String, boolean) cascade deleted}
 * in the tearDown.
 * </p>
 *
 * <p>This class also lets you {@link #setCurrentTime(Date) set the current time used by the
 * process engine}. This can be handy to control the exact time that is used by the engine
 * in order to verify e.g. e.g. due dates of timers.  Or start, end and duration times
 * in the history service.  In the tearDown, the internal clock will automatically be
 * reset to use the current system time rather then the time that was set during
 * a test method.  In other words, you don't have to clean up your own time messing mess ;-)
 * </p>
 *
 * @author Tom Baeyens
 * @author Falko Menge (camunda)
 */
public class ProcessEngineTestCase extends TestCase {

  protected String configurationResource = "camunda.cfg.xml";
  protected String configurationResourceCompat = "activiti.cfg.xml";
  protected String deploymentId = null;

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  @Deprecated protected HistoryService historicDataService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected FormService formService;
  protected FilterService filterService;
  protected AuthorizationService authorizationService;
  protected CaseService caseService;

  protected boolean skipTest = false;

  /** uses 'camunda.cfg.xml' as it's configuration resource */
  public ProcessEngineTestCase() {
  }

  public void assertProcessEnded(final String processInstanceId) {
    ProcessEngineAssert.assertProcessEnded(processEngine, processInstanceId);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    if (processEngine==null) {
      initializeProcessEngine();
      initializeServices();
    }

    boolean hasRequiredHistoryLevel = TestHelper.annotationRequiredHistoryLevelCheck(processEngine, getClass(), getName());
    // ignore test case when current history level is too low
    skipTest = !hasRequiredHistoryLevel;

    if (!skipTest) {
      deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());
    }
  }

  @Override
  protected void runTest() throws Throwable {
    if (!skipTest) {
      super.runTest();
    }
  }

  protected void initializeProcessEngine() {
    try {
      processEngine = TestHelper.getProcessEngine(getConfigurationResource());
    } catch (RuntimeException ex) {
      if (ex.getCause() != null && ex.getCause() instanceof FileNotFoundException) {
        processEngine = ProcessEngineConfiguration
            .createProcessEngineConfigurationFromResource(configurationResourceCompat)
            .buildProcessEngine();
      } else {
        throw ex;
      }
    }
  }

  protected void initializeServices() {
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historicDataService = processEngine.getHistoryService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
    formService = processEngine.getFormService();
    filterService = processEngine.getFilterService();
    authorizationService = processEngine.getAuthorizationService();
    caseService = processEngine.getCaseService();
  }

  @Override
  protected void tearDown() throws Exception {
    TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());

    ClockUtil.reset();

    super.tearDown();
  }

  public static void closeProcessEngines() {
    TestHelper.closeProcessEngines();
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

}

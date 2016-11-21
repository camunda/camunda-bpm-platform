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
package org.camunda.bpm.example.invoice;

import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.fileValue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Logger;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * Process Application exposing this application's resources the process engine.
 */
@ProcessApplication
public class InvoiceProcessApplication extends ServletProcessApplication {

  private static final Logger LOGGER = Logger.getLogger(InvoiceProcessApplication.class.getName());

  /**
   * In a @PostDeploy Hook you can interact with the process engine and access
   * the processes the application has deployed.
   */
  @PostDeploy
  public void startFirstProcess(ProcessEngine processEngine) {
    createUsers(processEngine);

    //enable metric reporting
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    processEngineConfiguration.setDbMetricsReporterActivate(true);
    processEngineConfiguration.getDbMetricsReporter().setReporterId("REPORTER");

    startProcessInstances(processEngine, "invoice", 1);
    startProcessInstances(processEngine, "invoice", null);

    //disable reporting
    processEngineConfiguration.setDbMetricsReporterActivate(false);
  }

  @Override
  public void createDeployment(String processArchiveName, DeploymentBuilder deploymentBuilder) {
    ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getProcessEngine("default");

    // Hack: deploy the first version of the invoice process once before the process application
    //   is deployed the first time
    if (processEngine != null) {

      RepositoryService repositoryService = processEngine.getRepositoryService();

      if (!isProcessDeployed(repositoryService, "invoice")) {
        ClassLoader classLoader = getProcessApplicationClassloader();

        repositoryService.createDeployment(this.getReference())
          .addInputStream("invoice.v1.bpmn", classLoader.getResourceAsStream("invoice.v1.bpmn"))
          .addInputStream("invoiceBusinessDecisions.dmn", classLoader.getResourceAsStream("invoiceBusinessDecisions.dmn"))
          .addInputStream("review-invoice.cmmn", classLoader.getResourceAsStream("review-invoice.cmmn"))
          .deploy();
      }
    }
  }

  protected boolean isProcessDeployed(RepositoryService repositoryService, String key) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionKey("invoice").count() > 0;
  }

  private void startProcessInstances(ProcessEngine processEngine, String processDefinitionKey, Integer version) {

    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    ProcessDefinitionQuery processDefinitionQuery = processEngine
      .getRepositoryService()
      .createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey);

    if (version != null) {
      processDefinitionQuery.processDefinitionVersion(version);
    }
    else {
      processDefinitionQuery.latestVersion();
    }

    ProcessDefinition processDefinition = processDefinitionQuery.singleResult();

    InputStream invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");

    long numberOfRunningProcessInstances = processEngine.getRuntimeService().createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).count();

    if (numberOfRunningProcessInstances == 0) { // start three process instances

      LOGGER.info("Start 3 instances of " + processDefinition.getName() + ", version " + processDefinition.getVersion());
      // process instance 1
      processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId(), createVariables()
          .putValue("creditor", "Great Pizza for Everyone Inc.")
          .putValue("amount", 30.00d)
          .putValue("invoiceCategory", "Travel Expenses")
          .putValue("invoiceNumber", "GPFE-23232323")
          .putValue("invoiceDocument", fileValue("invoice.pdf")
              .file(invoiceInputStream)
              .mimeType("application/pdf")
              .create()));

      IoUtil.closeSilently(invoiceInputStream);
      invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");
      processEngineConfiguration.getDbMetricsReporter().reportNow();

      // process instance 2
      try {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -14);
        ClockUtil.setCurrentTime(calendar.getTime());

        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId(), createVariables()
            .putValue("creditor", "Bobby's Office Supplies")
            .putValue("amount", 900.00d)
            .putValue("invoiceCategory", "Misc")
            .putValue("invoiceNumber", "BOS-43934")
            .putValue("invoiceDocument", fileValue("invoice.pdf")
                .file(invoiceInputStream)
                .mimeType("application/pdf")
                .create()));

        processEngineConfiguration.getDbMetricsReporter().reportNow();
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        ClockUtil.setCurrentTime(calendar.getTime());

        processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(Groups.CAMUNDA_ADMIN));
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).singleResult();
        processEngine.getTaskService().claim(task.getId(), "demo");
        processEngine.getTaskService().complete(task.getId(), createVariables().putValue("approved", true));
      }
      finally{
        processEngineConfiguration.getDbMetricsReporter().reportNow();
        ClockUtil.reset();
        processEngine.getIdentityService().clearAuthentication();
      }

      IoUtil.closeSilently(invoiceInputStream);
      invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");

      // process instance 3
      try {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        ClockUtil.setCurrentTime(calendar.getTime());

        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId(), createVariables()
            .putValue("creditor", "Papa Steve's all you can eat")
            .putValue("amount", 10.99d)
            .putValue("invoiceCategory", "Travel Expenses")
            .putValue("invoiceNumber", "PSACE-5342")
            .putValue("invoiceDocument", fileValue("invoice.pdf")
                .file(invoiceInputStream)
                .mimeType("application/pdf")
                .create()));

        processEngineConfiguration.getDbMetricsReporter().reportNow();
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        ClockUtil.setCurrentTime(calendar.getTime());

        processEngine.getIdentityService().setAuthenticatedUserId("mary");
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).singleResult();
        processEngine.getTaskService().createComment(null, pi.getId(), "I cannot approve this invoice: the amount is missing.\n\n Could you please provide the amount?");
        processEngine.getTaskService().complete(task.getId(), createVariables().putValue("approved", false));
      }
      finally{
        processEngineConfiguration.getDbMetricsReporter().reportNow();
        ClockUtil.reset();
        processEngine.getIdentityService().clearAuthentication();
      }
    } else {
      LOGGER.info("No new instances of " + processDefinition.getName()
          + " version " + processDefinition.getVersion()
          + " started, there are " + numberOfRunningProcessInstances + " instances running");
    }
  }

  private void createUsers(ProcessEngine processEngine) {

    // create demo users
    new DemoDataGenerator().createUsers(processEngine);
  }
}

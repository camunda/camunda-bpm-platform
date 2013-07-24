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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;

/**
 * Process Application exposing this application's resources the process engine.
 */
@ProcessApplication
public class InvoiceProcessApplication extends ServletProcessApplication {

  /**
   * In a @PostDeploy Hook you can interact with the process engine and access
   * the processes the application has deployed.
   */
  @PostDeploy
  public void startFirstProcess(ProcessEngine processEngine) {

    createUsers(processEngine);
    startProcessInstance(processEngine);
  }

  private void startProcessInstance(ProcessEngine processEngine) {
    // start an initial demo process.

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("creditor", "Great Pizzas for Everyone Inc.");
    variables.put("amount", "30$");
    variables.put("invoiceNumber", "GPFE-23232323");

    processEngine.getRuntimeService().startProcessInstanceByKey("invoice", variables);
  }

  private void createUsers(ProcessEngine processEngine) {

    // create demo users
    new DemoDataGenerator().createUsers(processEngine);
  }
}

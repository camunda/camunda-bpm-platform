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
package org.camunda.bpm.pa.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Daniel Meyer
 *
 */
public class CreateInvoiceDataListener implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {
    InvoiceData invoiceData = new InvoiceData();
    invoiceData.setAmount((String) execution.getVariable("amount"));
    invoiceData.setCreditor((String) execution.getVariable("creditor"));
    invoiceData.setDuedate(new Date());
    invoiceData.setPriority(10);

    execution.setVariable("invoiceData", invoiceData);

    Map<String,String> potentialApprovers = new HashMap<String, String>();
    potentialApprovers.put("demo", "Demo User");
    potentialApprovers.put("mary", "Mary Anne");
    potentialApprovers.put("peter", "Peter Meter");

    execution.setVariable("potentialApprovers", Variables.objectValue(potentialApprovers).serializationDataFormat("application/json"));
  }

}

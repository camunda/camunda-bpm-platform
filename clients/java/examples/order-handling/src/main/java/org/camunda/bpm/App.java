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
package org.camunda.bpm;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;

import java.util.HashMap;
import java.util.Map;

public class App {

  public static void main(String... args) {
    // bootstrap the client
    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl("http://localhost:8080/engine-rest")
      .asyncResponseTimeout(1000)
      .build();

    // subscribe to the topic
    client.subscribe("invoiceCreator")
      .handler((externalTask, externalTaskService) -> {

        // instantiate an invoice object
        Invoice invoice = new Invoice("A123");

        // create an object typed variable with the serialization format XML
        ObjectValue invoiceValue = Variables
          .objectValue(invoice)
          .serializationDataFormat("application/xml")
          .create();

        // add the invoice object and its id to a map
        Map<String, Object> variables = new HashMap<>();
        variables.put("invoiceId", invoice.id);
        variables.put("invoice", invoiceValue);

        // select the scope of the variables
        boolean isRandomSample = Math.random() <= 0.5;
        if (isRandomSample) {
          externalTaskService.complete(externalTask, variables);
        } else {
          externalTaskService.complete(externalTask, null, variables);
        }

        System.out.println("The External Task " + externalTask.getId() +
          " has been completed!");

      }).open();
  }

}
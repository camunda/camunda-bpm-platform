package org.camunda.bpm;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;

import java.util.HashMap;
import java.util.Map;

public class App {

  public static void main(String... args) throws InterruptedException {
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
        variables.put("invoice", invoice);

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

    Thread.sleep(1000 * 60 * 5);
  }

}
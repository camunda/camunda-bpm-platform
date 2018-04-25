package org.camunda.bpm;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;

import java.util.Collections;
import java.util.Map;


class Invoice {

  public String invoiceId;

  public Invoice(String invoiceId) {
    this.invoiceId = invoiceId;
  }

}

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

        // add the invoice object to a map
        Map<String, Object> variablesMap =
          Collections.singletonMap("invoice", invoiceValue);

        try {
          if (Math.random() <= 0.5) {
            // complete the external task with process variables
            externalTaskService.complete(externalTask, variablesMap);
          } else {
            // complete the external task with local variables
            externalTaskService.complete(externalTask, null, variablesMap);
          }
        } catch (Exception ignored) { }

        System.out.println("The External Task " + externalTask.getId() +
          " has been completed!");

      }).open();

    Thread.sleep(1000 * 60 * 5);
  }

}
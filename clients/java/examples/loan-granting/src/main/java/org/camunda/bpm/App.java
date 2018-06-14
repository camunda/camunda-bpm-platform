package org.camunda.bpm;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class App {

  public static void main(String... args) throws InterruptedException {
    // bootstrap the client
    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl("http://localhost:8080/engine-rest")
      .build();

    // subscribe to the topic
    client.subscribe("creditScoreChecker")
      .lockDuration(1000)
      .handler((externalTask, externalTaskService) -> {

        // retrieve a variable from the Workflow Engine
        int defaultScore = externalTask.getVariable("defaultScore");

        List<Integer> creditScores = new ArrayList<>(Arrays.asList(defaultScore, 9, 1, 4, 10));

        // create an object typed variable
        ObjectValue creditScoresObject = Variables
          .objectValue(creditScores)
          .create();

        // complete the external task
        externalTaskService.complete(externalTask,
          Collections.singletonMap("creditScores", creditScoresObject));

        System.out.println("The External Task " + externalTask.getId() + " has been completed!");

      }).open();
  }

}
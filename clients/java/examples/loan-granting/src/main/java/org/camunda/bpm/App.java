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
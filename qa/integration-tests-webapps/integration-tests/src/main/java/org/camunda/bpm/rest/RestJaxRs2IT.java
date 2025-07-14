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
package org.camunda.bpm.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import org.camunda.bpm.AbstractWebIntegrationTest;
import org.junit.Before;
import org.junit.Test;

public class RestJaxRs2IT extends AbstractWebIntegrationTest {

  private static final String ENGINE_DEFAULT_PATH = "engine/default";
  private static final String FETCH_AND_LOCK_PATH = ENGINE_DEFAULT_PATH + "/external-task/fetchAndLock";

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getRestCtxPath());
  }

  @Test(timeout=10000)
  public void shouldUseJaxRs2Artifact() {
    Map<String, Object> payload = new HashMap<>();
    payload.put("workerId", "aWorkerId");
    payload.put("asyncResponseTimeout", 1000 * 60 * 30 + 1);

    HttpResponse<JsonNode> response = Unirest.post(appBasePath + FETCH_AND_LOCK_PATH)
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .body(payload)
        .asJson();

    assertEquals(400, response.getStatus());
    String responseMessage = response.getBody().getObject().get("message").toString();
    assertEquals("The asynchronous response timeout cannot be set to a value greater than 1800000 milliseconds", responseMessage);
  }

  @Test
  public void shouldPerform500ConcurrentRequests() throws InterruptedException, ExecutionException {
    Callable<String> performRequest = () -> {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("workerId", "aWorkerId");
      requestBody.put("asyncResponseTimeout", 1000);

      HttpResponse<String> response = Unirest.post(appBasePath + FETCH_AND_LOCK_PATH)
          .header("Content-Type", "application/json")
          .body(requestBody)
          .asString();

      return response.getBody();
    };

    int requestsCount = 500;
    ExecutorService service = Executors.newFixedThreadPool(requestsCount);

    try {
      List<Callable<String>> requests = new ArrayList<>();
      for (int i = 0; i < requestsCount; i++) {
        requests.add(performRequest);
      }

      List<Future<String>> futures = service.invokeAll(requests);
      service.shutdown();
      boolean terminated = service.awaitTermination(1, TimeUnit.HOURS);
      if (!terminated) {
        service.shutdownNow();
      }

      for (Future<String> future : futures) {
        assertEquals("[]", future.get());
      }
    } finally {
      if (!service.isShutdown()) {
        service.shutdown();
      }
    }
  }

}

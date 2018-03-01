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
package org.camunda.bpm.client.impl;

import org.camunda.bpm.client.CamundaClient;
import org.camunda.bpm.client.CamundaClientException;
import org.camunda.bpm.client.WorkerSubscription;
import org.camunda.bpm.client.WorkerSubscriptionBuilder;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Tassilo Weidner
 */
public class SubscriptionTest {

  private static String ENDPOINT_URL = "http://localhost:8080/engine-rest";
  private static String TOPIC_NAME = "Address Validation";

  @Test
  public void shouldSubscribeToTopicsWithLockDuration() {
    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(ENDPOINT_URL)
      .build();

    for (int i = 0; i < 10; i++) {
      WorkerSubscription workerSubscription = camundaClient.subscribe(TOPIC_NAME+i)
        .lockDuration(5000+i)
        .execute();

      WorkerSubscriptionImpl workerSubscriptionImpl = (WorkerSubscriptionImpl) workerSubscription;
      assertThat(workerSubscriptionImpl.getLockDuration(), is(5000L+i));
      assertThat(workerSubscriptionImpl.getTopicName(), is(TOPIC_NAME+i));
    }

    CamundaClientImpl camundaClientImpl = (CamundaClientImpl) camundaClient;
    WorkerManager workerManager = camundaClientImpl.getWorkerManager();
    assertThat(workerManager.getSubscriptions().size(), is(10));
  }

  @Test
  public void shouldThrowExceptionDueToTopicNameIsNull() {
    // given
    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(ENDPOINT_URL)
      .build();

    try {
      // when
      camundaClient.subscribe(null)
        .execute();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), Is.is("Topic name cannot be null"));
    }
  }

  @Test
  public void shouldThrowExceptionDueToLockDurationIsNotGreaterThanZero() {
    // given
    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(ENDPOINT_URL)
      .build();

    for (int i = -1; i < 2; i++) {
      try {
        WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(TOPIC_NAME);

        // when
        if (i <= 0) {
          workerSubscriptionBuilder.lockDuration(i);
        }

        workerSubscriptionBuilder.execute();

        fail("No CamundaClientException thrown!");
      } catch (CamundaClientException e) {
        // then
        assertThat(e.getMessage(), Is.is("Lock duration is not greater than 0"));
      }
    }
  }

}

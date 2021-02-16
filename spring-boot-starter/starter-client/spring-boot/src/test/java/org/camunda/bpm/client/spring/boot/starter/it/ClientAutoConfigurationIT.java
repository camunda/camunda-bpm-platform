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
package org.camunda.bpm.client.spring.boot.starter.it;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientAutoConfigurationIT {

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  protected ExternalTaskClient externalTaskClient;

  @Autowired
  protected List<SpringTopicSubscription> topicSubscriptions;

  @Test
  public void startup() {
    assertThat(topicSubscriptions.size()).isEqualTo(2);
    assertThat(topicSubscriptions)
        .extracting("topicName", "autoOpen", "businessKey", "lockDuration", "processDefinitionKey")
        .containsExactlyInAnyOrder(
            tuple("topic-one", false, null, 33L, null),
            tuple("topic-two", false, "business-key", null, "proc-def-key"));
  }

}

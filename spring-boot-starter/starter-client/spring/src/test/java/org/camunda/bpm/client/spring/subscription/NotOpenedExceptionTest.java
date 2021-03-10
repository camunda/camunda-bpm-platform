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
package org.camunda.bpm.client.spring.subscription;

import org.camunda.bpm.client.spring.exception.NotOpenedException;
import org.camunda.bpm.client.spring.MockedTest;
import org.camunda.bpm.client.spring.subscription.configuration.NotOpenedExceptionConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NotOpenedExceptionTest {

  @Before
  public void setup() {
    MockedTest.mockClient();
  }

  @After
  public void reset() {
    MockedTest.close();
  }

  @Test
  public void shouldThrowException() {
    Class<?> clazz = NotOpenedExceptionConfiguration.class;
    assertThatThrownBy(() -> new AnnotationConfigApplicationContext(clazz))
        .isInstanceOf(NotOpenedException.class)
        .hasMessageContaining("TASK/CLIENT/SPRING-02009 Subscription with topic name " +
            "'topic-name' has yet not  been opened");
  }

}
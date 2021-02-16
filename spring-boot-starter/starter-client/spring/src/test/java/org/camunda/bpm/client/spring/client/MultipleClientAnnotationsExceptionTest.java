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
package org.camunda.bpm.client.spring.client;

import org.camunda.bpm.client.spring.client.configuration.AnotherSimpleClientConfiguration;
import org.camunda.bpm.client.spring.configuration.SimpleClientConfiguration;
import org.camunda.bpm.client.spring.exception.SpringExternalTaskClientException;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MultipleClientAnnotationsExceptionTest {

  @Test
  public void shouldThrowException() {
    Class<?> clazzOne = SimpleClientConfiguration.class;
    Class<?> clazzTwo = AnotherSimpleClientConfiguration.class;
    assertThatThrownBy(() -> new AnnotationConfigApplicationContext(clazzOne, clazzTwo))
        .isInstanceOf(SpringExternalTaskClientException.class)
        .hasMessageContaining("TASK/CLIENT/SPRING-01007 Multiple matching client annotation candidates");
  }

}
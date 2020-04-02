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
package org.camunda.bpm.spring.boot.starter.actuator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

@RunWith(MockitoJUnitRunner.class)
public class ProcessEngineHealthIndicatorTest {

  private static final String PROCESS_ENGINE_NAME = "process engine name";

  @Mock
  private ProcessEngine processEngine;

  @Test(expected = IllegalArgumentException.class)
  public void nullTest() {
    new ProcessEngineHealthIndicator(null);
  }

  @Test
  public void upTest() {
    when(processEngine.getName()).thenReturn(PROCESS_ENGINE_NAME);
    Health health = new ProcessEngineHealthIndicator(processEngine).health();
    assertEquals(Status.UP, health.getStatus());
    assertEquals(PROCESS_ENGINE_NAME, health.getDetails().get("name"));
  }
}

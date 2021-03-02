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
package org.camunda.bpm.client.backoff;

import org.assertj.core.util.Lists;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nikola Koevski
 */
public class ExponentialBackoffStrategyTest {

  protected ExponentialBackoffStrategy backoffStrategy;

  @Before
  public void setup() {
    backoffStrategy = new ExponentialBackoffStrategy();
  }

  @Test
  public void shouldAdvanceBackoffStrategy() {
    // given
    long initialWaitingTime = backoffStrategy.calculateBackoffTime();
    assertThat(initialWaitingTime).isEqualTo(0L);

    // when
    // in consecutive iterations, no external tasks are available
    backoffStrategy.reconfigure(Lists.emptyList());
    long waitingTime1 = backoffStrategy.calculateBackoffTime();
    backoffStrategy.reconfigure(Lists.emptyList());
    long waitingTime2 = backoffStrategy.calculateBackoffTime();

    // then
    assertThat(waitingTime1).isEqualTo(500L);
    assertThat(waitingTime2).isEqualTo(1000L);
  }

  @Test
  public void shouldResetBackoffStrategy() {
    // given
    backoffStrategy.reconfigure(Lists.emptyList());
    long waitingTime1 = backoffStrategy.calculateBackoffTime();
    assertThat(waitingTime1).isEqualTo(500L);

    // when
    // a not-empty response is received
    backoffStrategy.reconfigure(Lists.newArrayList(new ExternalTaskImpl()));

    // then
    long waitingTime2 = backoffStrategy.calculateBackoffTime();
    assertThat(waitingTime2).isEqualTo(0L);
  }

  @Test
  public void shouldCapWaitingTime() {
    // given
    long waitingTime = backoffStrategy.calculateBackoffTime();
    assertThat(waitingTime).isEqualTo(0L);

    // when
    // reach maximum waiting time
    for (int i=0; i<8; i++) {
      backoffStrategy.reconfigure(Lists.emptyList());
    }

    // then
    waitingTime = backoffStrategy.calculateBackoffTime();
    assertThat(waitingTime).isEqualTo(60000L);
  }
}

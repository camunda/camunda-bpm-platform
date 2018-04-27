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

package org.camunda.bpm.client.backoff;

import org.assertj.core.util.Lists;
import org.camunda.bpm.client.backoff.impl.ExponentialBackoffStrategy;
import org.camunda.bpm.client.task.ExternalTask;
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
    // no external tasks available
    backoffStrategy.reconfigure(Lists.emptyList());
    long waitingTime1 = backoffStrategy.calculateBackoffTime();
    backoffStrategy.reconfigure(Lists.emptyList());
    long waitingTime2 = backoffStrategy.calculateBackoffTime();

    // then
    assertThat(waitingTime1).isGreaterThan(0L);
    assertThat(waitingTime2).isGreaterThan(waitingTime1);
  }

  @Test
  public void shouldResetBackoffStrategy() {
    // given
    backoffStrategy.reconfigure(Lists.emptyList());
    long waitingTime = backoffStrategy.calculateBackoffTime();
    assertThat(waitingTime).isGreaterThan(0L);

    // when
    backoffStrategy.reconfigure(Lists.newArrayList(new ExternalTaskImpl()));

    // then
    long waitingTime2 = backoffStrategy.calculateBackoffTime();
    assertThat(waitingTime2).isEqualTo(0L);
  }
}

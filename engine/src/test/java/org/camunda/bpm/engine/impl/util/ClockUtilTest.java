/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class ClockUtilTest {

  private static final long ONE_SECOND = 1000L;

  @Before
  public void setUp() throws Exception {
    ClockUtil.reset();
  }

  @AfterClass
  public static void resetClock() {
    ClockUtil.reset();
  }

  @Test
  public void nowShouldReturnCurrentTime() {
    assertThat(ClockUtil.now()).isCloseTo(new Date(), ONE_SECOND);
  }

  @Test
  public void getCurrentTimeShouldReturnSameValueAsNow() {
    assertThat(ClockUtil.getCurrentTime()).isCloseTo(ClockUtil.now(), ONE_SECOND);
  }

  @Test
  public void offsetShouldTravelInTime() {
    Duration duration = Duration.standardDays(2);
    Date target = new Date(new Date().getTime() + duration.getMillis());

    ClockUtil.offset(duration.getMillis());

    assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);
  }

  @Test
  public void setCurrentTimeShouldFreezeTime() {
    Duration duration = Duration.standardDays(2);
    Date target = new Date(new Date().getTime() + duration.getMillis());

    ClockUtil.setCurrentTime(target);

    assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);
  }

  @Test
  public void resetClockShouldResetToCurrentTime() {
    Duration duration = Duration.standardDays(2);
    Date target = new Date(new Date().getTime() + duration.getMillis());

    ClockUtil.offset(duration.getMillis());

    assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);

    assertThat(ClockUtil.resetClock()).isCloseTo(new Date(), ONE_SECOND);
    assertThat(ClockUtil.getCurrentTime()).isCloseTo(new Date(), ONE_SECOND);
  }

  @Test
  public void resetShouldResetToCurrentTime() {
    Duration duration = Duration.standardDays(2);
    Date target = new Date(new Date().getTime() + duration.getMillis());

    ClockUtil.offset(duration.getMillis());

    assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);

    ClockUtil.reset();

    assertThat(ClockUtil.now()).isCloseTo(new Date(), ONE_SECOND);
  }

  @Test
  public void timeShouldMoveOnAfterTravel() throws InterruptedException {
    Date now = new Date();
    Duration duration = Duration.standardDays(2);
    Date target = new Date(now.getTime() + duration.getMillis());

    ClockUtil.offset(duration.getMillis());

    assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);

    Thread.sleep(10000);

    assertThat(ClockUtil.now()).isCloseTo(new Date(target.getTime() + Duration.standardSeconds(10).getMillis()), ONE_SECOND);
  }

  @Test
  public void timeShouldFreezeWithSetCurrentTime() throws InterruptedException {
    Date now = new Date();
    Duration duration = Duration.standardDays(2);
    Date target = new Date(now.getTime() + duration.getMillis());
    ClockUtil.setCurrentTime(target);

    Thread.sleep(10000);

    assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);
  }
}

package org.camunda.bpm.engine.impl.util;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License atØ
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ClockUtilTest {

    private static final long ONE_SECOND = 1000L;

    @Before
    public void setUp() throws Exception {
        ClockUtil.resetClock();
    }

    @Test
    public void now_should_return_current_time() {
        assertThat(ClockUtil.now()).isCloseTo(new Date(), ONE_SECOND);
    }

    @Test
    public void getCurrentTime_should_return_same_value_as_now() {
        assertThat(ClockUtil.getCurrentTime()).isCloseTo(ClockUtil.now(), 1000);
    }

    @Test
    public void offset_should_travel_in_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);
    }

    @Test
    public void setCurrentTime_should_freeze_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.setCurrentTime(target);

        assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);
    }

    @Test
    public void resetClock_should_reset_to_current_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);

        assertThat(ClockUtil.resetClock()).isCloseTo(new Date(), ONE_SECOND);
        assertThat(ClockUtil.getCurrentTime()).isCloseTo(new Date(), ONE_SECOND);
    }

    @Test
    public void reset_should_reset_to_current_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);

        ClockUtil.reset();

        assertThat(ClockUtil.now()).isCloseTo(new Date(), ONE_SECOND);
    }

    @Test
    public void time_should_move_on_after_travel() throws InterruptedException {
        Date now = new Date();
        Duration duration = Duration.standardDays(2);
        Date target = new Date(now.getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);

        Thread.sleep(10000);

        assertThat(ClockUtil.now()).isCloseTo(new Date(target.getTime() + Duration.standardSeconds(10).getMillis()), ONE_SECOND);
    }

    @Test
    public void time_should_freeze_with_setCurrentTime() throws InterruptedException {
        Date now = new Date();
        Duration duration = Duration.standardDays(2);
        Date target = new Date(now.getTime() + duration.getMillis());
        ClockUtil.setCurrentTime(target);

        Thread.sleep(10000);

        assertThat(ClockUtil.now()).isCloseTo(target, ONE_SECOND);
    }
}

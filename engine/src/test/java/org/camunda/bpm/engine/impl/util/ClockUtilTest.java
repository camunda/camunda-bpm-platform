package org.camunda.bpm.engine.impl.util;

import org.joda.time.Duration;
import org.junit.Test;

import java.util.Date;

import static org.exparity.hamcrest.date.DateMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
public class ClockUtilTest {

    @Test
    public void now_should_return_current_time() {
        assertThat(ClockUtil.now(), sameSecondOfMinute(new Date()));
    }

    @Test
    public void getCurrentTime_should_return_same_value_as_now() {
        assertThat(ClockUtil.getCurrentTime(), is(ClockUtil.now()));
    }

    @Test
    public void offset_should_travel_in_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertTimesAreInSameSecond(ClockUtil.now(), target);
    }

    @Test
    public void setCurrentTime_should_freeze_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.setCurrentTime(target);

        assertTimesAreInSameSecond(ClockUtil.now(), target);
    }

    @Test
    public void resetClock_should_reset_to_current_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertTimesAreInSameSecond(ClockUtil.now(), target);

        assertTimesAreInSameSecond(ClockUtil.resetClock(), new Date());
        assertTimesAreInSameSecond(ClockUtil.getCurrentTime(), new Date());
    }

    @Test
    public void reset_should_reset_to_current_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertTimesAreInSameSecond(ClockUtil.now(), target);

        ClockUtil.reset();

        assertTimesAreInSameSecond(ClockUtil.getCurrentTime(), new Date());
    }

    @Test
    public void time_should_move_on_after_travel() throws InterruptedException {
        Date now = new Date();
        Duration duration = Duration.standardDays(2);
        Date target = new Date(now.getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertTimesAreInSameSecond(ClockUtil.now(), target);

        Thread.sleep(10000);

        assertTimesAreInSameSecond(ClockUtil.now(), new Date(target.getTime() + Duration.standardSeconds(10).getMillis()));
    }

    @Test
    public void time_should_freeze_with_setCurrentTime() throws InterruptedException {
        Date now = new Date();
        Duration duration = Duration.standardDays(2);
        Date target = new Date(now.getTime() + duration.getMillis());
        ClockUtil.setCurrentTime(target);

        Thread.sleep(10000);

        assertTimesAreInSameSecond(ClockUtil.now(), target);
    }

    private static void assertTimesAreInSameSecond(Date first, Date second) {
        assertThat(first, sameSecondOfMinute(second));
        assertThat(first, sameMinuteOfHour(second));
        assertThat(first, sameHourOfDay(second));
        assertThat(first, sameDayOfMonth(second));
        assertThat(first, sameMonthOfYear(second));
        assertThat(first, sameMonthOfYear(second));
    }
}

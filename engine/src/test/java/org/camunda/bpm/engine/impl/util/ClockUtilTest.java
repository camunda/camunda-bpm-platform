package org.camunda.bpm.engine.impl.util;

import org.joda.time.Duration;
import org.junit.Test;

import java.util.Date;

import static org.exparity.hamcrest.date.DateMatchers.sameSecondOfMinute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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

        assertThat(ClockUtil.now(), sameSecondOfMinute(target));
    }

    @Test
    public void setCurrentTime_should_freeze_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.setCurrentTime(target);

        assertThat(ClockUtil.now(), sameSecondOfMinute(target));
    }

    @Test
    public void resetClock_should_reset_to_current_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertThat(ClockUtil.now(), sameSecondOfMinute(target));

        assertThat(ClockUtil.resetClock(), sameSecondOfMinute(new Date()));
        assertThat(ClockUtil.getCurrentTime(), sameSecondOfMinute(new Date()));
    }

    @Test
    public void reset_should_reset_to_current_time() {
        Duration duration = Duration.standardDays(2);
        Date target = new Date(new Date().getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertThat(ClockUtil.now(), sameSecondOfMinute(target));

        ClockUtil.reset();

        assertThat(ClockUtil.getCurrentTime(), sameSecondOfMinute(new Date()));
    }

    @Test
    public void time_should_move_on_after_travel() throws InterruptedException {
        Date now = new Date();
        Duration duration = Duration.standardDays(2);
        Date target = new Date(now.getTime() + duration.getMillis());

        ClockUtil.offset(duration.getMillis());

        assertThat(ClockUtil.now(), sameSecondOfMinute(target));

        Thread.sleep(10000);

        assertThat(ClockUtil.now(), sameSecondOfMinute(new Date(target.getTime() + Duration.standardSeconds(10).getMillis())));
    }

    @Test
    public void time_should_freeze_with_setCurrentTime() throws InterruptedException {
        Date now = new Date();
        Duration duration = Duration.standardDays(2);
        Date target = new Date(now.getTime() + duration.getMillis());
        ClockUtil.setCurrentTime(target);


        Thread.sleep(10000);

        assertThat(ClockUtil.now(), sameSecondOfMinute(target));
    }
}
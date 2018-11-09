package org.camunda.bpm.engine.impl.util;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        Instant target = Instant.now().plus(2L, ChronoUnit.DAYS);

        ClockUtil.offset(Duration.between(Instant.now(), target));

        assertThat(ClockUtil.now(), sameSecondOfMinute(Date.from(target)));
    }

    @Test
    public void setCurrentTime_should_freeze_time() {
        Instant target = Instant.now().plus(2L, ChronoUnit.DAYS);

        ClockUtil.setCurrentTime(Date.from(target));

        assertThat(ClockUtil.now(), sameSecondOfMinute(Date.from(target)));
    }

    @Test
    public void resetClock_should_reset_to_current_time() {
        Instant target = Instant.now().plus(2L, ChronoUnit.DAYS);

        ClockUtil.offset(Duration.between(Instant.now(), target));

        assertThat(ClockUtil.now(), sameSecondOfMinute(Date.from(target)));

        assertThat(ClockUtil.resetClock(), sameSecondOfMinute(new Date()));
        assertThat(ClockUtil.getCurrentTime(), sameSecondOfMinute(new Date()));
    }

    @Test
    public void reset_should_reset_to_current_time() {
        Instant target = Instant.now().plus(2L, ChronoUnit.DAYS);

        ClockUtil.offset(Duration.between(Instant.now(), target));

        assertThat(ClockUtil.now(), sameSecondOfMinute(Date.from(target)));

        ClockUtil.reset();

        assertThat(ClockUtil.getCurrentTime(), sameSecondOfMinute(new Date()));
    }

    @Test
    public void time_should_move_on_after_travel() throws InterruptedException {
        Instant target = Instant.now().plus(2L, ChronoUnit.DAYS);

        ClockUtil.offset(Duration.between(Instant.now(), target));

        assertThat(ClockUtil.now(), sameSecondOfMinute(Date.from(target)));

        Thread.sleep(10000);

        assertThat(ClockUtil.now(), sameSecondOfMinute(Date.from(target.plusMillis(10000))));
    }

    @Test
    public void time_should_freeze_with_setCurrentTime() throws InterruptedException {
        Instant target = Instant.now().plus(2L, ChronoUnit.DAYS);

        ClockUtil.setCurrentTime(Date.from(target));

        Thread.sleep(10000);

        assertThat(ClockUtil.now(), sameSecondOfMinute(Date.from(target)));
    }
}
/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.test.api.mgmt.metrics;

import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import org.junit.Assert;
import org.junit.Test;

/**
 * Represents a test suite for the metrics interval query to check if the
 * timestamps are read in a correct time zone.
 *
 * This was a problem before the column MILLISECONDS_ was added.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MetricsIntervalTimezoneTest extends AbstractMetricsIntervalTest {

  @Test
  public void testTimestampIsInCorrectTimezone() {
    //given generated metric data started at DEFAULT_INTERVAL ends at 3 * DEFAULT_INTERVAL

    //when metric query is executed (hint last interval is returned as first)
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().limit(1).interval();

    //then metric interval time should be less than FIRST_INTERVAL + 3 * DEFAULT_INTERVAL
    long metricIntervalTime = metrics.get(0).getTimestamp().getTime();
    Assert.assertTrue(metricIntervalTime < firstInterval.plusMinutes(3 * DEFAULT_INTERVAL).getMillis());
    //and larger than first interval time, if not than we have a timezone problem
    Assert.assertTrue(metricIntervalTime > firstInterval.getMillis());

    //when current time is used and metric is reported
    Date currentTime = new Date();
    MetricsRegistry metricsRegistry = processEngineConfiguration.getMetricsRegistry();
    ClockUtil.setCurrentTime(currentTime);
    metricsRegistry.markOccurrence(ACTIVTY_INSTANCE_START, 1);
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    //then current time should be larger than metric interval time
    List<MetricIntervalValue> m2 = managementService.createMetricsQuery().limit(1).interval();
    Assert.assertTrue(m2.get(0).getTimestamp().getTime() < currentTime.getTime());
  }
}

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
package org.camunda.bpm.engine.test.api.mgmt.metrics;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.metrics.MetricsQueryImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MetricsIntervalTest extends AbstractMetricsIntervalTest {

  // LIMIT //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryLimit() {
    //since generating test data of 200 metrics will take a long time we check if the default values are set of the query
    //given metric query
    MetricsQueryImpl query = (MetricsQueryImpl) managementService.createMetricsQuery();

    //when no changes are made
    //then max results are 200, lastRow 201, offset 0, firstRow 1
    assertEquals(1, query.getFirstRow());
    assertEquals(0, query.getFirstResult());
    assertEquals(200, query.getMaxResults());
    assertEquals(201, query.getLastRow());
  }

  @Test
  public void testMeterQueryDecreaseLimit() {
    //given metric data

    //when query metric interval data with limit of 10 values
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().limit(10).interval();

    //then 10 values are returned
    assertEquals(10, metrics.size());
  }

  @Test
  public void testMeterQueryIncreaseLimit() {
    //given metric data

    // when/then
    // when query metric interval data with max results set to 1000
    assertThatThrownBy(() -> managementService.createMetricsQuery().limit(1000).interval())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Metrics interval query row limit can't be set larger than 200.");
  }

  // OFFSET //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryOffset() {
    //given metric data

    //when query metric interval data with offset of metrics count
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().offset(metricsCount).interval();

    //then 2 * metricsCount values are returned and highest interval is second last interval, since first 9 was skipped
    assertEquals(2 * metricsCount, metrics.size());
    assertEquals(firstInterval.plusMinutes(DEFAULT_INTERVAL).getMillis(), metrics.get(0).getTimestamp().getTime());
  }

  @Test
  public void testMeterQueryMaxOffset() {
    //given metric data

    //when query metric interval data with max offset
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().offset(Integer.MAX_VALUE).interval();

    //then 0 values are returned
    assertEquals(0, metrics.size());
  }

  // INTERVAL //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryDefaultInterval() {
    //given metric data

    //when query metric interval data with default values
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().interval();

    //then default interval is 900 s (15 minutes)
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    metrics.remove(0);
    for (MetricIntervalValue metric : metrics) {
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + DEFAULT_INTERVAL_MILLIS);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryCustomInterval() {
    //given metric data

    //when query metric interval data with custom time interval
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().interval(300);

    //then custom interval is 300 s (5 minutes)
    int interval = 5 * 60 * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    metrics.remove(0);
    for (MetricIntervalValue metric : metrics) {
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + interval);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  // WHERE REPORTER //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryDefaultIntervalWhereReporter() {
    //given metric data

    //when query metric interval data with reporter in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().reporter(REPORTER_ID).interval();

    //then result contains only metrics from given reporter, since it is the default it contains all
    assertEquals(3 * metricsCount, metrics.size());
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String reporter = metrics.get(0).getReporter();
    metrics.remove(0);
    for (MetricIntervalValue metric : metrics) {
      assertEquals(reporter, metric.getReporter());
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + DEFAULT_INTERVAL_MILLIS);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryDefaultIntervalWhereReporterNotExist() {
    //given metric data

    //when query metric interval data with not existing reporter in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().reporter("notExist").interval();

    //then result contains no metrics from given reporter
    assertEquals(0, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereReporter() {
    //given metric data and custom interval
    int interval = 5 * 60;

    //when query metric interval data with custom interval and reporter in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().reporter(REPORTER_ID).interval(interval);

    //then result contains only metrics from given reporter, since it is the default it contains all
    assertEquals(9 * metricsCount, metrics.size());
    interval = interval * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String reporter = metrics.get(0).getReporter();
    metrics.remove(0);
    for (MetricIntervalValue metric : metrics) {
      assertEquals(reporter, metric.getReporter());
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + interval);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryCustomIntervalWhereReporterNotExist() {
    //given metric data

    //when query metric interval data with custom interval and non existing reporter in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().reporter("notExist").interval(300);

    //then result contains no metrics from given reporter
    assertEquals(0, metrics.size());
  }

  // WHERE NAME //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryDefaultIntervalWhereName() {
    //given metric data

    //when query metric interval data with name in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().name(ACTIVTY_INSTANCE_START).interval();

    //then result contains only metrics with given name
    assertEquals(3, metrics.size());
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String name = metrics.get(0).getName();
    metrics.remove(0);
    for (MetricIntervalValue metric : metrics) {
      assertEquals(name, metric.getName());
      assertEquals(Metrics.FLOW_NODE_INSTANCES, metric.getName());
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + DEFAULT_INTERVAL_MILLIS);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryDefaultIntervalWhereNameNotExist() {
    //given metric data

    //when query metric interval data with non existing name in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().name("notExist").interval();

    //then result contains no metrics with given name
    assertEquals(0, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereName() {
    //given metric data and custom interval
    int interval = 5 * 60;

    //when query metric interval data with custom interval and name in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().name(ACTIVTY_INSTANCE_START).interval(interval);

    //then result contains only metrics with given name
    assertEquals(9, metrics.size());
    interval = interval * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String name = metrics.get(0).getName();
    metrics.remove(0);
    for (MetricIntervalValue metric : metrics) {
      assertEquals(name, metric.getName());
      assertEquals(Metrics.FLOW_NODE_INSTANCES, metric.getName());
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + interval);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryCustomIntervalWhereNameNotExist() {
    //given metric data

    //when query metric interval data with custom interval and non existing name in where clause
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().name("notExist").interval(300);

    //then result contains no metrics from given name
    assertEquals(0, metrics.size());
  }

  // START DATE //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryDefaultIntervalWhereStartDate() {
    //given metric data created for 14.9  min intervals

    //when query metric interval data with second last interval as start date in where clause
    //second last interval = start date = Jan 1, 1970 1:15:00 AM
    Date startDate = firstInterval.plusMinutes(DEFAULT_INTERVAL).toDate();
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().startDate(startDate).interval();

    //then result contains 18 entries since 9 different metrics are created
    //intervals Jan 1, 1970 1:15:00 AM and Jan 1, 1970 1:30:00 AM
    assertEquals(2 * metricsCount, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereStartDate() {
    //given metric data created for 15 min intervals

    //when query metric interval data with custom interval and second last interval as start date in where clause
    Date startDate = firstInterval.plusMinutes(DEFAULT_INTERVAL).toDate();
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().startDate(startDate).interval(300);

    //then result contains 4 intervals * the metrics count
    //15 20 25 30 35 40
    assertEquals(6 * metricsCount, metrics.size());
  }

  // END DATE //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryDefaultIntervalWhereEndDate() {
    //given metric data created for 15 min intervals

    //when query metric interval data with second interval as end date in where clause
    //second interval = first interval - default interval
    DateTime endDate = firstInterval.plusMinutes(DEFAULT_INTERVAL);
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().endDate(endDate.toDate()).interval();

    //then result contains one interval with entry for each metric
    //and end time is exclusive
    assertEquals(metricsCount, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereEndDate() {
    //given metric data created for 15 min intervals

    //when query metric interval data with custom interval and second interval as end date in where clause
    Date endDate = firstInterval.plusMinutes(DEFAULT_INTERVAL).toDate();
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().endDate(endDate).interval(300);

    //then result contains 3 * metrics count 3 interval before end time
    //endTime is exclusive which means the given date is not included in the result
    assertEquals(3 * metricsCount, metrics.size());
  }

  // START AND END DATE //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryDefaultIntervalWhereStartAndEndDate() {
    //given metric data created for 15 min intervals

    //when query metric interval data with start and end date in where clause
    DateTime endDate = firstInterval.plusMinutes(DEFAULT_INTERVAL);
    DateTime startDate = firstInterval;
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().startDate(startDate.toDate()).endDate(endDate.toDate()).interval();

    //then result contains 9 entries since 9 different metrics are created
    //and start date is inclusive and end date exclusive
    assertEquals(metricsCount, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereStartAndEndDate() {
    //given metric data created for 15 min intervals

    //when query metric interval data with custom interval, start and end date in where clause
    DateTime endDate = firstInterval.plusMinutes(DEFAULT_INTERVAL);
    DateTime startDate = firstInterval;
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().startDate(startDate.toDate()).endDate(endDate.toDate()).interval(300);

    //then result contains 27 entries since 9 different metrics are created
    //endTime is exclusive which means the given date is not included in the result
    assertEquals(3 * metricsCount, metrics.size());
  }

  // VALUE //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryDefaultIntervalCalculatedValue() {
    //given metric data created for 15 min intervals

    //when query metric interval data with custom interval, start and end date in where clause
    DateTime endDate = firstInterval.plusMinutes(DEFAULT_INTERVAL);
    DateTime startDate = firstInterval;
    MetricsQuery metricQuery = managementService.createMetricsQuery()
            .startDate(startDate.toDate())
            .endDate(endDate.toDate())
            .name(ACTIVTY_INSTANCE_START);
    List<MetricIntervalValue> metrics = metricQuery.interval();
    long sum = metricQuery.sum();

    //then result contains 1 entries
    //sum should be equal to the sum which is calculated by the metric query
    assertEquals(1, metrics.size());
    assertEquals(sum, metrics.get(0).getValue());
  }

  @Test
  public void testMeterQueryCustomIntervalCalculatedValue() {
    //given metric data created for 15 min intervals

    //when query metric interval data with custom interval, start and end date in where clause
    DateTime endDate = firstInterval.plusMinutes(DEFAULT_INTERVAL);
    DateTime startDate = firstInterval;
    MetricsQuery metricQuery = managementService.createMetricsQuery()
            .startDate(startDate.toDate())
            .endDate(endDate.toDate())
            .name(ACTIVTY_INSTANCE_START);
    List<MetricIntervalValue> metrics = metricQuery.interval(300);
    long sum = metricQuery.sum();

    //then result contains 3 entries
    assertEquals(3, metrics.size());
    long summedValue = 0;
    summedValue += metrics.get(0).getValue();
    summedValue += metrics.get(1).getValue();
    summedValue += metrics.get(2).getValue();

    //summed value should be equal to the summed query value
    assertEquals(sum, summedValue);
  }

  // NOT LOGGED METRICS //////////////////////////////////////////////////////////////////////

  @Test
  public void testMeterQueryNotLoggedInterval() {
    //given metric data
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().name(ACTIVTY_INSTANCE_START).limit(1).interval();
    long value = metrics.get(0).getValue();

    //when start process and metrics are not logged
    processEngineConfiguration.getMetricsRegistry().markOccurrence(ACTIVTY_INSTANCE_START, 3);

    //then metrics values are either way aggregated to the last interval
    //on query with name
     metrics = managementService.createMetricsQuery().name(ACTIVTY_INSTANCE_START).limit(1).interval();
    long newValue = metrics.get(0).getValue();
    Assert.assertTrue(value + 3 == newValue);

    //on query without name also
     metrics = managementService.createMetricsQuery().interval();
     for (MetricIntervalValue intervalValue : metrics) {
       if (intervalValue.getName().equalsIgnoreCase(ACTIVTY_INSTANCE_START)) {
        newValue = intervalValue.getValue();
        Assert.assertTrue(value + 3 == newValue);
        break;
       }
     }

    //clean up
    clearLocalMetrics();
  }

  // NEW DATA AFTER SOME TIME ////////////////////////////////////////////////
  @Test
  public void testIntervallQueryWithGeneratedDataAfterSomeTime() {
    //given metric data and result of interval query
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().interval();

    //when time is running and metrics is reported
    Date lastInterval = metrics.get(0).getTimestamp();
    long nextTime = lastInterval.getTime() + DEFAULT_INTERVAL_MILLIS;
    ClockUtil.setCurrentTime(new Date(nextTime));

    reportMetrics();

    //then query returns more results
    List<MetricIntervalValue> newMetrics = managementService.createMetricsQuery().interval();
    assertNotEquals(metrics.size(), newMetrics.size());
    assertEquals(metrics.size() + metricsCount, newMetrics.size());
    assertEquals(newMetrics.get(0).getTimestamp().getTime(), metrics.get(0).getTimestamp().getTime() + DEFAULT_INTERVAL_MILLIS);
  }

  @Test
  public void testIntervallQueryWithGeneratedDataAfterSomeTimeForSpecificMetric() {
    //given metric data and result of interval query
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery()
      .name(ACTIVTY_INSTANCE_START)
      .startDate(new Date(0))
      .endDate(new Date(DEFAULT_INTERVAL_MILLIS * 200)).interval();

    //when time is running and metrics is reported
    Date lastInterval = metrics.get(0).getTimestamp();
    long nextTime = lastInterval.getTime() + DEFAULT_INTERVAL_MILLIS;
    ClockUtil.setCurrentTime(new Date(nextTime));

    reportMetrics();

    //then query returns more results
    List<MetricIntervalValue> newMetrics = managementService.createMetricsQuery()
      .name(ACTIVTY_INSTANCE_START)
      .startDate(new Date(0))
      .endDate(new Date(DEFAULT_INTERVAL_MILLIS * 200)).interval();
    assertNotEquals(metrics.size(), newMetrics.size());
    assertEquals(newMetrics.get(0).getTimestamp().getTime(), metrics.get(0).getTimestamp().getTime() + DEFAULT_INTERVAL_MILLIS);
    assertEquals(metrics.get(0).getValue(), newMetrics.get(1).getValue());

    //clean up
    clearMetrics();
  }

  // AGGREGATE BY REPORTER ////////////////////////////////////////////////

  @Test
  public void testMetricQueryAggregatedByReporterSingleReporter() {
    // given metric data and result of interval query
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().interval();

    // assume
    assertTrue(metrics.size() > 0);

    // when
    List<MetricIntervalValue> aggregatedMetrics = managementService.createMetricsQuery().aggregateByReporter().interval();

    // then
    assertEquals(metrics.size(), aggregatedMetrics.size());
    for (MetricIntervalValue metricIntervalValue : aggregatedMetrics) {
      assertNull(metricIntervalValue.getReporter());
    }
  }

  @Test
  public void testMetricQueryAggregatedByReporterThreeReporters() {
    // given metric data for default reported
    // generate data for reporter1
    processEngineConfiguration.getDbMetricsReporter().setReporterId("reporter1");
    generateMeterData(3, DEFAULT_INTERVAL_MILLIS);

    // generate data for reporter2
    processEngineConfiguration.getDbMetricsReporter().setReporterId("reporter2");
    generateMeterData(3, DEFAULT_INTERVAL_MILLIS);

    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().interval();

    // when
    List<MetricIntervalValue> aggregatedMetrics = managementService.createMetricsQuery().aggregateByReporter().interval();

    // then
    // multiply by 3 because there are three reporters: 'REPORTER_ID' (check the #initMetrics()), reporter1 and reporter2
    assertEquals(metrics.size(), aggregatedMetrics.size() * 3);
    for (MetricIntervalValue metricIntervalValue : aggregatedMetrics) {
      assertNull(metricIntervalValue.getReporter());
    }
  }

  @Test
  public void testMetricQueryAggregatedByReporterLimitAndTwoReporters() {
    // clean up default recorded metrics
    clearLocalMetrics();
    // given
    // generate data for reporter1
    processEngineConfiguration.getDbMetricsReporter().setReporterId("reporter1");
    generateMeterData(10, DEFAULT_INTERVAL_MILLIS);

    // generate data for reporter2
    processEngineConfiguration.getDbMetricsReporter().setReporterId("reporter2");
    generateMeterData(10, DEFAULT_INTERVAL_MILLIS);

    int limit = 10;
    // when
    List<MetricIntervalValue> metrics = managementService.createMetricsQuery().name(Metrics.ACTIVTY_INSTANCE_START).limit(limit).interval();
    List<MetricIntervalValue> aggregatedMetrics = managementService.createMetricsQuery().name(Metrics.ACTIVTY_INSTANCE_START).limit(limit).aggregateByReporter().interval();

    // then aggregatedMetrics contains wider time interval
    assertTrue(metrics.get(limit - 1).getTimestamp().getTime() > aggregatedMetrics.get(limit - 1).getTimestamp().getTime());
    assertEquals(metrics.size(), aggregatedMetrics.size());
  }

}

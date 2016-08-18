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
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsQueryImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metric;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import static junit.framework.TestCase.assertEquals;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.junit.ClassRule;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MetricsIntervalTest {

  protected static final ProcessEngineRule ENGINE_RULE = new ProvidedProcessEngineRule();
  protected static final ProcessEngineTestRule TEST_RULE = new ProcessEngineTestRule(ENGINE_RULE);

  @ClassRule
  public static RuleChain RULE_CHAIN = RuleChain.outerRule(ENGINE_RULE).around(TEST_RULE);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  protected static RuntimeService runtimeService;
  protected static ProcessEngineConfigurationImpl processEngineConfiguration;
  protected static ManagementService managementService;

  private static void generateMeterData(long dataCount, long intervall, long dataPerIntervall) {
    TEST_RULE.deploy(Bpmn.createExecutableProcess("testProcess")
            .startEvent()
            .manualTask()
            .endEvent()
            .done());
    long startDate = 0;
    long diff = intervall / dataPerIntervall - 1;
    for (int i = 0; i <= dataCount; i++) {
      for (int j = 0; j < dataPerIntervall; j++) {
        startDate += diff;
        ClockUtil.setCurrentTime(new Date(startDate));
        runtimeService.startProcessInstanceByKey("testProcess");
        processEngineConfiguration.getDbMetricsReporter().reportNow();
      }
    }
  }

  @BeforeClass
  public static void initMetrics() {
    runtimeService = ENGINE_RULE.getRuntimeService();
    processEngineConfiguration = ENGINE_RULE.getProcessEngineConfiguration();
    managementService = ENGINE_RULE.getManagementService();
    generateMeterData(3, 15 * 60 * 1000, 5);
  }

  @AfterClass
  public static void cleanUp() {
    managementService.deleteMetrics(null);
  }

  //====================================================================================
  //====================================LIMIT===========================================
  //====================================================================================
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
    List<Metric> metrics = managementService.createMetricsQuery().limit(10).interval();

    //then 10 values are returned
    assertEquals(10, metrics.size());
  }

  @Test
  public void testMeterQueryIncreaseLimit() {
    //given metric data

    //when query metric interval data with max results set to 1000
    exception.expect(ProcessEngineException.class);
    exception.expectMessage("Metrics interval query row limit can't be set larger than 200.");
    List<Metric> metrics = managementService.createMetricsQuery().limit(1000).interval();

    //then max 200 values are returned
    assertEquals(MetricsQueryImpl.DEFAULT_LIMIT_SELECT_INTERVAL, metrics.size());
  }

  //====================================================================================
  //====================================OFFSET==========================================
  //====================================================================================
  @Test
  public void testMeterQueryOffset() {
    //given metric data

    //when query metric interval data with offset of 10
    List<Metric> metrics = managementService.createMetricsQuery().offset(10).interval();

    //then 26 values are returned and highest interval is second last interval, since first 9 was skipped
    assertEquals(26, metrics.size());
    assertEquals(2 * 15 * 60 * 1000, metrics.get(0).getTimestamp().getTime());
  }

  @Test
  public void testMeterQueryMaxOffset() {
    //given metric data

    //when query metric interval data with max offset
    List<Metric> metrics = managementService.createMetricsQuery().offset(Integer.MAX_VALUE).interval();

    //then 0 values are returned
    assertEquals(0, metrics.size());
  }

  //====================================================================================
  //====================================INTERVAL========================================
  //====================================================================================
  @Test
  public void testMeterQueryDefaultInterval() {
    //given metric data

    //when query metric interval data with default values
    List<Metric> metrics = managementService.createMetricsQuery().interval();

    //then default interval is 900 s (15 minutes)
    int interval = 15 * 60 * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    metrics.remove(0);
    for (Metric metric : metrics) {
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + interval);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryCustomInterval() {
    //given metric data

    //when query metric interval data with custom time interval
    List<Metric> metrics = managementService.createMetricsQuery().interval(300);

    //then custom interval is 300 s (5 minutes)
    int interval = 5 * 60 * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    metrics.remove(0);
    for (Metric metric : metrics) {
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + interval);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  //====================================================================================
  //==================================WHERE REPORTER====================================
  //====================================================================================
  @Test
  public void testMeterQueryDefaultIntervalWhereReporter() {
    //given metric data

    //when query metric interval data with reporter in where clause
    List<Metric> metrics = managementService.createMetricsQuery().reporter("127.0.0.1$default").interval();

    //then result contains only metrics from given reporter, since it is the default it contains all
    assertEquals(36, metrics.size());
    int interval = 15 * 60 * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String reporter = metrics.get(0).getReporter();
    metrics.remove(0);
    for (Metric metric : metrics) {
      assertEquals(reporter, metric.getReporter());
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + interval);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryDefaultIntervalWhereReporterNotExist() {
    //given metric data

    //when query metric interval data with not existing reporter in where clause
    List<Metric> metrics = managementService.createMetricsQuery().reporter("notExist").interval();

    //then result contains no metrics from given reporter
    assertEquals(0, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereReporter() {
    //given metric data

    //when query metric interval data with custom interval and reporter in where clause
    List<Metric> metrics = managementService.createMetricsQuery().reporter("127.0.0.1$default").interval(300);

    //then result contains only metrics from given reporter, since it is the default it contains all
    //36 * (15/5) = 108
    assertEquals(108, metrics.size());
    int interval = 5 * 60 * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String reporter = metrics.get(0).getReporter();
    metrics.remove(0);
    for (Metric metric : metrics) {
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
    List<Metric> metrics = managementService.createMetricsQuery().reporter("notExist").interval(300);

    //then result contains no metrics from given reporter
    assertEquals(0, metrics.size());
  }

  //====================================================================================
  //==================================WHERE NAME========================================
  //====================================================================================
  @Test
  public void testMeterQueryDefaultIntervalWhereName() {
    //given metric data

    //when query metric interval data with name in where clause
    List<Metric> metrics = managementService.createMetricsQuery().name("activity-instance-start").interval();

    //then result contains only metrics with given name
    assertEquals(4, metrics.size());
    int interval = 15 * 60 * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String name = metrics.get(0).getName();
    metrics.remove(0);
    for (Metric metric : metrics) {
      assertEquals(name, metric.getName());
      long nextTimestamp = metric.getTimestamp().getTime();
      if (lastTimestamp != nextTimestamp) {
        assertEquals(lastTimestamp, nextTimestamp + interval);
        lastTimestamp = nextTimestamp;
      }
    }
  }

  @Test
  public void testMeterQueryDefaultIntervalWhereNameNotExist() {
    //given metric data

    //when query metric interval data with non existing name in where clause
    List<Metric> metrics = managementService.createMetricsQuery().name("notExist").interval();

    //then result contains no metrics with given name
    assertEquals(0, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereName() {
    //given metric data

    //when query metric interval data with custom interval and name in where clause
    List<Metric> metrics = managementService.createMetricsQuery().name("activity-instance-start").interval(300);

    //then result contains only metrics with given name
    assertEquals(12, metrics.size());
    int interval = 5 * 60 * 1000;
    long lastTimestamp = metrics.get(0).getTimestamp().getTime();
    String name = metrics.get(0).getName();
    metrics.remove(0);
    for (Metric metric : metrics) {
      assertEquals(name, metric.getName());
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
    List<Metric> metrics = managementService.createMetricsQuery().name("notExist").interval(300);

    //then result contains no metrics from given name
    assertEquals(0, metrics.size());
  }

  //====================================================================================
  //==================================START DATE========================================
  //====================================================================================
  @Test
  public void testMeterQueryDefaultIntervalWhereStartDate() {
    //given metric data created for 14.9  min intervals 5 test datas so each 2.98 min

    //when query metric interval data with second last interval as start date in where clause
    //second last interval = start date = Jan 1, 1970 1:30:00 AM
    Date startDate = new Date(2 * 15 * 60 * 1000);
    List<Metric> metrics = managementService.createMetricsQuery().startDate(startDate).interval();

    //then result contains 18 entries since 9 different metrics are created
    //intervals Jan 1, 1970 1:45:00 AM and Jan 1, 1970 1:30:00 AM
    assertEquals(18, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereStartDate() {
    //given metric data created for 14.9 min intervals 5 test datas so each 2.98 min

    //when query metric interval data with custom interval and second last interval as start date in where clause
    //second last interval = start date = Jan 1, 1970 1:30:00 AM
    Date startDate = new Date(2 * 15 * 60 * 1000);
    List<Metric> metrics = managementService.createMetricsQuery().startDate(startDate).interval(300);

    //then result contains 54 entries since 9 different metrics are created
    //intervals Jan 1, 1970 1:55:00 PM, 1:50, 1:45, 1:40, 1:35
    assertEquals(54, metrics.size());
  }

  //====================================================================================
  //==================================END DATE========================================
  //====================================================================================
  @Test
  public void testMeterQueryDefaultIntervalWhereEndDate() {
    //given metric data created for 14.9 min intervals 5 test datas so each 2.98 min

    //when query metric interval data with second interval as end date in where clause
    //second interval = end date = Jan 1, 1970 1:30:00 PM
    Date endDate = new Date(2 * 15 * 60 * 1000);
    List<Metric> metrics = managementService.createMetricsQuery().endDate(endDate).interval();

    //then result contains 18 entries since 9 different metrics are created
    //intervals Jan 1, 1970 1:00:00 PM and Jan 1, 1970 1:15:00 PM
    assertEquals(18, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereEndDate() {
    //given metric data created for 14.9 min intervals 5 test datas so each 2.98 min

    //when query metric interval data with custom interval and second interval as end date in where clause
    //second interval = end date = Jan 1, 1970 1:30:00 PM
    Date endDate = new Date(2 * 15 * 60 * 1000);
    List<Metric> metrics = managementService.createMetricsQuery().endDate(endDate).interval(300);

    //then result contains 54 entries since 9 different metrics are created
    //intervals Jan 1, 1970 1:00:00 PM, 1:05, 1:10, 1:15, 1:20, 1:25
    //endTime is exclusive which means the given date is not included in the result
    assertEquals(54, metrics.size());
  }

  //====================================================================================
  //==================================START AND END DATE================================
  //====================================================================================
  @Test
  public void testMeterQueryDefaultIntervalWhereStartAndEndDate() {
    //given metric data created for 15 min intervals 10 test datas so each 1.5 min

    //when query metric interval data with start and end date in where clause
    //end date = Jan 1, 1970 1:30:00 PM
    //start date = Jan 1, 1970 1:15:00 PM
    Date endDate = new Date(2 * 15 * 60 * 1000);
    Date startDate = new Date(1 * 15 * 60 * 1000);
    List<Metric> metrics = managementService.createMetricsQuery().startDate(startDate).endDate(endDate).interval();

    //then result contains 9 entries since 9 different metrics are created
    assertEquals(9, metrics.size());
  }

  @Test
  public void testMeterQueryCustomIntervalWhereStartAndEndDate() {
    //given metric data created for 15 min intervals 10 test datas so each 1.5 min

    //when query metric interval data with custom interval, start and end date in where clause
    //end date = Jan 1, 1970 1:30:00 PM
    //start date = Jan 1, 1970 1:15:00 PM
    Date endDate = new Date(2 * 15 * 60 * 1000);
    Date startDate = new Date(1 * 15 * 60 * 1000);
    List<Metric> metrics = managementService.createMetricsQuery().startDate(startDate).endDate(endDate).interval(300);

    //then result contains 27 entries since 9 different metrics are created
    //intervals Jan 1, 1970 1:15:00 PM, 1:20, 1:25
    //endTime is exclusive which means the given date is not included in the result
    assertEquals(27, metrics.size());
  }

  //====================================================================================
  //=======================================VALUE========================================
  //====================================================================================
  @Test
  public void testMeterQueryDefaultIntervalCalculatedValue() {
    //given metric data created for 15 min intervals 10 test datas so each 1.5 min

    //when query metric interval data with custom interval, start and end date in where clause
    //end date = Jan 1, 1970 1:30:00 PM
    //start date = Jan 1, 1970 1:15:00 PM
    Date endDate = new Date(2 * 15 * 60 * 1000);
    Date startDate = new Date(1 * 15 * 60 * 1000);
    MetricsQuery metricQuery = managementService.createMetricsQuery()
            .startDate(startDate)
            .endDate(endDate)
            .name("activity-instance-start");
    List<Metric> metrics = metricQuery.interval();
    long sum = metricQuery.sum();

    //then result contains 1 entries
    //sum should be equal to the sum which is calculated by the metric query
    assertEquals(1, metrics.size());
    assertEquals(sum, metrics.get(0).getValue());
  }

  @Test
  public void testMeterQueryCustomIntervalCalculatedValue() {
    //given metric data created for 15 min intervals 10 test datas so each 1.5 min

    //when query metric interval data with custom interval, start and end date in where clause
    //end date = Jan 1, 1970 1:30:00 PM
    //start date = Jan 1, 1970 1:15:00 PM
    Date endDate = new Date(2 * 15 * 60 * 1000);
    Date startDate = new Date(1 * 15 * 60 * 1000);
    MetricsQuery metricQuery = managementService.createMetricsQuery()
            .startDate(startDate)
            .endDate(endDate)
            .name("activity-instance-start");
    List<Metric> metrics = metricQuery.interval(300);
    long sum = metricQuery.sum();

    //then result contains 3 entries
    assertEquals(3, metrics.size());
    long summedValue = 0;
    //the first interval contains 4 entries since an entry will created each 2.98 min
    //so the summed value is 12 because 3 activities are created per entry
    //entries 26.82 29.8
    assertEquals(6, metrics.get(0).getValue());
    summedValue += metrics.get(0).getValue();

    //second interval contains 2 entries
    //entries 20.86 23.84
    assertEquals(6, metrics.get(1).getValue());
    summedValue += metrics.get(1).getValue();

    //third interval contains 1 entry
    //entries 17.88
    assertEquals(3, metrics.get(2).getValue());
    summedValue += metrics.get(2).getValue();

    //summed value should be equal to the summed query value
    assertEquals(sum, summedValue);
  }
}

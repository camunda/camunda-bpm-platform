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

import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * Represents the abstract metrics interval test class, which contains methods
 * for generating metrics and clean up afterwards.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public abstract class AbstractMetricsIntervalTest {

  protected static final ProcessEngineRule ENGINE_RULE = new ProvidedProcessEngineRule();
  protected static final ProcessEngineTestRule TEST_RULE = new ProcessEngineTestRule(ENGINE_RULE);
  protected static final String REPORTER_ID = "REPORTER_ID";
  protected static final int DEFAULT_INTERVAL = 15;
  protected static final int DEFAULT_INTERVAL_MILLIS = 15 * 60 * 1000;

  @ClassRule
  public static RuleChain RULE_CHAIN = RuleChain.outerRule(ENGINE_RULE).around(TEST_RULE);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  protected static RuntimeService runtimeService;
  protected static ProcessEngineConfigurationImpl processEngineConfiguration;
  protected static ManagementService managementService;
  protected static String lastReporterId;
  protected static DateTime firstInterval;
  protected static int metricsCount;

  private static void generateMeterData(long dataCount, long interval) {
    //set up for randomnes
    final int MIN_OCCURENCE = 1;
    final int MAX_OCCURENCE = 250;

    MetricsRegistry metricsRegistry = processEngineConfiguration.getMetricsRegistry();
    Set<String> metricNames = metricsRegistry.getMeters().keySet();
    metricsCount = metricNames.size();
    Random rand = new Random(new Date().getTime());

    //start date is the default interval since mariadb can't set 0 as timestamp
    long startDate = DEFAULT_INTERVAL_MILLIS;
    firstInterval = new DateTime(startDate);
    //we will have 5 metric reports in an interval
    int dataPerInterval = 5;

    //generate data
    for (int i = 0; i < dataCount; i++) {
      //calulate diff so timer can be set correctly
      long diff = interval / dataPerInterval;
      for (int j = 0; j < dataPerInterval; j++) {
        ClockUtil.setCurrentTime(new Date(startDate));
        //generate random count of data per interv
        //for each metric
        for (String metricName : metricNames) {
          //mark random occurence
          long occurence = (long) (rand.nextInt((MAX_OCCURENCE - MIN_OCCURENCE) + 1) + MIN_OCCURENCE);
          metricsRegistry.markOccurrence(metricName, occurence);
        }
        //report logged metrics
        processEngineConfiguration.getDbMetricsReporter().reportNow();
        startDate += diff;
      }
    }
  }

  protected static void clearMetrics() {
    clearLocalMetrics();
    managementService.deleteMetrics(null);
  }

  protected static void clearLocalMetrics() {
    Collection<Meter> meters = processEngineConfiguration.getMetricsRegistry().getMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
  }

  @BeforeClass
  public static void initMetrics() throws Exception {
    runtimeService = ENGINE_RULE.getRuntimeService();
    processEngineConfiguration = ENGINE_RULE.getProcessEngineConfiguration();
    managementService = ENGINE_RULE.getManagementService();

    //clean up before start
    clearMetrics();

    //init metrics
    processEngineConfiguration.setDbMetricsReporterActivate(true);
    lastReporterId = processEngineConfiguration.getDbMetricsReporter().getMetricsCollectionTask().getReporter();
    processEngineConfiguration.getDbMetricsReporter().setReporterId(REPORTER_ID);
    generateMeterData(3, DEFAULT_INTERVAL_MILLIS);
  }

  @AfterClass
  public static void cleanUp() {
    ClockUtil.reset();
    processEngineConfiguration.setDbMetricsReporterActivate(false);
    processEngineConfiguration.getDbMetricsReporter().setReporterId(lastReporterId);
    clearMetrics();
  }
}

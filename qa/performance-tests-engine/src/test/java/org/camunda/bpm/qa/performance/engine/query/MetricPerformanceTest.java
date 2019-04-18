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
package org.camunda.bpm.qa.performance.engine.query;

import java.util.Arrays;
import java.util.Date;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.qa.performance.engine.junit.ProcessEnginePerformanceTestCase;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.GenerateMetricsTask;
import org.camunda.bpm.qa.performance.engine.steps.MetricIntervalStep;
import org.camunda.bpm.qa.performance.engine.steps.MetricSumStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class MetricPerformanceTest extends ProcessEnginePerformanceTestCase {

  @Parameter(0)
  public String name;

  @Parameter(1)
  public Date startDate;

  @Parameter(2)
  public Date endDate;

  @Parameters(name="{index}")
  public static Iterable<Object[]> params() {
    return Arrays.asList(new Object[][]
    {
      {null,null,null},
      {Metrics.ACTIVTY_INSTANCE_START, null, null},
      {Metrics.ACTIVTY_INSTANCE_START, new Date(0), null},
      {null, new Date(0), null},
      {null, null, new Date(GenerateMetricsTask.INTERVAL*250)},
      {Metrics.ACTIVTY_INSTANCE_START, null, new Date(GenerateMetricsTask.INTERVAL*250)},
      {Metrics.ACTIVTY_INSTANCE_START, new Date(0), new Date(GenerateMetricsTask.INTERVAL*250)},
      {null, new Date(0), new Date(GenerateMetricsTask.INTERVAL*250)}
    });
  }

  @Test
  public void metricInterval() {
    performanceTest().step(new MetricIntervalStep(name, startDate, endDate, engine)).run();
  }

  @Test
  public void metricSum() {
    performanceTest().step(new MetricSumStep(name, startDate, endDate, engine)).run();
  }
}

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
package org.camunda.bpm.qa.performance.engine.loadgenerator.tasks;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * Represents an task which generates metrics of an year.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class GenerateMetricsTask implements Runnable {

  /**
   * The iteration count which indicates how often the metric generation is
   * repeated per thread execution.
   */
  public static final int ITERATION_PER_EXECUTION = 2;

  /**
   * The milliseconds per year.
   */
  public static final long MS_COUNT_PER_YEAR = 365 * 24 * 60 * 60 * 1000L;

  /**
   * Generator to generate the thread id's.
   */
  public static final AtomicInteger THREAD_ID_GENERATOR = new AtomicInteger(0);

  /**
   * The thread id which identifies the current thread.
   */
  public static final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<Integer>() {

    @Override
    protected Integer initialValue() {
      return THREAD_ID_GENERATOR.getAndIncrement();
    }
  };

  /**
   * The start time on which the thread should begin to generate metrics.
   * Each thread has his own start time, which is calculated with his id
   * and the milliseconds per year. That means each thread generated
   * data in a different year.
   */
  public static final ThreadLocal<Long> START_TIME = new ThreadLocal<Long>() {

    @Override
    protected Long initialValue() {
      return MS_COUNT_PER_YEAR * THREAD_ID.get();
    }
  };

  /**
   * The interval length in milliseconds.
   */
  public static final long INTERVAL = 15 * 60 * 1000;

  /**
   * The process engine configuration, which is used for the metric reporting.
   */
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public GenerateMetricsTask(ProcessEngine processEngine) {
    this.processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
  }

  @Override
  public void run() {
    //set up
    long startTime = START_TIME.get();
    MetricsRegistry metricsRegistry = processEngineConfiguration.getMetricsRegistry();
    Set<String> metricNames = metricsRegistry.getDbMeters().keySet();

    //generate metric
    for (int i = 0; i < ITERATION_PER_EXECUTION; i++) {
      ClockUtil.setCurrentTime(new Date(startTime));
      for (String metricName : metricNames) {
        //mark occurence
        metricsRegistry.markOccurrence(metricName, 1);
      }
      processEngineConfiguration.getDbMetricsReporter().reportNow();
      startTime += INTERVAL;
    }
    START_TIME.set(startTime);
  }

}

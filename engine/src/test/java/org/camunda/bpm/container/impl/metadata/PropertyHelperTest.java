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
package org.camunda.bpm.container.impl.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 */
public class PropertyHelperTest {

  // process engine properties
  protected static final String JOB_EXECUTOR_DEPLOYMENT_AWARE_PROP = "jobExecutorDeploymentAware";
  protected static final String JOB_EXECUTOR_PREFER_TIMER_JOBS = "jobExecutorPreferTimerJobs";
  protected static final String JOB_EXECUTOR_ACQUIRE_BY_DUE_DATE = "jobExecutorAcquireByDueDate";
  protected static final String MAIL_SERVER_PORT_PROP = "mailServerPort";
  protected static final String JDBC_URL_PROP = "jdbcUrl";
  protected static final String DB_IDENTITY_USED_PROP = "dbIdentityUsed";

  // job executor properties
  protected static final String MAX_JOBS_PER_ACQUISITION = "maxJobsPerAcquisition";
  protected static final String MAX_WAIT = "maxWait";
  protected static final String WAIT_INCREASE_FACTOR = "waitIncreaseFactor";
  protected static final String BACKOFF_TIME_IN_MILLIS = "backoffTimeInMillis";


  /**
   * Assert that String, int and boolean properties can be set.
   */
  @Test
  public void testProcessEngineConfigurationProperties() {
    ProcessEngineConfiguration engineConfiguration = new StandaloneProcessEngineConfiguration();

    Map<String, String> propertiesToSet = new HashMap<String, String>();
    propertiesToSet.put(JOB_EXECUTOR_DEPLOYMENT_AWARE_PROP, "true");
    propertiesToSet.put(JOB_EXECUTOR_PREFER_TIMER_JOBS, "true");
    propertiesToSet.put(JOB_EXECUTOR_ACQUIRE_BY_DUE_DATE, "true");
    propertiesToSet.put(MAIL_SERVER_PORT_PROP, "42");
    propertiesToSet.put(JDBC_URL_PROP, "someUrl");

    PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);

    Assert.assertTrue(engineConfiguration.isJobExecutorDeploymentAware());
    Assert.assertTrue(engineConfiguration.isJobExecutorPreferTimerJobs());
    Assert.assertTrue(engineConfiguration.isJobExecutorAcquireByDueDate());
    Assert.assertEquals(42, engineConfiguration.getMailServerPort());
    Assert.assertEquals("someUrl", engineConfiguration.getJdbcUrl());
  }

  @Test
  public void testJobExecutorConfigurationProperties() {
    // given
    JobExecutor jobExecutor = new DefaultJobExecutor();

    Map<String, String> propertiesToSet = new HashMap<String, String>();
    propertiesToSet.put(MAX_JOBS_PER_ACQUISITION, Integer.toString(Integer.MAX_VALUE));
    propertiesToSet.put(MAX_WAIT, Long.toString(Long.MAX_VALUE));
    propertiesToSet.put(WAIT_INCREASE_FACTOR, Float.toString(Float.MAX_VALUE));
    propertiesToSet.put(BACKOFF_TIME_IN_MILLIS, Integer.toString(Integer.MAX_VALUE));

    // when
    PropertyHelper.applyProperties(jobExecutor, propertiesToSet);

    // then
    Assert.assertEquals(Integer.MAX_VALUE, jobExecutor.getMaxJobsPerAcquisition());
    Assert.assertEquals(Long.MAX_VALUE, jobExecutor.getMaxWait());
    Assert.assertEquals(Float.MAX_VALUE, jobExecutor.getWaitIncreaseFactor(), 0.0001d);
    Assert.assertEquals(Integer.MAX_VALUE, jobExecutor.getBackoffTimeInMillis());
  }

  /**
   * Assures that property names are matched on the setter name according to java beans conventions
   * and not on the field name.
   */
  @Test
  public void testConfigurationPropertiesWithMismatchingFieldAndSetter() {
    ProcessEngineConfigurationImpl engineConfiguration = new StandaloneProcessEngineConfiguration();

    Map<String, String> propertiesToSet = new HashMap<String, String>();
    propertiesToSet.put(DB_IDENTITY_USED_PROP, "false");
    PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);

    Assert.assertFalse(engineConfiguration.isDbIdentityUsed());

    propertiesToSet.put(DB_IDENTITY_USED_PROP, "true");
    PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);

    Assert.assertTrue(engineConfiguration.isDbIdentityUsed());
  }

  @Test
  public void testNonExistingPropertyForProcessEngineConfiguration() {
    ProcessEngineConfiguration engineConfiguration = new StandaloneProcessEngineConfiguration();
    Map<String, String> propertiesToSet = new HashMap<String, String>();
    propertiesToSet.put("aNonExistingProperty", "someValue");

    try {
      PropertyHelper.applyProperties(engineConfiguration, propertiesToSet);
      Assert.fail();
    } catch (Exception e) {
      // happy path
    }
  }

  @Test
  public void testResolvePropertyForExistingProperty() {
    Properties source = new Properties();
    source.put("camunda.test.someKey", "1234");
    String result = PropertyHelper.resolveProperty(source, "${camunda.test.someKey}");
    Assert.assertEquals("1234", result);
  }

  @Test
  public void testResolvePropertyWhitespaceAndMore() {
    Properties source = new Properties();
    source.put("camunda.test.someKey", "1234");
    String result = PropertyHelper.resolveProperty(source, " -${ camunda.test.someKey }- ");
    Assert.assertEquals(" -1234- ", result);
  }

  @Test
  public void testResolvePropertyForMultiplePropertes() {
    Properties source = new Properties();
    source.put("camunda.test.oneKey", "1234");
    source.put("camunda.test.anotherKey", "5678");
    String result = PropertyHelper.resolveProperty(source, "-${ camunda.test.oneKey }-${ camunda.test.anotherKey}-");
    Assert.assertEquals("-1234-5678-", result);
  }

  @Test
  public void testResolvePropertyForMissingProperty() {
    Properties source = new Properties();
    String result = PropertyHelper.resolveProperty(source, "${camunda.test.someKey}");
    Assert.assertEquals("", result);
  }

  @Test
  public void testResolvePropertyNoTemplate() {
    Properties source = new Properties();
    source.put("camunda.test.someKey", "1234");
    String result = PropertyHelper.resolveProperty(source, "camunda.test.someKey");
    Assert.assertEquals("camunda.test.someKey", result);
  }
}

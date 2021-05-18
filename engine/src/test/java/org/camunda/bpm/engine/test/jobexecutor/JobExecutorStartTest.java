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
package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.jobexecutor.AcquireJobsCommandFactory;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JobExecutorStartTest {

  private JobExecutor jobExecutor;

  @Before
  public void setUp() throws Exception {
    jobExecutor = new DefaultJobExecutor();
  }

  @After
  public void tearDown() throws Exception {
    jobExecutor.shutdown();
  }

  @Test
  public void testDefaultInitialization() {
    jobExecutor.start();
    assertThat(jobExecutor.getAcquireJobsCmdFactory()).isNotNull();
    assertThat(jobExecutor.getAcquireJobsRunnable()).isNotNull();
    assertThat(jobExecutor.isActive()).isTrue();
  }

  @Test
  public void testCustomJobsCmdFactoryInitialization() {
    class MyAcquireJobCmdFactory implements AcquireJobsCommandFactory {

      @Override
      public Command<AcquiredJobs> getCommand(int numJobsToAcquire) {
        return null;
      }
    }
    MyAcquireJobCmdFactory myFactory = new MyAcquireJobCmdFactory();
    jobExecutor.setAcquireJobsCmdFactory(myFactory);
    jobExecutor.start();
    assertThat(jobExecutor.getAcquireJobsCmdFactory()).isSameAs(myFactory);
    assertThat(jobExecutor.isActive()).isTrue();

  }
}

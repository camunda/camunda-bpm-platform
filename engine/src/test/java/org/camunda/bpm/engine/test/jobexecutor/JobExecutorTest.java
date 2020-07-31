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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.junit.Test;

/**
 * @author Tom Baeyens
 */
public class JobExecutorTest extends JobExecutorTestCase {

  @Test
  public void testBasicJobExecutorOperation() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        jobManager.send(createTweetMessage("message-one"));
        jobManager.send(createTweetMessage("message-two"));
        jobManager.send(createTweetMessage("message-three"));
        jobManager.send(createTweetMessage("message-four"));

        jobManager.schedule(createTweetTimer("timer-one", new Date()));
        jobManager.schedule(createTweetTimer("timer-two", new Date()));
        return null;
      }
    });

    testRule.executeAvailableJobs();

    Set<String> messages = new HashSet<String>(tweetHandler.getMessages());
    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("message-one");
    expectedMessages.add("message-two");
    expectedMessages.add("message-three");
    expectedMessages.add("message-four");
    expectedMessages.add("timer-one");
    expectedMessages.add("timer-two");

    assertEquals(new TreeSet<String>(expectedMessages), new TreeSet<String>(messages));

    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        List<HistoricJobLog> historicJobLogs = processEngineConfiguration
            .getHistoryService()
            .createHistoricJobLogQuery()
            .list();

        for (HistoricJobLog historicJobLog : historicJobLogs) {
          commandContext
            .getHistoricJobLogManager()
            .deleteHistoricJobLogById(historicJobLog.getId());



        }
        return null;
      }
    });
  }

  @Test
  public void testJobExecutorHintConfiguration() {
    ProcessEngineConfiguration engineConfig1 =
        ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();

    assertTrue("default setting is true", engineConfig1.isHintJobExecutor());

    ProcessEngineConfiguration engineConfig2 =
        ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().setHintJobExecutor(false);

    assertFalse(engineConfig2.isHintJobExecutor());

    ProcessEngineConfiguration engineConfig3 =
        ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().setHintJobExecutor(true);

    assertTrue(engineConfig3.isHintJobExecutor());
  }

  @Test
  public void testAcquiredJobs() {
    List<String> firstBatch = new ArrayList<String>(Arrays.asList("a", "b", "c"));
    List<String> secondBatch = new ArrayList<String>(Arrays.asList("d", "e", "f"));
    List<String> thirdBatch = new ArrayList<String>(Arrays.asList("g"));

    AcquiredJobs acquiredJobs = new AcquiredJobs(0);
    acquiredJobs.addJobIdBatch(firstBatch);
    acquiredJobs.addJobIdBatch(secondBatch);
    acquiredJobs.addJobIdBatch(thirdBatch);

    assertEquals(firstBatch, acquiredJobs.getJobIdBatches().get(0));
    assertEquals(secondBatch, acquiredJobs.getJobIdBatches().get(1));
    assertEquals(thirdBatch, acquiredJobs.getJobIdBatches().get(2));

    acquiredJobs.removeJobId("a");
    assertEquals(Arrays.asList("b", "c"), acquiredJobs.getJobIdBatches().get(0));
    assertEquals(secondBatch, acquiredJobs.getJobIdBatches().get(1));
    assertEquals(thirdBatch, acquiredJobs.getJobIdBatches().get(2));

    assertEquals(3, acquiredJobs.getJobIdBatches().size());
    acquiredJobs.removeJobId("g");
    assertEquals(2, acquiredJobs.getJobIdBatches().size());
  }
}

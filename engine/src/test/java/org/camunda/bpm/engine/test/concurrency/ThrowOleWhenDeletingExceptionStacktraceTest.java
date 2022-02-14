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
package org.camunda.bpm.engine.test.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.junit.After;
import org.junit.Test;

/**
 * @author Tassilo Weidner
 */
public class ThrowOleWhenDeletingExceptionStacktraceTest extends ConcurrencyTestCase {

  protected AtomicReference<JobEntity> job = new AtomicReference<>();

  @After
  public void tearDown() throws Exception {
    if (job.get() != null) {
      processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {
        JobEntity jobEntity = job.get();

        jobEntity.setRevision(2);

        commandContext.getJobManager().deleteJob(jobEntity);
        commandContext.getByteArrayManager().deleteByteArrayById(jobEntity.getExceptionByteArrayId());
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobEntity.getId());

        return null;
      });
    }
  }

  @Test
  public void testThrowOleWhenDeletingExceptionStacktraceTest() {
    // given
    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute((Command<Void>) commandContext -> {
        JobEntity jobEntity = new MessageEntity();

        jobEntity.setExceptionStacktrace("foo");

        commandContext.getJobManager().insert(jobEntity);

        job.set(jobEntity);

        return null;
      });

    ThreadControl threadOne = executeControllableCommand(new ThreadOne());

    ThreadControl threadTwo = executeControllableCommand(new ThreadTwo());
    threadTwo.reportInterrupts();

    threadOne.waitForSync();
    threadTwo.waitForSync();

    threadTwo.makeContinueAndWaitForSync(); // store job entity in cache

    threadOne.makeContinue(); // flush job update statement (REV_ + 1)
    threadOne.join();

    // when
    threadTwo.makeContinue(); // flush delete statements (REV_ - 1)
    threadTwo.join();

    // then
    assertThat(threadTwo.getException().getMessage()).contains("Entity was updated by another transaction concurrently");
  }

  public class ThreadOne extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {
      monitor.sync();

      JobEntity jobEntity = commandContext.getJobManager().findJobById(job.get().getId());
      jobEntity.setLockOwner("foo");

      return null;
    }

  }

  public class ThreadTwo extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {
      monitor.sync();

      JobEntity jobEntity = commandContext.getJobManager().findJobById(job.get().getId());

      monitor.sync();

      commandContext.getJobManager().deleteJob(jobEntity);

      String byteArrayId = jobEntity.getExceptionByteArrayId();
      commandContext.getByteArrayManager().deleteByteArrayById(byteArrayId);

      return null;
    }

  }

}

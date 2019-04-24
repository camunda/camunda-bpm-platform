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
package org.camunda.bpm.engine.test.concurrency.partitioning;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tassilo Weidner
 */

public class CompetingHistoricAttachmentPartitioningTest extends AbstractPartitioningTest {

  public void testConcurrentFetchAndDelete() {
    // given
    String processInstanceId = deployAndStartProcess(PROCESS_WITH_USERTASK).getId();

    final Attachment attachment = taskService.createAttachment("anAttachmentType", null, processInstanceId,
      "anAttachmentName", null, "http://camunda.com");

    ThreadControl asyncThread = executeControllableCommand(new AsyncThread(attachment.getId()));

    // assume
    assertThat(taskService.getAttachment(attachment.getId()), notNullValue());

    asyncThread.waitForSync();

    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        commandContext.getAttachmentManager().delete((AttachmentEntity) attachment);

        return null;
      }
    });

    // when
    asyncThread.makeContinue();
    asyncThread.waitUntilDone();

    // then
    assertThat(taskService.getAttachment(attachment.getId()), nullValue());
  }

  public class AsyncThread extends ControllableCommand<Void> {

    String attachmentId;

    AsyncThread(String attachmentId) {
      this.attachmentId = attachmentId;
    }

    public Void execute(CommandContext commandContext) {

      commandContext.getDbEntityManager()
        .selectById(AttachmentEntity.class, attachmentId); // cache

      monitor.sync();

      AttachmentEntity changedAttachmentEntity = new AttachmentEntity();
      changedAttachmentEntity.setId(attachmentId);

      taskService.saveAttachment(changedAttachmentEntity);

      return null;
    }

  }

}

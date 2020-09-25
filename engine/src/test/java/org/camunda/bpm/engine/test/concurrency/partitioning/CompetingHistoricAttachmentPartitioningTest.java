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

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.task.Attachment;
import org.junit.Test;

/**
 * @author Tassilo Weidner
 */

public class CompetingHistoricAttachmentPartitioningTest extends AbstractPartitioningTest {

  @Test
  @RequiredDatabase(excludes = {DbSqlSessionFactory.CRDB })
  public void shouldSuppressOleOnConcurrentFetchAndDelete() {
    // given
    String processInstanceId = deployAndStartProcess(PROCESS_WITH_USERTASK).getId();

    final Attachment attachment = taskService.createAttachment("anAttachmentType", null, processInstanceId,
      "anAttachmentName", null, "http://camunda.com");

    ThreadControl asyncThread = executeControllableCommand(new AsyncThread(attachment.getId()));

    // assume
    assertThat(taskService.getAttachment(attachment.getId())).isNotNull();

    asyncThread.waitForSync();

    commandExecutor.execute((Command<Void>) commandContext -> {

      commandContext.getAttachmentManager().delete((AttachmentEntity) attachment);
      return null;
    });

    // when
    asyncThread.makeContinue();
    asyncThread.waitUntilDone();

    // then
    assertThat(taskService.getAttachment(attachment.getId())).isNull();
  }

  @Test
  @RequiredDatabase(excludes = { DbSqlSessionFactory.DB2, DbSqlSessionFactory.MSSQL, DbSqlSessionFactory.ORACLE,
    DbSqlSessionFactory.POSTGRES, DbSqlSessionFactory.MYSQL, DbSqlSessionFactory.MARIADB,
    DbSqlSessionFactory.H2 })
  public void shouldThrowOleOnConcurrentFetchAndDeleteWithCrdb() {
    // given
    String processInstanceId = deployAndStartProcess(PROCESS_WITH_USERTASK).getId();

    final Attachment attachment = taskService.createAttachment("anAttachmentType", null, processInstanceId,
      "anAttachmentName", null, "http://camunda.com");

    ThreadControl asyncThread = executeControllableCommand(new AsyncThread(attachment.getId()));
    asyncThread.reportInterrupts();

    // assume
    assertThat(taskService.getAttachment(attachment.getId())).isNotNull();

    asyncThread.waitForSync();

    commandExecutor.execute((Command<Void>) commandContext -> {

      commandContext.getAttachmentManager().delete((AttachmentEntity) attachment);
      return null;
    });

    // when
    asyncThread.makeContinue();
    asyncThread.waitUntilDone();

    // then
    // TODO: consider additional assertions that show that the OLE prevented the changes in the original test
    assertThat(asyncThread.getException()).isInstanceOf(OptimisticLockingException.class);
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

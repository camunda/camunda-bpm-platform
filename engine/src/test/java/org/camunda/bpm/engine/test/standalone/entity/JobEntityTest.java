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
package org.camunda.bpm.engine.test.standalone.entity;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 *
 * @author Clint Manning
 */
public class JobEntityTest extends PluggableProcessEngineTest {

  /**
   * Note: This does not test a message with 4-byte Unicode supplementary
   * characters for two reasons:
   * - MySQL 5.1 does not support 4-byte supplementary characters (support from 5.5.3 onwards)
   * - {@link String#length()} counts these characters twice (since they are represented by two
   * chars), so essentially the cutoff would be half the actual cutoff for such a string
   */
  @Test
  public void testInsertJobWithExceptionMessage() {
    String fittingThreeByteMessage = repeatCharacter("\u9faf", StringUtil.DB_MAX_STRING_LENGTH);

    JobEntity threeByteJobEntity = new MessageEntity();
    threeByteJobEntity.setExceptionMessage(fittingThreeByteMessage);

    // should not fail
    insertJob(threeByteJobEntity);

    deleteJob(threeByteJobEntity);
  }

  @Test
  public void testJobExceptionMessageCutoff() {
    JobEntity threeByteJobEntity = new MessageEntity();

    String message = repeatCharacter("a", StringUtil.DB_MAX_STRING_LENGTH * 2);
    threeByteJobEntity.setExceptionMessage(message);
    assertEquals(StringUtil.DB_MAX_STRING_LENGTH, threeByteJobEntity.getExceptionMessage().length());
  }

  protected void insertJob(final JobEntity jobEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getJobManager().insert(jobEntity);
        return null;
      }
    });
  }

  protected void deleteJob(final JobEntity jobEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getJobManager().delete(jobEntity);
        return null;
      }
    });
  }

  protected String repeatCharacter(String encodedCharacter, int numCharacters) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < numCharacters; i++) {
      sb.append(encodedCharacter);
    }

    return sb.toString();
  }

  
   @Deployment
  @Test
  public void testLongProcessDefinitionKey() {
    String key = "myrealrealrealrealrealrealrealrealrealrealreallongprocessdefinitionkeyawesome";
    String processInstanceId = runtimeService.startProcessInstanceByKey(key).getId();

    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    assertEquals(key, job.getProcessDefinitionKey());
  }

}

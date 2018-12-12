/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.junit.Test;

public class JobExecutorExceptionLoggingHandlerTest {

  @Test
  @SuppressWarnings("unchecked")
  public void shouldBeAbleToReplaceLoggingHandler() {
    ExecuteJobHelper.ExceptionLoggingHandler originalHandler = ExecuteJobHelper.LOGGING_HANDLER;
    CollectingHandler collectingHandler = new CollectingHandler();
    RuntimeException exception = new RuntimeException();

    try {
      ExecuteJobHelper.LOGGING_HANDLER = collectingHandler;
      CommandExecutor failingCommandExecutor = mock(CommandExecutor.class);
      when(failingCommandExecutor.execute(any(Command.class))).thenThrow(exception);

      // when
      ExecuteJobHelper.executeJob("10", failingCommandExecutor);

      fail("exception expected");
    }
    catch (RuntimeException e) {
      // then
      Throwable collectedException = collectingHandler.collectedExceptions.get("10");
      assertEquals(collectedException, e);
      assertEquals(collectedException, exception);
    }
    finally {
      ExecuteJobHelper.LOGGING_HANDLER = originalHandler;
    }
  }

  static class CollectingHandler implements ExecuteJobHelper.ExceptionLoggingHandler {

    Map<String, Throwable> collectedExceptions = new HashMap<String, Throwable>();

    @Override
    public void exceptionWhileExecutingJob(String jobId, Throwable exception) {
      collectedExceptions.put(jobId, exception);
    }

  }

}

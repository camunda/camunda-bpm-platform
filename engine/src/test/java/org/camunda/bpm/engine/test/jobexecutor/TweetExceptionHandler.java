/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public class TweetExceptionHandler implements JobHandler {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  public static final String TYPE = "tweet-exception";

  protected AtomicInteger exceptionsRemaining = new AtomicInteger(2);

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ExecutionEntity execution, CommandContext commandContext) {
    if (exceptionsRemaining.decrementAndGet() >= 0) {
      throw new RuntimeException("exception remaining: "+exceptionsRemaining);
    }
    LOG.info("no more exceptions to throw.");
  }


  public int getExceptionsRemaining() {
    return exceptionsRemaining.get();
  }


  public void setExceptionsRemaining(int exceptionsRemaining) {
    this.exceptionsRemaining.set(exceptionsRemaining);
  }
}

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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;


/**
 * Throws an exception from a nested command; Unlike {@link TweetExceptionHandler}, this handler always throws exceptions.
 *
 * @author Thorben Lindhauer
 */
public class TweetNestedCommandExceptionHandler implements JobHandler {

  public static final String TYPE = "tweet-exception-nested";

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ExecutionEntity execution, CommandContext commandContext) {
    Context.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        throw new RuntimeException("nested command exception");
      }

    });
  }
}

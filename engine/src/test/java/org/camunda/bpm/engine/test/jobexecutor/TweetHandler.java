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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.test.jobexecutor.TweetHandler.TweetJobConfiguration;
import org.junit.Assert;

public class TweetHandler implements JobHandler<TweetJobConfiguration> {

  List<String> messages = new ArrayList<String>();

  public String getType() {
    return "tweet";
  }

  public void execute(TweetJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    messages.add(configuration.getMessage());
    Assert.assertNotNull(commandContext);
  }

  public List<String> getMessages() {
    return messages;
  }

  @Override
  public TweetJobConfiguration newConfiguration(String canonicalString) {
    TweetJobConfiguration config = new TweetJobConfiguration();
    config.message = canonicalString;

    return config;
  }

  public static class TweetJobConfiguration implements JobHandlerConfiguration {
    protected String message;

    public String getMessage() {
      return message;
    }

    public String toCanonicalString() {
      return message;
    }
  }

  public void onDelete(TweetJobConfiguration configuration, JobEntity jobEntity) {
    // do nothing
  }

}

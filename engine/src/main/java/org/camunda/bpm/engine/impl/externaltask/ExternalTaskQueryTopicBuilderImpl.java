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
package org.camunda.bpm.engine.impl.externaltask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskQueryTopicBuilderImpl implements ExternalTaskQueryTopicBuilder {

  protected TopicFetchInstruction instruction;
  protected ExternalTaskQueryBuilderImpl queryBuilder;

  public ExternalTaskQueryTopicBuilderImpl(ExternalTaskQueryBuilderImpl queryBuilder, String topicName, long lockDuration) {
    this.queryBuilder = queryBuilder;
    this.instruction = new TopicFetchInstruction(topicName, lockDuration);
  }

  public ExternalTaskQueryTopicBuilder topic(String topicName, long lockDuration) {
    submitInstruction();
    return queryBuilder.topic(topicName, lockDuration);
  }

  public List<LockedExternalTask> execute() {
    submitInstruction();
    return queryBuilder.execute();
  }

  public ExternalTaskQueryTopicBuilder variables(String... variables) {
    // don't use plain Arrays.asList since this returns an instance of a different list class
    // that is private and may mess mybatis queries up
    instruction.setVariablesToFetch(new ArrayList<String>(Arrays.asList(variables)));
    return this;
  }

  public ExternalTaskQueryTopicBuilder variables(List<String> variables) {
    instruction.setVariablesToFetch(variables);
    return this;
  }

  protected void submitInstruction() {
    queryBuilder.addInstruction(instruction);
  }


}

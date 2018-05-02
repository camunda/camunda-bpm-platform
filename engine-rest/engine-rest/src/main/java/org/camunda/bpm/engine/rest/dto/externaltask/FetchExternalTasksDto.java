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
package org.camunda.bpm.engine.rest.dto.externaltask;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;

import java.util.HashMap;
import java.util.List;

/**
 * @author Thorben Lindhauer
 *
 */
public class FetchExternalTasksDto {

  protected int maxTasks;
  protected String workerId;
  protected boolean usePriority = false;
  protected List<FetchExternalTaskTopicDto> topics;

  public int getMaxTasks() {
    return maxTasks;
  }
  public void setMaxTasks(int maxTasks) {
    this.maxTasks = maxTasks;
  }
  public String getWorkerId() {
    return workerId;
  }
  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }
  public List<FetchExternalTaskTopicDto> getTopics() {
    return topics;
  }
  public void setTopics(List<FetchExternalTaskTopicDto> topics) {
    this.topics = topics;
  }

  public boolean isUsePriority() {
    return usePriority;
  }

  public void setUsePriority(boolean usePriority) {
    this.usePriority = usePriority;
  }

  public static class FetchExternalTaskTopicDto {
    protected String topicName;
    protected String businessKey;
    protected long lockDuration;
    protected List<String> variables;
    protected HashMap<String, Object> processVariables;
    protected boolean deserializeValues = false;
    protected boolean localVariables = false;

    public String getTopicName() {
      return topicName;
    }
    public void setTopicName(String topicName) {
      this.topicName = topicName;
    }
    public String getBusinessKey() {
      return businessKey;
    }
    public void setBusinessKey(String businessKey) {
      this.businessKey = businessKey;
    }
    public long getLockDuration() {
      return lockDuration;
    }
    public void setLockDuration(long lockDuration) {
      this.lockDuration = lockDuration;
    }
    public List<String> getVariables() {
      return variables;
    }
    public void setVariables(List<String> variables) {
      this.variables = variables;
    }
    public HashMap<String, Object> getProcessVariables() {
      return processVariables;
    }
    public void setProcessVariables(HashMap<String, Object> processVariables) {
      this.processVariables = processVariables;
    }
    public boolean isDeserializeValues() {
      return deserializeValues;
    }
    public void setDeserializeValues(boolean deserializeValues) {
      this.deserializeValues = deserializeValues;
    }
    public boolean isLocalVariables() {
	  return localVariables;
    }
    public void setLocalVariables(boolean localVariables) {
      this.localVariables = localVariables;
    }
  }

  public ExternalTaskQueryBuilder buildQuery(ProcessEngine processEngine) {
    ExternalTaskQueryBuilder fetchBuilder = processEngine
      .getExternalTaskService()
      .fetchAndLock(getMaxTasks(), getWorkerId(), isUsePriority());

    if (getTopics() != null) {
      for (FetchExternalTaskTopicDto topicDto : getTopics()) {
        ExternalTaskQueryTopicBuilder topicFetchBuilder =
          fetchBuilder.topic(topicDto.getTopicName(), topicDto.getLockDuration());

        if (topicDto.getBusinessKey() != null) {
          topicFetchBuilder = topicFetchBuilder.businessKey(topicDto.getBusinessKey());
        }

        if (topicDto.getVariables() != null) {
          topicFetchBuilder = topicFetchBuilder.variables(topicDto.getVariables());
        }

        if (topicDto.getProcessVariables() != null) {
          topicFetchBuilder = topicFetchBuilder.processInstanceVariableEquals(topicDto.getProcessVariables());
        }

        if (topicDto.isDeserializeValues()) {
          topicFetchBuilder = topicFetchBuilder.enableCustomObjectDeserialization();
        }

        if (topicDto.isLocalVariables()) {
          topicFetchBuilder = topicFetchBuilder.localVariables();
        }

        fetchBuilder = topicFetchBuilder;
      }
    }

    return fetchBuilder;
  }

}

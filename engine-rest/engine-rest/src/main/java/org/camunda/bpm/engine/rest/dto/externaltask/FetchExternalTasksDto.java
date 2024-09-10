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
package org.camunda.bpm.engine.rest.dto.externaltask;

import static java.lang.Boolean.TRUE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.FetchAndLockBuilder;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.rest.dto.SortingDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * @author Thorben Lindhauer
 *
 */
public class FetchExternalTasksDto {

  protected int maxTasks;
  protected String workerId;
  protected boolean usePriority = false;
  protected List<FetchExternalTaskTopicDto> topics;
  protected boolean includeExtensionProperties = false;

  protected List<SortingDto> sorting;

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

  public void setSorting(List<SortingDto> sorting) {
    this.sorting = sorting;
  }

  public List<SortingDto> getSorting() {
    return this.sorting;
  }

  public boolean isIncludeExtensionProperties() {
    return includeExtensionProperties;
  }

  public void setIncludeExtensionProperties(boolean includeExtensionProperties) {
    this.includeExtensionProperties = includeExtensionProperties;
  }

  public static class FetchExternalTaskTopicDto {
    protected String topicName;
    protected String businessKey;
    protected String processDefinitionId;
    protected String[] processDefinitionIdIn;
    protected String processDefinitionKey;
    protected String[] processDefinitionKeyIn;
    protected String processDefinitionVersionTag;
    protected long lockDuration;
    protected List<String> variables;
    protected HashMap<String, Object> processVariables;
    protected boolean deserializeValues = false;
    protected boolean localVariables = false;
    protected boolean includeExtensionProperties = false;

    protected boolean withoutTenantId;
    protected String[] tenantIdIn;

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
    public String getProcessDefinitionId() {
      return processDefinitionId;
    }
    public void setProcessDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }
    public String[] getProcessDefinitionIdIn() {
      return processDefinitionIdIn;
    }
    public void setProcessDefinitionIdIn(String[] processDefinitionIds) {
      this.processDefinitionIdIn = processDefinitionIds;
    }
    public String getProcessDefinitionKey() {
      return processDefinitionKey;
    }
    public void setProcessDefinitionKey(String processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
    }
    public String[] getProcessDefinitionKeyIn() {
      return processDefinitionKeyIn;
    }
    public void setProcessDefinitionKeyIn(String[] processDefinitionKeys) {
      this.processDefinitionKeyIn = processDefinitionKeys;
    }
    public String getProcessDefinitionVersionTag() {
      return processDefinitionVersionTag;
    }
    public void setProcessDefinitionVersionTag(String processDefinitionVersionTag) {
      this.processDefinitionVersionTag = processDefinitionVersionTag;
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
    public boolean isWithoutTenantId() {
      return withoutTenantId;
    }
    public void setWithoutTenantId(boolean withoutTenantId) {
      this.withoutTenantId = withoutTenantId;
    }
    public String[] getTenantIdIn() {
      return tenantIdIn;
    }
    public void setTenantIdIn(String[] tenantIdIn) {
      this.tenantIdIn = tenantIdIn;
    }
    public boolean isIncludeExtensionProperties() {
      return includeExtensionProperties;
    }
    public void setIncludeExtensionProperties(boolean includeExtensionProperties) {
      this.includeExtensionProperties = includeExtensionProperties;
    }
  }

  public ExternalTaskQueryTopicBuilder buildQuery(ProcessEngine processEngine) {
    FetchAndLockBuilder fetchAndLockBuilder = getBuilder(processEngine);

    return configureTopics(fetchAndLockBuilder);
  }

  protected ExternalTaskQueryTopicBuilder configureTopics(FetchAndLockBuilder builder) {
    ExternalTaskQueryTopicBuilder topicBuilder = builder.subscribe();

    if (CollectionUtil.isEmpty(topics)) {
      return topicBuilder;
    }

    topics.forEach(topic -> {
      topicBuilder.topic(topic.getTopicName(), topic.getLockDuration());

      if (topic.getBusinessKey() != null) {
        topicBuilder.businessKey(topic.getBusinessKey());
      }

      if (topic.getProcessDefinitionId() != null) {
        topicBuilder.processDefinitionId(topic.getProcessDefinitionId());
      }

      if (topic.getProcessDefinitionIdIn() != null) {
        topicBuilder.processDefinitionIdIn(topic.getProcessDefinitionIdIn());
      }

      if (topic.getProcessDefinitionKey() != null) {
        topicBuilder.processDefinitionKey(topic.getProcessDefinitionKey());
      }

      if (topic.getProcessDefinitionKeyIn() != null) {
        topicBuilder.processDefinitionKeyIn(topic.getProcessDefinitionKeyIn());
      }

      if (topic.getVariables() != null) {
        topicBuilder.variables(topic.getVariables());
      }

      if (topic.getProcessVariables() != null) {
        topicBuilder.processInstanceVariableEquals(topic.getProcessVariables());
      }

      if (topic.isDeserializeValues()) {
        topicBuilder.enableCustomObjectDeserialization();
      }

      if (topic.isLocalVariables()) {
        topicBuilder.localVariables();
      }

      if (TRUE.equals(topic.isWithoutTenantId())) {
        topicBuilder.withoutTenantId();
      }

      if (topic.getTenantIdIn() != null) {
        topicBuilder.tenantIdIn(topic.getTenantIdIn());
      }

      if(topic.getProcessDefinitionVersionTag() != null) {
        topicBuilder.processDefinitionVersionTag(topic.getProcessDefinitionVersionTag());
      }

      if(topic.isIncludeExtensionProperties()) {
        topicBuilder.includeExtensionProperties();
      }
    });

    return topicBuilder;
  }

  protected FetchAndLockBuilder getBuilder(ProcessEngine engine) {
    ExternalTaskService service = engine.getExternalTaskService();

    FetchAndLockBuilder builder = service.fetchAndLock()
        .workerId(workerId)
        .maxTasks(maxTasks)
        .usePriority(usePriority);

    SortMapper mapper = new SortMapper(sorting, builder);

    return mapper.getBuilderWithSortConfigs();
  }

  /**
   * Encapsulates the mapping of sorting configurations (field, order) to the respective methods builder config methods
   * and applies them.
   * <p>
   * To achieve that, maps are used internally to map fields and orders to the corresponding builder method.
   */
  static class SortMapper {

    protected static Map<String, Consumer<FetchAndLockBuilder>> FIELD_MAPPINGS = Map.of(
        "createTime", FetchAndLockBuilder::orderByCreateTime
    );

    protected static Map<String, Consumer<FetchAndLockBuilder>> ORDER_MAPPINGS = Map.of(
        "asc", FetchAndLockBuilder::asc,
        "desc", FetchAndLockBuilder::desc
    );

    protected final List<SortingDto> sorting;
    protected final FetchAndLockBuilder builder;

    protected SortMapper(List<SortingDto> sorting, FetchAndLockBuilder builder) {
      this.sorting = (sorting == null) ? Collections.emptyList() : sorting;
      this.builder = builder;
    }

    /**
     * Applies the sorting field mappings to the builder and returns it.
     */
    protected FetchAndLockBuilder getBuilderWithSortConfigs() {
      for (SortingDto dto : sorting) {
        String sortBy = dto.getSortBy();
        configureFieldOrBadRequest(sortBy, "sortBy", FIELD_MAPPINGS);

        String sortOrder = dto.getSortOrder();
        if (sortOrder != null) {
          configureFieldOrBadRequest(sortOrder, "sortOrder", ORDER_MAPPINGS);
        }
      }
      return builder;
    }

    protected void configureFieldOrBadRequest(String key, String parameterName, Map<String, Consumer<FetchAndLockBuilder>> fieldMappings) {
      if (!fieldMappings.containsKey(key)) {
        throw new InvalidRequestException(BAD_REQUEST, "Cannot set query " + parameterName + " parameter to value " + key);
      }
      fieldMappings.get(key).accept(builder);
    }
  }
}

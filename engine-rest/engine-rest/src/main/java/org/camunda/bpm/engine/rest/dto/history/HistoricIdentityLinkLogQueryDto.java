package org.camunda.bpm.engine.rest.dto.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkLogQueryDto extends AbstractQueryDto<HistoricIdentityLinkLogQuery> {

  private static final String SORT_BY_TIME = "time";
  private static final String SORT_BY_TYPE = "type";
  private static final String SORT_BY_USER_ID = "userId";
  private static final String SORT_BY_GROUP_ID = "groupId";
  private static final String SORT_BY_TASK_ID = "taskId";
  private static final String SORT_BY_PROCESS_DEFINITION_ID = "processDefinitionId";
  private static final String SORT_BY_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  private static final String SORT_BY_OPERATION_TYPE = "operationType";
  private static final String SORT_BY_ASSIGNER_ID = "assignerId";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_TIME);
    VALID_SORT_BY_VALUES.add(SORT_BY_TYPE);
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_GROUP_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_TASK_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY);
    VALID_SORT_BY_VALUES.add(SORT_BY_OPERATION_TYPE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ASSIGNER_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected Date dateBefore;
  protected Date dateAfter;
  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String operationType;
  protected String assignerId;
  protected List<String> tenantIds;

  public HistoricIdentityLinkLogQueryDto() {
  }

  public HistoricIdentityLinkLogQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricIdentityLinkLogQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricIdentityLinkLogQuery();
  }

  @CamundaQueryParam("type")
  public void setType(String type) {
    this.type = type;
  }

  @CamundaQueryParam("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  @CamundaQueryParam("groupId")
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  @CamundaQueryParam(value = "dateBefore", converter = DateConverter.class)
  public void setDateBefore(Date dateBefore) {
    this.dateBefore = dateBefore;
  }

  @CamundaQueryParam(value = "dateAfter", converter = DateConverter.class)
  public void setDateAfter(Date dateAfter) {
    this.dateAfter = dateAfter;
  }

  @CamundaQueryParam("taskId")
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
  
  @CamundaQueryParam("operationType")
  public void setOperationType(String operationType) {
    this.operationType = operationType;
  }

  @CamundaQueryParam("assignerId")
  public void setAssignerId(String assignerId) {
    this.assignerId = assignerId;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @Override
  protected void applyFilters(HistoricIdentityLinkLogQuery query) {
    if (dateBefore != null) {
      query.dateBefore(dateBefore);
    }
    if (dateAfter != null) {
      query.dateAfter(dateAfter);
    }
    if (type != null) {
      query.type(type);
    }
    if (userId != null) {
      query.userId(userId);
    }
    if (groupId != null) {
      query.groupId(groupId);
    }
    if (taskId != null) {
      query.taskId(taskId);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (operationType != null) {
      query.operationType(operationType);
    }
    if (assignerId != null) {
      query.assignerId(assignerId);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
  }

  @Override
  protected void applySortBy(HistoricIdentityLinkLogQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_TIME)) {
      query.orderByTime();
    } else if (sortBy.equals(SORT_BY_TYPE)) {
      query.orderByType();
    } else if (sortBy.equals(SORT_BY_USER_ID)) {
      query.orderByUserId();
    } else if (sortBy.equals(SORT_BY_GROUP_ID)) {
      query.orderByGroupId();
    } else if (sortBy.equals(SORT_BY_TASK_ID)) {
      query.orderByTaskId();
    } else if (sortBy.equals(SORT_BY_OPERATION_TYPE)) {
      query.orderByOperationType();
    } else if (sortBy.equals(SORT_BY_ASSIGNER_ID)) {
      query.orderByAssignerId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_KEY)) {
      query.orderByProcessDefinitionKey();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }
}

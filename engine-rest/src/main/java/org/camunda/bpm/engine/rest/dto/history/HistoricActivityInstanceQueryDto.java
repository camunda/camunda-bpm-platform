package org.camunda.bpm.engine.rest.dto.history;


import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;

public class HistoricActivityInstanceQueryDto extends AbstractQueryDto<HistoricActivityInstanceQuery> {

	private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE = "activityInstanceId";
	private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
	private static final String SORT_BY_PROCESS_DEFINITION_ID_VALUE = "definitionId";
	private static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
	private static final String SORT_BY_ACTIVITY_ID_VALUE = "activityId";
	private static final String SORT_BY_ACTIVITY_NAME_VALUE = "activityName";
	private static final String SORT_BY_ACTIVITY_TYPE_VALUE = "activityType";
	private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_START_TIME_VALUE = "startTime";
	private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_END_TIME_VALUE = "endTime";
	private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_DURATION_VALUE = "duration";
	
	private static final List<String> VALID_SORT_BY_VALUES;
	static {
		VALID_SORT_BY_VALUES = new ArrayList<String>();
		VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_NAME_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_TYPE_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_START_TIME_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_END_TIME_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_DURATION_VALUE);
	}	
	
	private String processInstanceId;
	private String processDefinitionId;
	private String executionId;
	private String activityId;
	private String activityName;
	private String activityType;
	private String userId;
    private Boolean finished;

	public HistoricActivityInstanceQueryDto() {
			
    }
	 
	public HistoricActivityInstanceQueryDto(MultivaluedMap<String, String> queryParameters) {
	    super(queryParameters);
	}

	@CamundaQueryParam("processInstanceId")
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@CamundaQueryParam("processDefinitionId")
	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	@CamundaQueryParam("executionId")
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	@CamundaQueryParam("activityId")
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	@CamundaQueryParam("activityName")
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	@CamundaQueryParam("activityType")
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	@CamundaQueryParam("userId")
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@CamundaQueryParam(value = "finished", converter = BooleanConverter.class)
	public void setFinished(Boolean finished) {
		this.finished = finished;
	}

	@Override
	protected boolean isValidSortByValue(String value) {
		return VALID_SORT_BY_VALUES.contains(value);
	}

	@Override
	protected HistoricActivityInstanceQuery createNewQuery(ProcessEngine engine) {
		return engine.getHistoryService().createHistoricActivityInstanceQuery();	
	}

	@Override
	protected void applyFilters(HistoricActivityInstanceQuery query) {
		if (processInstanceId != null) {
		      query.processInstanceId(processInstanceId);
		    }
		    if (processDefinitionId != null) {
		      query.processDefinitionId(processDefinitionId);
		    }
		    if (executionId != null) {
		      query.executionId(executionId);
		    }
		    if (activityId != null) {
		      query.activityId(activityId);
		    }
		    if (activityName != null) {
			      query.activityName(activityName);
			}
		    if (activityType != null) {
			      query.activityType(activityType);
			}
		    if (userId != null) {
			      query.taskAssignee(userId);
			}
		    if (finished != null) {
		    	if(this.finished) {
		    		  query.finished();	
		    	} else {
		    		query.unfinished();
		    	}		    
			}			
	}
	
	@Override
	protected void applySortingOptions(HistoricActivityInstanceQuery query) {
	    if (sortBy != null) {
	        if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE)) {
	          query.orderByHistoricActivityInstanceId();
	        } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
	          query.orderByProcessInstanceId();
	        } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID_VALUE)) {
	          query.orderByProcessDefinitionId();
	        } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
		          query.orderByExecutionId();
		    } else if (sortBy.equals(SORT_BY_ACTIVITY_ID_VALUE)) {
		          query.orderByActivityId();
		    } else if (sortBy.equals(SORT_BY_ACTIVITY_NAME_VALUE)) {
		          query.orderByActivityName();
		    } else if (sortBy.equals(SORT_BY_ACTIVITY_TYPE_VALUE)) {
		          query.orderByActivityType();
		    } else if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_START_TIME_VALUE)) {
			          query.orderByHistoricActivityInstanceStartTime();
			} else if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_END_TIME_VALUE)) {
			          query.orderByHistoricActivityInstanceEndTime();
			} else if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_DURATION_VALUE)) {
			          query.orderByHistoricActivityInstanceDuration();
			}
	      }
	      
	      if (sortOrder != null) {
	        if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
	          query.asc();
	        } else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
	          query.desc();
	        }
	      }				
	}

}

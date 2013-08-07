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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.ConditionListConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.JobQuery;

public class JobQueryDto extends AbstractQueryDto<JobQuery> {

	private static final String SORT_BY_JOB_ID_VALUE = "jobId";
	private static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
	private static final String SORT_BY_PROCESSINSTANCE_ID_VALUE = "processInstanceId";
	private static final String SORT_BY_JOB_RETRIES_VALUE = "jobRetries";
	private static final String SORT_BY_JOB_DUEDATE_VALUE = "jobDueDate";

	private static final List<String> VALID_SORT_BY_VALUES;
	static {
		VALID_SORT_BY_VALUES = new ArrayList<String>();
		VALID_SORT_BY_VALUES.add(SORT_BY_JOB_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESSINSTANCE_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_JOB_RETRIES_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_JOB_DUEDATE_VALUE);
	}

	private String jobId;
	private String processInstanceId;
	private String executionId;
	private Boolean withRetriesLeft;
	private Boolean executable;
	private Boolean timers;
	private Boolean messages;
	private Boolean withException;
	private String exceptionMessage;
	private Boolean noRetriesLeft;

	private List<ConditionQueryParameterDto> dueDates;
	
  public JobQueryDto() {}

	public JobQueryDto(MultivaluedMap<String, String> queryParameters) {
		super(queryParameters);
	}

	@CamundaQueryParam("jobId")
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@CamundaQueryParam("processInstanceId")
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@CamundaQueryParam("executionId")
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}
	
  @CamundaQueryParam(value="withRetriesLeft", converter = BooleanConverter.class)
  public void setWithRetriesLeft(Boolean withRetriesLeft) {
    this.withRetriesLeft = withRetriesLeft;
  }
  
  @CamundaQueryParam(value="executable", converter = BooleanConverter.class)
  public void setExecutable(Boolean executable) {
    this.executable = executable;
  }

  @CamundaQueryParam(value="timers", converter = BooleanConverter.class)
  public void setTimers(Boolean timers) {
    this.timers = timers;
  }
  
  @CamundaQueryParam(value="withException", converter = BooleanConverter.class)
  public void setWithException(Boolean withException) {
    this.withException = withException;
  }
  
  @CamundaQueryParam(value="messages", converter = BooleanConverter.class)
  public void setMessages(Boolean messages) {
    this.messages = messages;
  }
  
	@CamundaQueryParam("exceptionMessage")
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	@CamundaQueryParam(value = "dueDates", converter = ConditionListConverter.class)
	public void setDueDates(List<ConditionQueryParameterDto> dueDates) {
		this.dueDates = dueDates;
	}

  @CamundaQueryParam(value="noRetriesLeft", converter = BooleanConverter.class)
  public void setNoRetriesLeft(Boolean noRetriesLeft) {
    this.noRetriesLeft = noRetriesLeft;
  }

	@Override
	protected boolean isValidSortByValue(String value) {
		return VALID_SORT_BY_VALUES.contains(value);
	}

	@Override
	protected JobQuery createNewQuery(ProcessEngine engine) {
		return engine.getManagementService().createJobQuery();
	}

	@Override
	protected void applyFilters(JobQuery query) {
		if (jobId != null) {
			query.jobId(jobId);
		}
		
		if (processInstanceId != null) {
			query.processInstanceId(processInstanceId);
		}
		
		if (executionId != null) {
			query.executionId(executionId);
		}
		
		if (withRetriesLeft != null && withRetriesLeft) {
		  query.withRetriesLeft();
		}
		
    if (executable != null && executable) {
      query.executable();
    }
		
    if (timers != null && timers) {
      if (messages != null && messages) {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Parameter timers cannot be used together with parameter messages.");
      }
      query.timers();
    }

    if (messages != null && messages) {
      if (timers != null && timers) {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Parameter messages cannot be used together with parameter timers.");
      }
      query.messages();
    }
    
    if (withException != null && withException) {
      query.withException();
    }
    
		if (exceptionMessage != null) {
			query.exceptionMessage(exceptionMessage);
		}

    if (noRetriesLeft != null && noRetriesLeft) {
      query.noRetriesLeft();
    }

		if (dueDates != null) {
			DateConverter dateConverter = new DateConverter();
			
			for (ConditionQueryParameterDto conditionQueryParam : dueDates) {
				String op = conditionQueryParam.getOperator();
				Date dueDate = null;
				
				try {
				  dueDate = dateConverter.convertQueryParameterToType((String)conditionQueryParam.getValue());
				} catch (IllegalArgumentException e) {
				  throw new InvalidRequestException(Status.BAD_REQUEST, e, "Invalid due date format: " + e.getMessage());
				}

				if (op.equals(ConditionQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
					query.duedateHigherThan(dueDate);
				} else if (op.equals(ConditionQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
					query.duedateLowerThan(dueDate);
				} else {
					throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid due date comparator specified: " + op);
				}
			}
		}
	}

	@Override
	protected void applySortingOptions(JobQuery query) {
		if (sortBy != null) {
			if (sortBy.equals(SORT_BY_JOB_ID_VALUE)) {
				query.orderByJobId();
			} else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
				query.orderByExecutionId();
			} else if (sortBy.equals(SORT_BY_PROCESSINSTANCE_ID_VALUE)) {
				query.orderByProcessInstanceId();
			} else if (sortBy.equals(SORT_BY_JOB_RETRIES_VALUE)) {
				query.orderByJobRetries();
			} else if (sortBy.equals(SORT_BY_JOB_DUEDATE_VALUE)) {
				query.orderByJobDuedate();
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

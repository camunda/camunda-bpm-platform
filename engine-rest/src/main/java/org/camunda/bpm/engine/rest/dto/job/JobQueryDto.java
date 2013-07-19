package org.camunda.bpm.engine.rest.dto.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
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
	private String exceptionMessage;

	private List<VariableQueryParameterDto> dueDates;

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

	@CamundaQueryParam("exceptionMessage")
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	@CamundaQueryParam(value = "dueDates", converter = VariableListConverter.class)
	public void setDueDates(List<VariableQueryParameterDto> dueDates) {
		this.dueDates = dueDates;
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
		if (exceptionMessage != null) {
			query.exceptionMessage(exceptionMessage);
		}

		if (dueDates != null) {
			DateConverter dateConverter = new DateConverter();
			for (VariableQueryParameterDto variableQueryParam : dueDates) {
				String op = variableQueryParam.getOperator();
				String variableValue = (String)variableQueryParam.getValue();

				if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
					query.duedateHigherThan(dateConverter.convertQueryParameterToType(variableValue));
				} else if (op
						.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
					query.duedateLowerThan(dateConverter.convertQueryParameterToType(variableValue));
				} else {
					throw new InvalidRequestException(Status.BAD_REQUEST,
							"Invalid due date comparator specified: " + op);
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

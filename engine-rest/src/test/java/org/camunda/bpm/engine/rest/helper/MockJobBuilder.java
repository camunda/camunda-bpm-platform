package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Date;

import org.camunda.bpm.engine.runtime.Job;

public class MockJobBuilder {

	private String id;
	private Date dueDate;
	private String exceptionMsg;
	private String executionId;
	private String processInstanceId;
	private int retries;

	public MockJobBuilder id(String id) {
		this.id = id;
		return this;
	}

	public MockJobBuilder dueDate(Date dueDate) {
		this.dueDate = dueDate;
		return this;
	}

	public MockJobBuilder exceptionMessage(String exceptionMessage) {
		this.exceptionMsg = exceptionMessage;
		return this;
	}

	public MockJobBuilder executionId(String executionId) {
		this.executionId = executionId;
		return this;
	}

	public MockJobBuilder processInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
		return this;
	}

	public MockJobBuilder retries(int retries) {
		this.retries = retries;
		return this;
	}

	public Job build() {
		Job mockJob = mock(Job.class);
		when(mockJob.getId()).thenReturn(id);
		when(mockJob.getDuedate()).thenReturn(dueDate);
		when(mockJob.getExceptionMessage()).thenReturn(exceptionMsg);
		when(mockJob.getExecutionId()).thenReturn(executionId);
		when(mockJob.getProcessInstanceId()).thenReturn(processInstanceId);
		when(mockJob.getRetries()).thenReturn(retries);
		return mockJob;
	}

}

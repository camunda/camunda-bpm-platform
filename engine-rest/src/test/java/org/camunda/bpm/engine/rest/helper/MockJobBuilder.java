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
package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.camunda.bpm.engine.runtime.Job;

public class MockJobBuilder {

	private String id;
	private Date dueDate;
	private String exceptionMessage;
	private String executionId;
	private String processInstanceId;
	private String processDefinitionId;
	private String processDefinitionKey;
	private int retries;
	private boolean suspended;

	public MockJobBuilder id(String id) {
		this.id = id;
		return this;
	}

	public MockJobBuilder dueDate(Date dueDate) {
		this.dueDate = dueDate;
		return this;
	}

	public MockJobBuilder exceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
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

  public MockJobBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public MockJobBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public MockJobBuilder suspended(boolean suspended) {
    this.suspended = suspended;
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
		when(mockJob.getExceptionMessage()).thenReturn(exceptionMessage);
		when(mockJob.getExecutionId()).thenReturn(executionId);
		when(mockJob.getProcessInstanceId()).thenReturn(processInstanceId);
		when(mockJob.getProcessDefinitionId()).thenReturn(processDefinitionId);
		when(mockJob.getProcessDefinitionKey()).thenReturn(processDefinitionKey);
		when(mockJob.getRetries()).thenReturn(retries);
		when(mockJob.isSuspended()).thenReturn(suspended);
		return mockJob;
	}

}

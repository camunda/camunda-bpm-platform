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
package org.camunda.bpm.engine.test.cmmn.listener;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;

/**
 * @author Thorben Lindhauer
 *
 */
public class DelegateVariableInstanceSpec {

  protected String expectedEventName;
  protected String expectedVariableName;
  protected String expectedVariableValue;

  protected String expectedProcessInstanceId;
  protected String expectedExecutionId;
  protected String expectedCaseInstanceId;
  protected String expectedCaseExecutionId;
  protected String expectedTaskId;
  protected String expectedActivityInstanceId;

  protected CaseExecution expectedSourceExecution;

  public void matches(DelegateCaseVariableInstance instance) {
    assertEquals(expectedEventName, instance.getEventName());
    assertEquals(expectedVariableName, instance.getName());
    assertEquals(expectedVariableValue, instance.getValue());
    assertEquals(expectedProcessInstanceId, instance.getProcessInstanceId());
    assertEquals(expectedExecutionId, instance.getExecutionId());
    assertEquals(expectedCaseInstanceId, instance.getCaseInstanceId());
    assertEquals(expectedCaseExecutionId, instance.getCaseExecutionId());
    assertEquals(expectedTaskId, instance.getTaskId());
    assertEquals(expectedActivityInstanceId, instance.getActivityInstanceId());

    assertEquals(expectedSourceExecution.getId(), instance.getSourceExecution().getId());
    assertEquals(expectedSourceExecution.getActivityId(), instance.getSourceExecution().getActivityId());
    assertEquals(expectedSourceExecution.getActivityName(), instance.getSourceExecution().getActivityName());
    assertEquals(expectedSourceExecution.getCaseDefinitionId(), instance.getSourceExecution().getCaseDefinitionId());
    assertEquals(expectedSourceExecution.getCaseInstanceId(), instance.getSourceExecution().getCaseInstanceId());
    assertEquals(expectedSourceExecution.getParentId(), instance.getSourceExecution().getParentId());
  }

  public static DelegateVariableInstanceSpec fromCaseExecution(CaseExecution caseExecution) {
    DelegateVariableInstanceSpec spec = new DelegateVariableInstanceSpec();
    spec.expectedCaseExecutionId = caseExecution.getId();
    spec.expectedCaseInstanceId = caseExecution.getCaseInstanceId();
    spec.expectedSourceExecution = caseExecution;
    return spec;
  }

  public DelegateVariableInstanceSpec sourceExecution(CaseExecution sourceExecution) {
    this.expectedSourceExecution = sourceExecution;
    return this;
  }

  public DelegateVariableInstanceSpec event(String eventName) {
    this.expectedEventName = eventName;
    return this;
  }

  public DelegateVariableInstanceSpec name(String variableName) {
    this.expectedVariableName = variableName;
    return this;
  }

  public DelegateVariableInstanceSpec value(String variableValue) {
    this.expectedVariableValue = variableValue;
    return this;
  }

  public DelegateVariableInstanceSpec activityInstanceId(String activityInstanceId) {
    this.expectedActivityInstanceId = activityInstanceId;
    return this;
  }
}

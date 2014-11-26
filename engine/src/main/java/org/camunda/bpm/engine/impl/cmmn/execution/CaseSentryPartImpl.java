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
package org.camunda.bpm.engine.impl.cmmn.execution;

/**
 * @author Roman Smirnov
 *
 */
public class CaseSentryPartImpl extends CmmnSentryPart {

  private static final long serialVersionUID = 1L;

  protected CaseExecutionImpl caseInstance;
  protected CaseExecutionImpl caseExecution;
  protected CaseExecutionImpl sourceCaseExecution;

  public CaseExecutionImpl getCaseInstance() {
    return caseInstance;
  }

  public void setCaseInstance(CmmnExecution caseInstance) {
    this.caseInstance = (CaseExecutionImpl) caseInstance;
  }

  public CmmnExecution getCaseExecution() {
    return caseExecution;
  }

  public void setCaseExecution(CmmnExecution caseExecution) {
    this.caseExecution = (CaseExecutionImpl) caseExecution;
  }

  public CmmnExecution getSourceCaseExecution() {
    return sourceCaseExecution;
  }

  public void setSourceCaseExecution(CmmnExecution sourceCaseExecution) {
    this.sourceCaseExecution = (CaseExecutionImpl) sourceCaseExecution;
  }

  public String getId() {
    return String.valueOf(System.identityHashCode(this));
  }

  public String getCaseInstanceId() {
    if (caseInstance != null) {
      return caseInstance.getId();
    }
    return null;
  }

  public String getCaseExecutionId() {
    if (caseExecution != null) {
      return caseExecution.getId();
    }
    return null;
  }

  public String getSourceCaseExecutionId() {
    if (sourceCaseExecution != null) {
      return sourceCaseExecution.getId();
    }
    return null;
  }

}

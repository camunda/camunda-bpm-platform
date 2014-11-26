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

import java.io.Serializable;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CmmnSentryPart implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String type;
  protected String sentryId;
  protected String standardEvent;
  protected boolean satisfied = false;

  public abstract CmmnExecution getCaseInstance();

  public abstract void setCaseInstance(CmmnExecution caseInstance);

  public abstract CmmnExecution getCaseExecution();

  public abstract void setCaseExecution(CmmnExecution caseExecution);

  public String getSentryId() {
    return sentryId;
  }

  public void setSentryId(String sentryId) {
    this.sentryId = sentryId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public abstract String getSourceCaseExecutionId();

  public abstract CmmnExecution getSourceCaseExecution();

  public abstract void setSourceCaseExecution(CmmnExecution sourceCaseExecution);

  public String getStandardEvent() {
    return standardEvent;
  }

  public void setStandardEvent(String standardEvent) {
    this.standardEvent = standardEvent;
  }

  public boolean isSatisfied() {
    return satisfied;
  }

  public void setSatisfied(boolean satisfied) {
    this.satisfied = satisfied;
  }

}

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
package org.camunda.bpm.engine.impl.cmmn.model;

import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;


/**
 * @author Roman Smirnov
 *
 */
public class CmmnCaseDefinition extends CmmnActivity {

  private static final long serialVersionUID = 1L;

  public CmmnCaseDefinition(String id) {
    super(id, null);
    caseDefinition = this;
  }

  public CmmnCaseInstance createCaseInstance() {
    return createCaseInstance(null);
  }

  public CmmnCaseInstance createCaseInstance(String businessKey) {

    // create a new case instance
    CmmnExecution caseInstance = newCaseInstance();

    // set the definition...
    caseInstance.setCaseDefinition(this);
    // ... and the case instance (identity)
    caseInstance.setCaseInstance(caseInstance);

    // set the business key
    caseInstance.setBusinessKey(businessKey);

    // get the case plan model as "initial" activity
    CmmnActivity casePlanModel = getActivities().get(0);

    // set the case plan model activity
    caseInstance.setActivity(casePlanModel);

    return caseInstance;
  }

  protected CmmnExecution newCaseInstance() {
    return new CaseExecutionImpl();
  }

}

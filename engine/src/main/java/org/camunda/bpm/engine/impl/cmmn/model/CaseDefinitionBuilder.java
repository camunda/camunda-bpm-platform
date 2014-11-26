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

import java.util.Stack;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.StageActivityBehavior;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;

/**
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionBuilder {

  protected CmmnCaseDefinition caseDefinition;
  protected CmmnActivity casePlanModel;
  protected Stack<CmmnActivity> activityStack = new Stack<CmmnActivity>();
  protected CoreModelElement processElement = caseDefinition;

  public CaseDefinitionBuilder() {
    this(null);
  }

  public CaseDefinitionBuilder(String caseDefinitionId) {
    // instantiate case definition
    caseDefinition = new CmmnCaseDefinition(caseDefinitionId);
    activityStack.push(caseDefinition);

    // instantiate casePlanModel of case definition (ie. outermost stage)
    createActivity(caseDefinitionId);
    behavior(new StageActivityBehavior());
  }

  public CaseDefinitionBuilder createActivity(String id) {
    CmmnActivity activity = activityStack.peek().createActivity(id);
    activityStack.push(activity);
    processElement = activity;

    return this;
  }

  public CaseDefinitionBuilder endActivity() {
    activityStack.pop();
    processElement = activityStack.peek();

    return this;
  }

  public CaseDefinitionBuilder behavior(CmmnActivityBehavior behavior) {
    getActivity().setActivityBehavior(behavior);
    return this;
  }

  public CaseDefinitionBuilder autoComplete(boolean autoComplete) {
    getActivity().setProperty("autoComplete", autoComplete);
    return this;
  }

  protected CmmnActivity getActivity() {
    return activityStack.peek();
  }

  public CmmnCaseDefinition buildCaseDefinition() {
    return caseDefinition;
  }

  public CaseDefinitionBuilder listener(String eventName, CaseExecutionListener planItemListener) {
    activityStack.peek().addListener(eventName, planItemListener);
    return this;
  }

  public CaseDefinitionBuilder property(String name, Object value) {
    activityStack.peek().setProperty(name, value);
    return this;
  }

}

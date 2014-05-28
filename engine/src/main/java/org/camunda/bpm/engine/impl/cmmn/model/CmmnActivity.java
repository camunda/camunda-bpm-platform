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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.core.model.CoreActivity;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnActivity extends CoreActivity {

  private static final long serialVersionUID = 1L;

  protected List<CmmnActivity> activities = new ArrayList<CmmnActivity>();
  protected Map<String, CmmnActivity> namedActivities = new HashMap<String, CmmnActivity>();

  protected CmmnElement cmmnElement;

  protected CmmnActivityBehavior activityBehavior;

  protected CmmnCaseDefinition caseDefinition;

  protected CmmnActivity parent;

  public CmmnActivity(String id, CmmnCaseDefinition caseDefinition) {
    super(id);
    this.caseDefinition = caseDefinition;
  }

  // create a new activity ///////////////////////////////////////

  public CmmnActivity createActivity(String activityId) {
    CmmnActivity activity = new CmmnActivity(activityId, caseDefinition);
    if (activityId!=null) {
      namedActivities.put(activityId, activity);
    }
    activity.setParent(this);
    activities.add(activity);
    return activity;
  }

  // activities ////////////////////////////////////////////////

  public List<CmmnActivity> getActivities() {
    return activities;
  }

  // child activity ////////////////////////////////////////////

  public CmmnActivity getChildActivity(String activityId) {
    return namedActivities.get(activityId);
  }

  // behavior //////////////////////////////////////////////////

  public CmmnActivityBehavior getActivityBehavior() {
    return activityBehavior;
  }

  public void setActivityBehavior(CmmnActivityBehavior behavior) {
    this.activityBehavior = behavior;
  }

  // parent ////////////////////////////////////////////////////

  public CmmnActivity getParent() {
    return this.parent;
  }

  public void setParent(CmmnActivity parent) {
    this.parent = parent;
  }

  // case definition

  public CmmnCaseDefinition getCaseDefinition() {
    return caseDefinition;
  }

  public void setCaseDefinition(CmmnCaseDefinition caseDefinition) {
    this.caseDefinition = caseDefinition;
  }

  // cmmn element

  public CmmnElement getCmmnElement() {
    return cmmnElement;
  }

  public void setCmmnElement(CmmnElement cmmnElement) {
    this.cmmnElement = cmmnElement;
  }

}

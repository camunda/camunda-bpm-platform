/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.upgrade.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExpectedActivityInstance {

  /**
   * This is a list in some migration scenarios where the
   * activity id is not clear.
   */
  protected List<String> activityIds = new ArrayList<String>();
  protected List<ExpectedActivityInstance> childActivityInstances = new ArrayList<ExpectedActivityInstance>();
  protected List<ExpectedTransitionInstance> childTransitionInstances = new ArrayList<ExpectedTransitionInstance>();

  public List<String> getActivityIds() {
    return activityIds;
  }
  public void setActivityIds(List<String> activityIds) {
    this.activityIds = activityIds;
  }
  public void setActivityIds(String[] activityIds) {
    this.activityIds = Arrays.asList(activityIds);
  }
  public void setActivityId(String activityId) {
    this.activityIds = Arrays.asList(activityId);
  }
  public List<ExpectedActivityInstance> getChildActivityInstances() {
    return childActivityInstances;
  }
  public void setChildActivityInstances(List<ExpectedActivityInstance> childInstances) {
    this.childActivityInstances = childInstances;
  }
  public List<ExpectedTransitionInstance> getChildTransitionInstances() {
    return childTransitionInstances;
  }
  public void setChildTransitionInstances(List<ExpectedTransitionInstance> childTransitionInstances) {
    this.childTransitionInstances = childTransitionInstances;
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    writeTree(writer, "", true);
    return writer.toString();
  }

  protected void writeTree(StringWriter writer, String prefix, boolean isTail) {
    writer.append(prefix);
    if(isTail) {
      writer.append("└── ");
    } else {
      writer.append("├── ");
    }

    writer.append(getActivityIds() + "\n");

    for (int i = 0; i < childTransitionInstances.size(); i++) {
      ExpectedTransitionInstance transitionInstance = childTransitionInstances.get(i);
      boolean transitionIsTail = (i == (childTransitionInstances.size() - 1))
          && (childActivityInstances.size() == 0);
      writeTransition(transitionInstance, writer, prefix +  (isTail ? "    " : "│   "), transitionIsTail);
    }

    for (int i = 0; i < childActivityInstances.size(); i++) {
      ExpectedActivityInstance child = childActivityInstances.get(i);
      child.writeTree(writer, prefix + (isTail ? "    " : "│   "), (i == (childActivityInstances.size() - 1)));
    }
  }

  protected void writeTransition(ExpectedTransitionInstance transition, StringWriter writer, String prefix, boolean isTail) {
    writer.append(prefix);
    if(isTail) {
      writer.append("└── ");
    } else {
      writer.append("├── ");
    }

    writer.append("transition to/from " + transition.getActivityId() + "\n");
  }
}

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
package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * Expresses that the list of activities should be instantiated and optionally a transition
 * afterwards.
 *
 * @author Thorben Lindhauer
 *
 */
public class InstantiationStack {

  protected List<PvmActivity> activities;
  protected PvmActivity targetActivity;
  protected PvmTransition targetTransition;

  public InstantiationStack(List<PvmActivity> activities) {
    this.activities = activities;
  }

  public InstantiationStack(List<PvmActivity> activities, PvmActivity targetActivity, PvmTransition targetTransition) {
    EnsureUtil.ensureOnlyOneNotNull("target must be either a transition or an activity", targetActivity, targetTransition);
    this.activities = activities;
    // TODO: make this a subclass that contains targetActivity and targetTransition?!
    this.targetActivity = targetActivity;
    this.targetTransition = targetTransition;
  }

  public List<PvmActivity> getActivities() {
    return activities;
  }

  public PvmTransition getTargetTransition() {
    return targetTransition;
  }

  public PvmActivity getTargetActivity() {
    return targetActivity;
  }
}

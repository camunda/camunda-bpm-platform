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
package org.camunda.bpm.engine.impl.cmmn.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
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

  protected List<CmmnSentryDeclaration> sentries = new ArrayList<CmmnSentryDeclaration>();
  protected Map<String, CmmnSentryDeclaration> sentryMap = new HashMap<String, CmmnSentryDeclaration>();

  protected List<CmmnSentryDeclaration> entryCriteria = new ArrayList<CmmnSentryDeclaration>();
  protected List<CmmnSentryDeclaration> exitCriteria = new ArrayList<CmmnSentryDeclaration>();

  // eventName => activity id => variable listeners
  protected Map<String, Map<String, List<VariableListener<?>>>> resolvedVariableListeners;
  protected Map<String, Map<String, List<VariableListener<?>>>> resolvedBuiltInVariableListeners;

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

  public CmmnActivity findActivity(String activityId) {
    return (CmmnActivity) super.findActivity(activityId);
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

  // sentry

  public List<CmmnSentryDeclaration> getSentries() {
    return sentries;
  }

  public CmmnSentryDeclaration getSentry(String sentryId) {
    return sentryMap.get(sentryId);
  }

  public void addSentry(CmmnSentryDeclaration sentry) {
    sentryMap.put(sentry.getId(), sentry);
    sentries.add(sentry);
  }

  // entryCriteria

  public List<CmmnSentryDeclaration> getEntryCriteria() {
    return entryCriteria;
  }

  public void setEntryCriteria(List<CmmnSentryDeclaration> entryCriteria) {
    this.entryCriteria = entryCriteria;
  }

  public void addEntryCriteria(CmmnSentryDeclaration entryCriteria) {
    this.entryCriteria.add(entryCriteria);
  }

  // exitCriteria

  public List<CmmnSentryDeclaration> getExitCriteria() {
    return exitCriteria;
  }

  public void setExitCriteria(List<CmmnSentryDeclaration> exitCriteria) {
    this.exitCriteria = exitCriteria;
  }

  public void addExitCriteria(CmmnSentryDeclaration exitCriteria) {
    this.exitCriteria.add(exitCriteria);
  }

  // variable listeners

  /**
   * Returns a map of all variable listeners defined on this activity or any of
   * its parents activities. The map's key is the id of the respective activity
   * the listener is defined on.
   */
  public Map<String, List<VariableListener<?>>> getVariableListeners(String eventName, boolean includeCustomListeners) {
    Map<String, Map<String, List<VariableListener<?>>>> listenerCache;
    if (includeCustomListeners) {
      if (resolvedVariableListeners == null) {
        resolvedVariableListeners = new HashMap<String, Map<String,List<VariableListener<?>>>>();
      }

      listenerCache = resolvedVariableListeners;
    } else {
      if (resolvedBuiltInVariableListeners == null) {
        resolvedBuiltInVariableListeners = new HashMap<String, Map<String,List<VariableListener<?>>>>();
      }
      listenerCache = resolvedBuiltInVariableListeners;
    }

    Map<String, List<VariableListener<?>>> resolvedListenersForEvent = listenerCache.get(eventName);

    if (resolvedListenersForEvent == null) {
      resolvedListenersForEvent = new HashMap<String, List<VariableListener<?>>>();
      listenerCache.put(eventName, resolvedListenersForEvent);

      CmmnActivity currentActivity = this;

      while (currentActivity != null) {
        List<VariableListener<?>> localListeners = null;
        if (includeCustomListeners) {
          localListeners = currentActivity.getVariableListenersLocal(eventName);
        } else {
          localListeners = currentActivity.getBuiltInVariableListenersLocal(eventName);
        }

        if (localListeners != null && !localListeners.isEmpty()) {
          resolvedListenersForEvent.put(currentActivity.getId(), localListeners);
        }

        currentActivity = currentActivity.getParent();
      }
    }

    return resolvedListenersForEvent;
  }
}

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
package org.camunda.bpm.engine.test.assertions.helpers;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.transformer.AbstractCmmnTransformListener;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.Milestone;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;

/**
 * Created by Malte on 08.09.2015.
 */
public class LoggerCmmnTransformListener extends AbstractCmmnTransformListener {
  public static CaseExecutionListener listener = new CaseExecutionListener() {
    @Override
    public void notify(DelegateCaseExecution var1) throws Exception {
      System.out.printf("Execution Event: %s %s\n", var1.getEventName(), var1.getActivityId());
    }

    ;
  };

  protected void addListeners(CmmnActivity activity) {
    if (activity != null) {
      activity.addBuiltInListener(CaseExecutionListener.CREATE, listener);
      activity.addBuiltInListener(CaseExecutionListener.ENABLE, listener);
      activity.addBuiltInListener(CaseExecutionListener.START, listener);
      activity.addBuiltInListener(CaseExecutionListener.MANUAL_START, listener);
      activity.addBuiltInListener(CaseExecutionListener.COMPLETE, listener);
      activity.addBuiltInListener(CaseExecutionListener.TERMINATE, listener);
      activity.addBuiltInListener(CaseExecutionListener.OCCUR, listener);
    }
  }

  @Override
  public void transformHumanTask(PlanItem planItem, HumanTask humanTask, CmmnActivity activity) {
    addListeners(activity);
  }

  @Override
  public void transformProcessTask(PlanItem planItem, ProcessTask processTask, CmmnActivity activity) {
    addListeners(activity);
  }

  @Override
  public void transformCaseTask(PlanItem planItem, CaseTask caseTask, CmmnActivity activity) {
    addListeners(activity);
  }

  @Override
  public void transformTask(PlanItem planItem, Task task, CmmnActivity activity) {
    addListeners(activity);
  }

  @Override
  public void transformStage(PlanItem planItem, Stage stage, CmmnActivity activity) {
    addListeners(activity);
  }

  @Override
  public void transformMilestone(PlanItem planItem, Milestone milestone, CmmnActivity activity) {
    addListeners(activity);
  }
}


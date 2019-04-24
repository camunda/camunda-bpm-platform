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
package org.camunda.bpm.engine.test.cmmn.handler;

import java.util.Arrays;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler;
import org.camunda.bpm.engine.test.cmmn.handler.specification.AbstractExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.ClassExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.DelegateExpressionExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.ExpressionExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.ScriptExecutionListenerSpec;

public class ExecutionListenerCases {

  public static final Iterable<Object[]> TASK_OR_STAGE_CASES =
      Arrays.asList(new Object[][] {
          // class delegate
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.START)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new ClassExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.TASK_OR_STAGE_EVENTS)},

          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldExpression("firstField", "${myFirstExpression}")
            .withFieldExpression("secondField", "${mySecondExpression}")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildExpression("firstField", "${myFirstExpression}")
            .withFieldChildExpression("secondField", "${mySecondExpression}")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldStringValue("firstField", "aFirstFixedValue")
            .withFieldStringValue("secondField", "aSecondFixedValue")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildStringValue("firstField", "aFirstFixedValue")
            .withFieldChildStringValue("secondField", "aSecondFixedValue")},

          // script
          {new ScriptExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.START)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new ScriptExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.TASK_OR_STAGE_EVENTS)},

          // delegate expression
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.START)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new DelegateExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.TASK_OR_STAGE_EVENTS)},

          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldExpression("firstField", "${myFirstExpression}")
            .withFieldExpression("secondField", "${mySecondExpression}")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildExpression("firstField", "${myFirstExpression}")
            .withFieldChildExpression("secondField", "${mySecondExpression}")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldStringValue("firstField", "aFirstFixedValue")
            .withFieldStringValue("secondField", "aSecondFixedValue")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildStringValue("firstField", "aFirstFixedValue")
            .withFieldChildStringValue("secondField", "aSecondFixedValue")},

          // expression
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.START)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new ExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.TASK_OR_STAGE_EVENTS)}
      });


  public static final Iterable<Object[]> EVENTLISTENER_OR_MILESTONE_CASES =
      Arrays.asList(new Object[][] {
          // class delegate
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.OCCUR)},
          {new ClassExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.EVENT_LISTENER_OR_MILESTONE_EVENTS)},

          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldExpression("firstField", "${myFirstExpression}")
            .withFieldExpression("secondField", "${mySecondExpression}")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildExpression("firstField", "${myFirstExpression}")
            .withFieldChildExpression("secondField", "${mySecondExpression}")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldStringValue("firstField", "aFirstFixedValue")
            .withFieldStringValue("secondField", "aSecondFixedValue")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildStringValue("firstField", "aFirstFixedValue")
            .withFieldChildStringValue("secondField", "aSecondFixedValue")},

          // script
          {new ScriptExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.OCCUR)},
          {new ScriptExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.EVENT_LISTENER_OR_MILESTONE_EVENTS)},

          // delegate expression
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.OCCUR)},
          {new DelegateExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.EVENT_LISTENER_OR_MILESTONE_EVENTS)},

          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldExpression("firstField", "${myFirstExpression}")
            .withFieldExpression("secondField", "${mySecondExpression}")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildExpression("firstField", "${myFirstExpression}")
            .withFieldChildExpression("secondField", "${mySecondExpression}")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldStringValue("firstField", "aFirstFixedValue")
            .withFieldStringValue("secondField", "aSecondFixedValue")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildStringValue("firstField", "aFirstFixedValue")
            .withFieldChildStringValue("secondField", "aSecondFixedValue")},

          // expression
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.OCCUR)},
          {new ExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.EVENT_LISTENER_OR_MILESTONE_EVENTS)},
      });

  public static final Iterable<Object[]> CASE_PLAN_MODEL_CASES =
      Arrays.asList(new Object[][] {
          // class delegate
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ClassExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.CASE_PLAN_MODEL_EVENTS)},

          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldExpression("firstField", "${myFirstExpression}")
            .withFieldExpression("secondField", "${mySecondExpression}")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildExpression("firstField", "${myFirstExpression}")
            .withFieldChildExpression("secondField", "${mySecondExpression}")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldStringValue("firstField", "aFirstFixedValue")
            .withFieldStringValue("secondField", "aSecondFixedValue")},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildStringValue("firstField", "aFirstFixedValue")
            .withFieldChildStringValue("secondField", "aSecondFixedValue")},

          // script
          {new ScriptExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ScriptExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.CASE_PLAN_MODEL_EVENTS)},

          // delegate expression
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new DelegateExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.CASE_PLAN_MODEL_EVENTS)},

          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldExpression("firstField", "${myFirstExpression}")
            .withFieldExpression("secondField", "${mySecondExpression}")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildExpression("firstField", "${myFirstExpression}")
            .withFieldChildExpression("secondField", "${mySecondExpression}")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldStringValue("firstField", "aFirstFixedValue")
            .withFieldStringValue("secondField", "aSecondFixedValue")},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)
            .withFieldChildStringValue("firstField", "aFirstFixedValue")
            .withFieldChildStringValue("secondField", "aSecondFixedValue")},

          // expression
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ItemHandler.CASE_PLAN_MODEL_EVENTS)}
      });


}

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
package org.camunda.bpm.engine.test.cmmn.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.test.cmmn.handler.specification.AbstractExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.ClassExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.DelegateExpressionExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.ExpressionExecutionListenerSpec;
import org.camunda.bpm.engine.test.cmmn.handler.specification.ScriptExecutionListenerSpec;

public class ExecutionListenerCases {

  public static final Set<String> ALL_EXECUTION_EVENTS =
      new HashSet<String>(Arrays.asList(new String[] {
          CaseExecutionListener.CREATE,
          CaseExecutionListener.COMPLETE,
          CaseExecutionListener.DISABLE,
          CaseExecutionListener.ENABLE,
          CaseExecutionListener.EXIT,
          CaseExecutionListener.MANUAL_START,
          CaseExecutionListener.PARENT_RESUME,
          CaseExecutionListener.PARENT_SUSPEND,
          CaseExecutionListener.RE_ENABLE,
          CaseExecutionListener.RESUME,
          CaseExecutionListener.START,
          CaseExecutionListener.SUSPEND,
          CaseExecutionListener.TERMINATE
      }));

  public static final Set<String> PLAN_RELEVANT_EXECUTION_EVENTS =
      new HashSet<String>(Arrays.asList(new String[] {
          CaseExecutionListener.CREATE,
          CaseExecutionListener.CLOSE,
          CaseExecutionListener.COMPLETE,
          CaseExecutionListener.RE_ACTIVATE,
          CaseExecutionListener.SUSPEND,
          CaseExecutionListener.TERMINATE
      }));

  public static final Iterable<Object[]> ITEM_CASES =
      Arrays.asList(new Object[][] {
          // class delegate
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.START)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ClassExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ALL_EXECUTION_EVENTS)},

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
          {new ScriptExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.START)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ScriptExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ALL_EXECUTION_EVENTS)},

          // delegate expression
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.START)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new DelegateExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ALL_EXECUTION_EVENTS)},

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
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.DISABLE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.ENABLE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.EXIT)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.MANUAL_START)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_RESUME)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_SUSPEND)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.PARENT_TERMINATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RE_ENABLE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RESUME)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.START)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(ALL_EXECUTION_EVENTS)}
      });

  public static final Iterable<Object[]> PLAN_CASES =
      Arrays.asList(new Object[][] {
          // class delegate
          {new ClassExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ClassExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ClassExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(PLAN_RELEVANT_EXECUTION_EVENTS)},

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
          {new ScriptExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ScriptExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ScriptExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(PLAN_RELEVANT_EXECUTION_EVENTS)},

          // delegate expression
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CREATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new DelegateExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new DelegateExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(PLAN_RELEVANT_EXECUTION_EVENTS)},

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
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.CLOSE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.COMPLETE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.RE_ACTIVATE)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.SUSPEND)},
          {new ExpressionExecutionListenerSpec(CaseExecutionListener.TERMINATE)},
          {new ExpressionExecutionListenerSpec(AbstractExecutionListenerSpec.ANY_EVENT)
              .expectRegistrationFor(PLAN_RELEVANT_EXECUTION_EVENTS)}
      });


}

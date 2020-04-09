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
package org.camunda.bpm.engine.test.standalone.deploy;

import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse.COMPENSATE_EVENT_DEFINITION;

import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.CompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 * @author Frederik Heremans
 */
public class TestBPMNParseListener extends AbstractBpmnParseListener {

  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
    // Change the key of all deployed process-definitions
    for (ProcessDefinitionEntity entity : processDefinitions) {
      entity.setKey(entity.getKey() + "-modified");
    }
  }

  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    // Change activity behavior
    startEventActivity.setActivityBehavior(new TestNoneStartEventActivityBehavior());
  }

  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    // Change activity behavior
    Element compensateEventDefinitionElement = intermediateEventElement.element(COMPENSATE_EVENT_DEFINITION);
    if (compensateEventDefinitionElement != null) {
      final String activityRef = compensateEventDefinitionElement.attribute("activityRef");
      CompensateEventDefinition compensateEventDefinition = new CompensateEventDefinition();
      compensateEventDefinition.setActivityRef(activityRef);
      compensateEventDefinition.setWaitForCompletion(false);

      activity.setActivityBehavior(new TestCompensationEventActivityBehavior(compensateEventDefinition));
    }
  }

  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    // Change activity behavior
    activity.setActivityBehavior(new TestNoneEndEventActivityBehavior());
  }

  public class TestNoneStartEventActivityBehavior extends NoneStartEventActivityBehavior {

  }

  public class TestNoneEndEventActivityBehavior extends NoneEndEventActivityBehavior {

  }

  public class TestCompensationEventActivityBehavior extends CompensationEventActivityBehavior {

    public TestCompensationEventActivityBehavior(CompensateEventDefinition compensateEventDefinition) {
      super(compensateEventDefinition);
    }
  }

}

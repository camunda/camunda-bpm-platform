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
package org.camunda.connect.plugin.impl;

import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseUtil.findCamundaExtensionElement;
import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseUtil.parseInputOutput;

import org.camunda.bpm.engine.BpmnParseException;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class ConnectorParseListener extends AbstractBpmnParseListener {

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseConnectorElement(serviceTaskElement, scope, activity);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    Element messageEventDefinitionElement = endEventElement.element(BpmnParse.MESSAGE_EVENT_DEFINITION);

    if (messageEventDefinitionElement != null) {
      parseConnectorElement(messageEventDefinitionElement, scope, activity);
    }
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    Element messageEventDefinitionElement = intermediateEventElement.element(BpmnParse.MESSAGE_EVENT_DEFINITION);

    if (messageEventDefinitionElement != null) {
      parseConnectorElement(messageEventDefinitionElement, scope, activity);
    }
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseConnectorElement(businessRuleTaskElement, scope, activity);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    parseConnectorElement(sendTaskElement, scope, activity);
  }

  protected void parseConnectorElement(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    Element connectorDefinition = findCamundaExtensionElement(serviceTaskElement, "connector");
    if (connectorDefinition != null) {
      Element connectorIdElement = connectorDefinition.element("connectorId");

      String connectorId = null;
      if (connectorIdElement != null)  {
        connectorId = connectorIdElement.getText().trim();
      }
      if (connectorIdElement == null || connectorId.isEmpty()) {
        throw new BpmnParseException("No 'id' defined for connector.", connectorDefinition);
      }

      IoMapping ioMapping = parseInputOutput(connectorDefinition);
      activity.setActivityBehavior(new ServiceTaskConnectorActivityBehavior(connectorId, ioMapping));
    }
  }

}

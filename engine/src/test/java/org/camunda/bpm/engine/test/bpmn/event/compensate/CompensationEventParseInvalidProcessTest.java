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
package org.camunda.bpm.engine.test.bpmn.event.compensate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.AssertionFailedError;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.Problem;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Parse an invalid process definition and assert the error message.
 *
 * @author Philipp Ossler
 */
@RunWith(Parameterized.class)
public class CompensationEventParseInvalidProcessTest {

  private static final String PROCESS_DEFINITION_DIRECTORY = "org/camunda/bpm/engine/test/bpmn/event/compensate/";

  @Parameters(name = "{index}: process definition = {0}, expected error message = {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "CompensationEventParseInvalidProcessTest.illegalCompensateActivityRefParentScope.bpmn20.xml", "Invalid attribute value for 'activityRef': no activity with id 'someServiceInMainProcess' in scope 'subProcess'", new String[] { "throwCompensate" } },
        { "CompensationEventParseInvalidProcessTest.illegalCompensateActivityRefNestedScope.bpmn20.xml", "Invalid attribute value for 'activityRef': no activity with id 'someServiceInNestedScope' in scope 'subProcess'", new String[] { "throwCompensate" } },
        { "CompensationEventParseInvalidProcessTest.invalidActivityRefFails.bpmn20.xml", "Invalid attribute value for 'activityRef':", new String[]{"throwCompensate"} },
        { "CompensationEventParseInvalidProcessTest.multipleCompensationCatchEventsCompensationAttributeMissingFails.bpmn20.xml", "compensation boundary catch must be connected to element with isForCompensation=true", new String[] { "compensateBookHotelEvt", "undoBookHotel", "Association" } },
        { "CompensationEventParseInvalidProcessTest.multipleCompensationCatchEventsFails.bpmn20.xml", "multiple boundary events with compensateEventDefinition not supported on same activity", new String[] { "compensateBookHotelEvt2" } },
        { "CompensationEventParseInvalidProcessTest.multipleCompensationEventSubProcesses.bpmn20.xml", "multiple event subprocesses with compensation start event are not supported on the same scope", new String[] { "startInCompensationScope2" } },
        { "CompensationEventParseInvalidProcessTest.compensationEventSubProcessesAtProcessLevel.bpmn20.xml", "event subprocess with compensation start event is only supported for embedded subprocess", new String[] { "startInCompensationScope" } },
        { "CompensationEventParseInvalidProcessTest.compensationEventSubprocessAndBoundaryEvent.bpmn20.xml", "compensation boundary event and event subprocess with compensation start event are not supported on the same scope", new String[] { "subprocess", "compensateSubProcess" } },
        { "CompensationEventParseInvalidProcessTest.invalidOutgoingSequenceflow.bpmn20.xml", "Invalid outgoing sequence flow of compensation activity 'undoTask'. A compensation activity should not have an incoming or outgoing sequence flow.", new String[] { "undoTask" } },
        { "CompensationEventParseInvalidProcessTest.invalidIncomingSequenceflow.bpmn20.xml", "Invalid incoming sequence flow of compensation activity 'task'. A compensation activity should not have an incoming or outgoing sequence flow.", new String[] { "task" } }
    });
  }

  @Parameter(0)
  public String processDefinitionResource;

  @Parameter(1)
  public String expectedErrorMessage;

  @Parameter(2)
  public String[] bpmnElementIds;

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected RepositoryService repositoryService;

  @Before
  public void initServices() {
    repositoryService = rule.getRepositoryService();
  }

  @Test
  public void testParseInvalidProcessDefinition() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource(PROCESS_DEFINITION_DIRECTORY + processDefinitionResource)
        .deploy();

      fail("exception expected: " + expectedErrorMessage);
    } catch (ParseException e) {
      assertExceptionMessageContainsText(e, expectedErrorMessage);
      List<Problem> errors = e.getResorceReports().get(0).getErrors();
      assertThat(errors.size()).isEqualTo(1);
      assertThat(errors.get(0).getMainElementId()).isEqualTo(bpmnElementIds[0]);
      if (bpmnElementIds.length == 2) {
        assertThat(errors.get(0).getElementIds()).containsExactlyInAnyOrder(bpmnElementIds);
      }
    }
  }

  public void assertExceptionMessageContainsText(Exception e, String expectedMessage) {
    String actualMessage = e.getMessage();
    if (actualMessage == null || !actualMessage.contains(expectedMessage)) {
      throw new AssertionFailedError("expected presence of [" + expectedMessage + "], but was [" + actualMessage + "]");
    }
  }
}

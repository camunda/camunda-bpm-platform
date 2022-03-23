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
package org.camunda.bpm.engine.test.assertions.cmmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.bpm.engine.test.assertions.helpers.CaseExecutionQueryFluentAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * You will notice that this test class does not cover all methods.
 * That's because simple, one-liner boilerplate code is not regarded as test-worthy.
 * That code is so simplistic, it is not expected to break easily.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CmmnAwareTestsTest {

  public static final String ACTIVITY_ID = "FOO";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private CaseInstance caseInstance;

  @Mock
  private CaseExecution caseExecution;

  @Mock
  private CaseDefinition caseDefinition;

  @Mock
  private ProcessEngine processEngine;

  @Mock
  private CaseService caseService;

  //mocked below
  private CaseExecutionQuery caseExecutionQuery;
  private MockedStatic<CmmnAwareTests> cmmnAwareTestsMockedStatic;
  private MockedStatic<AbstractAssertions> abstractAssertionsMockedStatic;

  @Before
  public void setUp() {
    cmmnAwareTestsMockedStatic = mockStatic(CmmnAwareTests.class, CALLS_REAL_METHODS);
    abstractAssertionsMockedStatic = mockStatic(AbstractAssertions.class);
    abstractAssertionsMockedStatic.when(AbstractAssertions::processEngine).thenReturn(processEngine);
    when(processEngine.getCaseService()).thenReturn(caseService);

    caseExecutionQuery = mock(CaseExecutionQuery.class, new CaseExecutionQueryFluentAnswer());
    when(caseService.createCaseExecutionQuery()).thenReturn(caseExecutionQuery);
  }

  @After
  public void tearDown() {
    cmmnAwareTestsMockedStatic.close();
    abstractAssertionsMockedStatic.close();
  }

  @Test
  public void assertThatCaseDefinition_should_delegate_to_CmmnAwareAssertions() {

    //prepare and mock static methods
    //because we need control over the CaseDefinitionAssert created
    final MockedStatic<CaseDefinitionAssert> caseDefinitionAssertMockedStatic = mockStatic(CaseDefinitionAssert.class);
    CaseDefinitionAssert caseDefinitionAssert = mock(CaseDefinitionAssert.class);
    caseDefinitionAssertMockedStatic.when(() -> CaseDefinitionAssert.assertThat(Mockito.any(), any())).thenReturn(caseDefinitionAssert);

    //when calling the method under test with a non-null CaseDefinition object
    CaseDefinitionAssert actualCaseDefinitionAssert = CmmnAwareTests.assertThat(caseDefinition);

    //then the delegate is called with that CaseDefinition object
    caseDefinitionAssertMockedStatic.verify(() -> CaseDefinitionAssert.assertThat(Mockito.any(), any()));
    //and whatever the delegate returns, is returned by the tested method, too
    Assertions.assertThat(actualCaseDefinitionAssert).isSameAs(caseDefinitionAssert);

    caseDefinitionAssertMockedStatic.close();
  }

  @Test
  public void assertThatCaseExecution_should_delegate_to_CmmnAwareAssertions() {

    //prepare and mock static methods
    //because we need control over the CaseExecutionAssert created
    final MockedStatic<CaseExecutionAssert> caseExecutionAssertMockedStatic = mockStatic(CaseExecutionAssert.class);
    CaseExecutionAssert caseExecutionAssert = mock(CaseExecutionAssert.class);
    caseExecutionAssertMockedStatic.when(() -> CaseExecutionAssert.assertThat(Mockito.any(), any())).thenReturn(caseExecutionAssert);

    //when calling the method under test with a non-null CaseExecution object
    CaseExecutionAssert actualCaseExecutionAssert = CmmnAwareTests.assertThat(caseExecution);

    //then the delegate is called with that CaseExecution object
    caseExecutionAssertMockedStatic.verify(() -> CaseExecutionAssert.assertThat(Mockito.any(), any()));
    //and whatever the delegate returns, is returned by the tested method, too
    Assertions.assertThat(actualCaseExecutionAssert).isSameAs(caseExecutionAssert);

    caseExecutionAssertMockedStatic.close();
  }

  @Test
  public void assertThatCaseInstance_should_delegate_to_CmmnAwareAssertions() {

    //prepare and mock static methods
    //because we need control over the CaseInstanceAssert created
    final MockedStatic<CaseInstanceAssert> caseInstanceAssertMockedStatic = mockStatic(CaseInstanceAssert.class);
    CaseInstanceAssert caseInstanceAssert = mock(CaseInstanceAssert.class);
    caseInstanceAssertMockedStatic.when(() -> CaseInstanceAssert.assertThat(any(), any())).thenReturn(caseInstanceAssert);

    //when calling the method under test with a non-null CaseInstance object
    CaseInstanceAssert actualCaseInstanceAssert = CmmnAwareTests.assertThat(caseInstance);

    //then the delegate is called with that CaseInstance object
    caseInstanceAssertMockedStatic.verify(() -> CaseInstanceAssert.assertThat(any(), any()));
    //and whatever the delegate returns, is returned by the tested method, too
    Assertions.assertThat(actualCaseInstanceAssert).isSameAs(caseInstanceAssert);

    caseInstanceAssertMockedStatic.close();
  }

  @Test
  public void caseDefinitionQuery_should_create_vanilla_query() {

    //prepare and mock static methods
    //because we need control over the CaseDefinitionQuery created
    final MockedStatic<BpmnAwareTests> bpmnAwareTestsMockedStatic = mockStatic(BpmnAwareTests.class);
    RepositoryService repositoryService = mock(RepositoryService.class);
    bpmnAwareTestsMockedStatic.when(BpmnAwareTests::repositoryService).thenReturn(repositoryService);

    CaseDefinitionQuery caseDefinitionQuery = mock(CaseDefinitionQuery.class);
    when(repositoryService.createCaseDefinitionQuery()).thenReturn(caseDefinitionQuery);

    //when getting a CaseDefinitionQuery
    CaseDefinitionQuery actualCaseDefinitionQuery = CmmnAwareTests.caseDefinitionQuery();

    //then the RepositoryService is used for creating a new one
    verify(repositoryService).createCaseDefinitionQuery();
    //and the new CaseDefinitionQuery is returned from the tested method
    Assertions.assertThat(actualCaseDefinitionQuery).isSameAs(caseDefinitionQuery);
    //and the returned CaseDefinitionQuery is a vanilla one
    verifyNoMoreInteractions(caseDefinitionQuery);

    bpmnAwareTestsMockedStatic.close();
  }

  @Test
  public void caseExecutionCaseExecutionQueryCaseInstance_should_delegate_to_assertThatCaseInstance() {
    final MockedStatic<CaseInstanceAssert> caseInstanceAssertMockedStatic = mockStatic(CaseInstanceAssert.class);
    CaseInstanceAssert caseInstanceAssert = mock(CaseInstanceAssert.class);
    when(caseInstanceAssert.isNotNull()).thenReturn(caseInstanceAssert);
    when(caseInstanceAssert.descendantCaseExecution(caseExecutionQuery)).thenReturn(mock(CaseExecutionAssert.class));

    //prepare and mock static methods
    //because we need control over the context of tested method
    caseInstanceAssertMockedStatic.when(() -> CaseInstanceAssert.assertThat(any(), any())).thenReturn(caseInstanceAssert);

    //when getting the CaseExecution for a CaseInstance and a given CaseExecutionQuery
    CmmnAwareTests.caseExecution(caseExecutionQuery, caseInstance);

    //then the call is delegated via assertThat to a CaseInstanceAssert
    cmmnAwareTestsMockedStatic.verify(() -> CmmnAwareTests.assertThat(caseInstance));
    caseInstanceAssertMockedStatic.verify(() -> CaseInstanceAssert.assertThat(any(), any()));

    //and the CaseExecutionQuery is used for narrowing down the result
    verify(caseInstanceAssert).descendantCaseExecution(caseExecutionQuery);
    verifyNoMoreInteractions(caseExecutionQuery);

    caseInstanceAssertMockedStatic.close();
  }

  @Test
  public void caseExecutionQuery_should_create_vanilla_query() {
    when(caseService.createCaseExecutionQuery()).thenReturn(caseExecutionQuery);

    //when getting a CaseExecutionQuery
    CaseExecutionQuery actualCaseExecutionQuery = CmmnAwareTests.caseExecutionQuery();

    //then the CaseService is used for creating a new one
    verify(caseService).createCaseExecutionQuery();
    //and the new CaseExecutionQuery is returned from the tested method
    Assertions.assertThat(actualCaseExecutionQuery).isSameAs(caseExecutionQuery);
    //and the returned CaseExecutionQuery is a vanilla one
    verifyNoMoreInteractions(caseExecutionQuery);
  }

  @Test
  public void caseExecutionStringCaseInstance_should_delegate_to_its_query_variant() {
    final MockedStatic<CaseInstanceAssert> caseInstanceAssertMockedStatic = mockStatic(CaseInstanceAssert.class);
    final CaseInstanceAssert caseInstanceAssert = mock(CaseInstanceAssert.class);
    caseInstanceAssertMockedStatic.when(() -> CaseInstanceAssert.assertThat(any(), eq(caseInstance))).thenReturn(caseInstanceAssert);
    when(caseInstanceAssert.isNotNull()).thenReturn(caseInstanceAssert);
    final CaseExecutionAssert caseExecutionAssert = mock(CaseExecutionAssert.class);
    when(caseInstanceAssert.descendantCaseExecution(any())).thenReturn(caseExecutionAssert);
    when(caseExecutionAssert.getActual()).thenReturn(caseExecution);

    //when getting the CaseExecution for activity FOO in a given case instance
    CmmnAwareTests.caseExecution(ACTIVITY_ID, caseInstance);

    //then the caseExecutionQuery is enhanced with the activityId only
    verify(caseExecutionQuery).activityId(ACTIVITY_ID);
    caseInstanceAssertMockedStatic.verify(() -> CaseInstanceAssert.assertThat(any(), eq(caseInstance)));
    verifyNoMoreInteractions(caseExecutionQuery);
    //and the call is delegated properly
    cmmnAwareTestsMockedStatic.verify(() -> CmmnAwareTests.caseExecution(caseExecutionQuery, caseInstance));

    caseInstanceAssertMockedStatic.close();
  }

  @Test
  public void caseInstanceQuery_should_create_vanilla_query() {
    CaseInstanceQuery caseInstanceQuery = mock(CaseInstanceQuery.class);
    when(caseService.createCaseInstanceQuery()).thenReturn(caseInstanceQuery);

    //when getting a CaseInstanceQuery
    CaseInstanceQuery actualCaseInstanceQuery = CmmnAwareTests.caseInstanceQuery();

    //then the CaseService is used for creating a new one
    verify(caseService).createCaseInstanceQuery();
    //and the new CaseInstanceQuery is returned from the tested method
    Assertions.assertThat(actualCaseInstanceQuery).isSameAs(caseInstanceQuery);
    //and the returned CaseInstanceQuery is a vanilla one
    verifyNoMoreInteractions(caseInstanceQuery);
  }

  @Test
  public void caseService_should_return_the_processEngines_caseService() {
    //when getting a CaseInstanceQuery
    CaseService actualCaseService = CmmnAwareTests.caseService();

    //then the returned CaseService is the same as
    Assertions.assertThat(actualCaseService).isSameAs(caseService);
  }

  @Test
  public void completeCaseExecution_should_delegate_to_caseService() {
    when(caseExecution.getId()).thenReturn("baz");

    CmmnAwareTests.complete(caseExecution);

    verify(caseService).completeCaseExecution("baz");
  }

  @Test
  public void completeCaseExecution_should_throw_IAE_for_null_arg() {
    thrown.expect(IllegalArgumentException.class);

    CmmnAwareTests.complete((CaseExecution) null);
  }

  @Test
  public void disableCaseExecution_should_delegate_to_caseService() {
    when(caseExecution.getId()).thenReturn("baz");

    CmmnAwareTests.disable(caseExecution);

    verify(caseService).disableCaseExecution("baz");
  }

  @Test
  public void disableCaseExecution_should_throw_IAE_for_null_arg() {
    thrown.expect(IllegalArgumentException.class);

    CmmnAwareTests.disable((CaseExecution) null);
  }

  @Test
  public void manuallyStartCaseExecution_should_delegate_to_caseService() {
    when(caseExecution.getId()).thenReturn("baz");

    CmmnAwareTests.manuallyStart(caseExecution);

    verify(caseService).manuallyStartCaseExecution("baz");
  }

  @Test
  public void manuallyStartCaseExecution_should_throw_IAE_for_null_arg() {
    thrown.expect(IllegalArgumentException.class);

    CmmnAwareTests.manuallyStart((CaseExecution) null);
  }

  @Test
  public void completeCaseExecutionWithVariables_should_delegate_to_caseService() {
    when(caseExecution.getId()).thenReturn("baz");

    CmmnAwareTests.complete(caseExecution, withVariables("aVariable", "aValue"));

    verify(caseService).completeCaseExecution("baz", withVariables("aVariable", "aValue"));
  }

  @Test
  public void completeCaseExecutionWithVariables_should_throw_IAE_for_null_arg_caseExecution() {
    thrown.expect(IllegalArgumentException.class);

    CmmnAwareTests.complete((CaseExecution) null, withVariables("aVariable", "aValue"));
  }

  @Test
  public void completeCaseExecutionWithVariables_should_throw_IAE_for_null_arg_valriables() {
    thrown.expect(IllegalArgumentException.class);

    CmmnAwareTests.complete(caseExecution, null);
  }

}

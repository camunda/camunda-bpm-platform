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


import static java.lang.String.format;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;

/**
 * Convenience class to access camunda *BPMN* and *CMMN*
 * related Assertions PLUS helper methods. Use it with a static import:
 *
 * import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.*;
 *
 */
public class CmmnAwareTests extends BpmnAwareTests {

  /**
   * Assert that... the given CaseInstance meets your expectations.
   *
   * @param   actual CaseInstance under test
   * @return  Assert object offering CaseInstance specific assertions.
   */
  public static CaseInstanceAssert assertThat(final CaseInstance actual) {
    return CaseInstanceAssert.assertThat(processEngine(), actual);
  }

  /**
   * Assert that... the given CaseExecution meets your expectations.
   *
   * @param   actual CaseExecution under test
   * @return  Assert object offering CaseExecution specific assertions.
   */
  public static CaseExecutionAssert assertThat(final CaseExecution actual) {
    return CaseExecutionAssert.assertThat(processEngine(), actual);
  }

  /**
   * Assert that... the given CaseDefinition meets your expectations.
   *
   * @param   actual ProcessDefinition under test
   * @return  Assert object offering ProcessDefinition specific assertions.
   */
  public static CaseDefinitionAssert assertThat(final CaseDefinition actual) {
    return CaseDefinitionAssert.assertThat(processEngine(), actual);
  }

  /**
   * Helper method to easily access CaseService
   *
   * @return  CaseService of process engine bound to this testing thread
   * @see     org.camunda.bpm.engine.CaseService
   */
  public static CaseService caseService() {
    return processEngine().getCaseService();
  }

  /**
   * Helper method to easily create a new CaseInstanceQuery.
   * @return new CaseInstanceQuery for process engine bound to this testing thread
   */
  public static CaseInstanceQuery caseInstanceQuery() {
    return caseService().createCaseInstanceQuery();
  }

  /**
   * Helper method to easily create a new CaseExecutionQuery.
   * @return new CaseExecutionQuery for process engine bound to this testing thread
   */
  public static CaseExecutionQuery caseExecutionQuery() {
    return caseService().createCaseExecutionQuery();
  }

  /**
   * Helper method to easily create a new CaseDefinitionQuery.
   * @return new CaseExecutionQuery for process engine bound to this testing thread
   */
  public static CaseDefinitionQuery caseDefinitionQuery() {
    return repositoryService().createCaseDefinitionQuery();
  }

  /**
   * Helper method to easily complete a CaseExecution.
   *
   * @param caseExecution the CaseExecution to complete
   */
  public static void complete(CaseExecution caseExecution) {
    if (caseExecution == null) {
      throw new IllegalArgumentException("Illegal call of complete(caseExecution) - must not be null!");
    }
    caseService().completeCaseExecution(caseExecution.getId());
  }

  /**
   * Helper method to easily disable a case execution.
   *
   * @param caseExecution
   *        the case execution to complete
   */
  public static void disable(CaseExecution caseExecution) {
    if (caseExecution == null) {
      throw new IllegalArgumentException("Illegal call of disable(caseExecution) - must not be null!");
    }
    caseService().disableCaseExecution(caseExecution.getId());
  }

  /**
   * Helper method to manually start a case execution.
   *
   * @param caseExecution
   *        the case execution to start
   */
  public static void manuallyStart(CaseExecution caseExecution) {
    if (caseExecution == null) {
      throw new IllegalArgumentException("Illegal call of manuallyStart(caseExecution) - must not be null!");
    }
    caseService().manuallyStartCaseExecution(caseExecution.getId());
  }

	/**
     * Helper method to find any {@link CaseExecution} in the context of a CaseInstance.
     * @param activityId activity to find
     * @param caseInstance CaseInstance to search in
     * @return CaseExecution or null
     */
  public static CaseExecution caseExecution(String activityId, CaseInstance caseInstance) {
    Assertions.assertThat(activityId).isNotNull();
    return caseExecution(caseExecutionQuery().activityId(activityId), caseInstance);
  }

  /**
   * Helper method to find any {@link CaseExecution} in the context of a CaseInstance
   * @param caseExecutionQuery query for narrowing down on the CaseExecution to find
   * @param caseInstance CaseInstance to search in
   * @return CaseExecution or null
     */
  public static CaseExecution caseExecution(CaseExecutionQuery caseExecutionQuery, CaseInstance caseInstance) {
    return assertThat(caseInstance).isNotNull().descendantCaseExecution(caseExecutionQuery).getActual();
  }

  /**
   * Helper method to easily complete a caseExecution and pass some
   * case variables.
   *
   * @param   caseExecution CaseExecution to be completed
   * @param   variables Case variables to be passed to the
   *          case instance when completing the caseExecution. For
   *          setting those variables, you can use
   *          {@link #withVariables(String, Object, Object...)}
   */
  public static void complete(CaseExecution caseExecution, Map<String, Object> variables) {
    if (caseExecution == null || variables == null) {
      throw new IllegalArgumentException(format("Illegal call of complete(caseExecution = '%s', variables = '%s') - both must not be null!", caseExecution, variables));
    }
    caseService().completeCaseExecution(caseExecution.getId(), variables);
  }

}

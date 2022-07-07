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
package org.camunda.bpm.engine.test.errorcode.conf;

import ch.qos.logback.classic.Level;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode;
import org.camunda.bpm.engine.impl.errorcode.ExceptionCodeProvider;
import org.camunda.bpm.engine.test.errorcode.FailingJavaDelegateWithCustomException;
import org.camunda.bpm.engine.test.errorcode.FailingJavaDelegateWithErrorCode;
import org.camunda.bpm.engine.test.errorcode.FailingJavaDelegateWithOleAndErrorCode;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

public class CustomErrorCodeProviderTest {

  protected static int PROVIDED_CUSTOM_CODE = 33_333;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(c -> {
    c.setCustomExceptionCodeProvider(new ExceptionCodeProvider() {

      @Override
      public Integer provideCode(SQLException sqlException) {
        return PROVIDED_CUSTOM_CODE;
      }

      @Override
      public Integer provideCode(ProcessEngineException processEngineException) {
        return PROVIDED_CUSTOM_CODE;
      }

    });
  });

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch("org.camunda.bpm.engine.cmd")
      .level(Level.WARN);

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected IdentityService identityService;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
  }

  @After
  public void clear() {
    engineRule.getIdentityService().deleteUser("kermit");
  }

  @Test
  public void shouldOverrideProvidedExceptionCode1() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    Throwable exception = catchThrowable(() -> runtimeService.startProcessInstanceByKey("foo",
        Variables.putValue("code", 22_222)));

    // then
    assertThat(((ProcessEngineException) exception).getCode()).isEqualTo(22_222);
  }

  @Test
  public void shouldOverrideProvidedExceptionCode2() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    Throwable exception = catchThrowable(() -> runtimeService.startProcessInstanceByKey("foo",
        Variables.putValue("code", 20_000)));

    // then
    assertThat(((ProcessEngineException) exception).getCode()).isEqualTo(20_000);
  }

  @Test
  public void shouldOverrideProvidedExceptionCode3() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    Throwable exception = catchThrowable(() -> runtimeService.startProcessInstanceByKey("foo",
        Variables.putValue("code", 39_999)));

    // then
    assertThat(((ProcessEngineException) exception).getCode()).isEqualTo(39_999);
  }

  /**
   * This situation cannot happen right now since OLE are not thrown within delegation code
   * but when the process transaction is flushed. However, with this test case we ensure
   * that the built-in code provider has precedence over a code that was assigned via delegation
   * code. This ensures consistent behavior when we add non-SQL exception related built-in codes in the future.
   */
  @Test
  public void shouldOverrideCodeFromDelegationCodeWithBuiltinCode() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithOleAndErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    ThrowingCallable callable =
        () -> runtimeService.startProcessInstanceByKey("foo",
            Variables.putValue("code", 40_000));

    // then
    assertThatThrownBy(callable)
        .extracting("code")
        .contains(BuiltinExceptionCode.OPTIMISTIC_LOCKING.getCode());
    assertThat(loggingRule.getLog().get(0).getMessage())
        .contains("Falling back to built-in code");
  }

  @Test
  public void shouldOverrideCodeZeroWithCustomCode() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    ThrowingCallable callable =
        () -> runtimeService.startProcessInstanceByKey("foo",
            Variables.putValue("code", 0));

    // then
    assertThatThrownBy(callable)
        .extracting("code")
        .contains(PROVIDED_CUSTOM_CODE);
  }

  @Test
  public void shouldResetReservedCodeFromDelegationCode1() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    ThrowingCallable callable =
        () -> runtimeService.startProcessInstanceByKey("foo",
            Variables.putValue("code", 1));

    // then
    assertThatThrownBy(callable)
        .extracting("code")
        .contains(BuiltinExceptionCode.FALLBACK.getCode());
    assertThat(loggingRule.getLog().get(0).getMessage())
        .contains("Falling back to default error code 0.");
  }

  @Test
  public void shouldResetReservedCodeFromDelegationCode2() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    ThrowingCallable callable =
        () -> runtimeService.startProcessInstanceByKey("foo",
            Variables.putValue("code", 19_999));

    // then
    assertThatThrownBy(callable)
        .extracting("code")
        .contains(BuiltinExceptionCode.FALLBACK.getCode());
    assertThat(loggingRule.getLog().get(0).getMessage())
        .contains("Falling back to default error code 0.");
  }

  @Test
  public void shouldResetReservedCodeFromDelegationCode3() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithErrorCode.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    ThrowingCallable callable =
        () -> runtimeService.startProcessInstanceByKey("foo",
            Variables.putValue("code", 40_000));

    // then
    assertThatThrownBy(callable)
        .extracting("code")
        .contains(BuiltinExceptionCode.FALLBACK.getCode());
    assertThat(loggingRule.getLog().get(0).getMessage())
        .contains("Falling back to default error code 0.");
  }

  @Test
  public void shouldProvideCustomCodeFromDelegationCodeWithCustomException() {
    // given
    BpmnModelInstance myProcess = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingJavaDelegateWithCustomException.class)
        .endEvent()
        .done();

    testRule.deploy(myProcess);

    // when
    ThrowingCallable callable =
        () -> runtimeService.startProcessInstanceByKey("foo",
            Variables.putValue("code", 22_222));

    // then
    assertThatThrownBy(callable)
        .extracting("code")
        .contains(22_222);
  }

  @Test
  public void shouldHaveSubordinationToBuiltinCode() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .endEvent()
        .done();

    testRule.deploy(modelInstance);

    String businessKey = generateString(1_000);

    // when/then
    assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("process", businessKey))
        .isInstanceOf(ProcessEngineException.class)
        .extracting("code")
        .contains(BuiltinExceptionCode.COLUMN_SIZE_TOO_SMALL.getCode());
  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////

  protected String generateString(int size) {
    return new String(new char[size]).replace('\0', 'a');
  }

}

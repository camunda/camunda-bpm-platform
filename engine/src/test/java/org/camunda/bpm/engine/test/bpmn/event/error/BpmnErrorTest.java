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
package org.camunda.bpm.engine.test.bpmn.event.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;

/**
 * This test asserts the constructors of the {@link BpmnError} class as this
 * is part of the public API
 */
public class BpmnErrorTest {

  /** Error code used when creating BpmnError instances */
  private static final String ERROR_CODE = "testErrorCode";

  /** Error message used when creating BpmnError instances */
  private static final String ERROR_MESSAGE = "testErrorMessage";

  /** Cause object used when creating BpmnError instances with a cause */
  private static final Throwable CAUSE = new IllegalArgumentException("causeMessage");

  @Test
  public void testCreation_ErrorCodeOnly() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE);
    
    // then
    assertThat(bpmnError).hasMessage(null);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }

  @Test
  public void testCreation_ErrorMessagePresent() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE, ERROR_MESSAGE);
    
    // then
    assertThat(bpmnError).hasMessage(ERROR_MESSAGE);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }

  @Test
  public void testCreation_ErrorCodeOnlyWithCause() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE, CAUSE);
    
    // then
    assertThat(bpmnError)
            .hasMessage(null)
            .hasCause(CAUSE);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }

  @Test
  public void testCreation_ErrorMessageAndCausePresent() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE, ERROR_MESSAGE, CAUSE);
    
    // then
    assertThat(bpmnError)
            .hasMessageContaining(ERROR_MESSAGE)
            .hasCause(CAUSE);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }
}

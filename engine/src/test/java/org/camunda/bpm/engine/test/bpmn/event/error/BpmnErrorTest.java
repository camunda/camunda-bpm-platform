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
  public void testCreation_ErrorCodeOnly_BpmnMessageContainsErrorCode() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE);
    
    // then
    assertThat(bpmnError).hasMessage(null);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }

  @Test
  public void testCreation_ErrorMessagePresent_BpmnMessageContainsErrorCodeAndMessage() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE, ERROR_MESSAGE);
    
    // then
    assertThat(bpmnError).hasMessage(ERROR_MESSAGE);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }

  @Test
  public void testCreation_ErrorCodeOnlyWithCause_BpmnMessageIsCopiedFromCause() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE, CAUSE);
    
    // then
    assertThat(bpmnError)
            .hasMessage(null)
            .hasCause(CAUSE);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }

  @Test
  public void testCreation_ErrorMessageAndCausePresent_BpmnMessageContainsCause() {
    // when
    BpmnError bpmnError = new BpmnError(ERROR_CODE, ERROR_MESSAGE, CAUSE);
    
    // then
    assertThat(bpmnError)
            .hasMessageContaining(ERROR_MESSAGE)
            .hasCause(CAUSE);
    assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
  }
}

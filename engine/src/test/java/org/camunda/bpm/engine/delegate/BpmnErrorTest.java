package org.camunda.bpm.engine.delegate;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

public class BpmnErrorTest extends TestCase {

    /** Error code used when creating BpmnError instances */
    private static final String ERROR_CODE = "testErrorCode";

    /** Error message used when creating BpmnError instances */
    private static final String ERROR_MESSAGE = "testErrorMessage";

    /** Cause object used when creating BpmnError instances with a cause */
    private static final Throwable CAUSE = new IllegalArgumentException("causeMessage");


    public void testCreation_ErrorCodeOnly_BpmnMessageContainsErrorCode() {
        BpmnError bpmnError = new BpmnError(ERROR_CODE);
        assertThat(bpmnError).hasMessageContaining(ERROR_CODE);
    }

    public void testCreation_ErrorMessagePresent_BpmnMessageContainsErrorCodeAndMessage() {
        BpmnError bpmnError = new BpmnError(ERROR_CODE, ERROR_MESSAGE);
        assertThat(bpmnError) //
                .hasMessageContaining(ERROR_CODE) //
                .hasMessageContaining(ERROR_MESSAGE);
    }

    public void testCreation_ErrorCodeOnlyWithCause_BpmnMessageIsCopiedFromCause() {
        BpmnError bpmnError = new BpmnError(ERROR_CODE, CAUSE);
        assertThat(bpmnError) //
                .hasMessage(CAUSE.getMessage()) //
                .hasCause(CAUSE);
        assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
    }

    public void testCreation_ErrorMessageAndCausePresent_BpmnMessageContainsCause() {
        BpmnError bpmnError = new BpmnError(ERROR_CODE, ERROR_MESSAGE, CAUSE);
        assertThat(bpmnError) //
                .hasMessageContaining(ERROR_CODE) //
                .hasMessageContaining(ERROR_MESSAGE) //
                .hasMessageContaining(CAUSE.getMessage()) //
                .hasCause(CAUSE);
        assertThat(bpmnError.getErrorCode()).isEqualTo(ERROR_CODE);
    }

}

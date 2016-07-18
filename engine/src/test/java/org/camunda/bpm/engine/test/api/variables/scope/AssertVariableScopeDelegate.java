package org.camunda.bpm.engine.test.api.variables.scope;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class AssertVariableScopeDelegate implements JavaDelegate {
  private static final String TEST_SCOPE = "SubProcess_1";

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Map<ScopeImpl, PvmExecutionImpl> mapping = ((ExecutionEntity) execution).createActivityExecutionMapping();
    for (ScopeImpl scope : mapping.keySet()) {
      if (scope.getId().equals(TEST_SCOPE)) {
        assertThat(mapping.get(scope).getVariableLocal("targetOrderId"),is(notNullValue()));
      }
    }

  }
}

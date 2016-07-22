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

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    assertThat(execution.getVariableLocal("targetOrderId"),is(notNullValue()));
  }
}

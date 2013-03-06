package org.camunda.bpm.engine.rest.dto.converter;

import org.camunda.bpm.engine.task.DelegationState;

public class DelegationStateConverter implements StringToTypeConverter<DelegationState> {

  @Override
  public DelegationState convertQueryParameterToType(String value) {
    return DelegationState.valueOf(value.toUpperCase());
  }
}

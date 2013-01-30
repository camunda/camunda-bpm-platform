package org.camunda.bpm.engine.rest.dto.converter;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.QueryOperator;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

public class VariableListConverter implements
    StringToTypeConverter<List<VariableQueryParameterDto>> {

  @Override
  public List<VariableQueryParameterDto> convertToType(String value) {
    VariableQueryParameterDto queryVariable = new VariableQueryParameterDto();
    
    String[] valueTriple = value.split("_");
    if (valueTriple.length != 3) {
      throw new InvalidRequestException("variable query parameter has to have format KEY_OPERATOR_VALUE.");
    }
    queryVariable.setVariableKey(valueTriple[0]);
    queryVariable.setOperator(QueryOperator.valueOf(valueTriple[1].toUpperCase()));
    queryVariable.setVariableValue(valueTriple[2]);
    
    List<VariableQueryParameterDto> queryVariables = new ArrayList<VariableQueryParameterDto>();
    queryVariables.add(queryVariable);
    return queryVariables;
  }

}

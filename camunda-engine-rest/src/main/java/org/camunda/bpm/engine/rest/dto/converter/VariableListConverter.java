package org.camunda.bpm.engine.rest.dto.converter;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.QueryOperator;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * Reads a list of {@link VariableQueryParameterDto}s from a single parameter in the format KEY_OPERATOR_VALUE,
 * so for example: aVariableName_equals_aValue.
 * @author Thorben Lindhauer
 *
 */
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
    QueryOperator op = convertToQueryOperator(valueTriple[1]);
    queryVariable.setOperator(op);
    queryVariable.setVariableValue(valueTriple[2]);
    
    List<VariableQueryParameterDto> queryVariables = new ArrayList<VariableQueryParameterDto>();
    queryVariables.add(queryVariable);
    return queryVariables;
  }
  
  private QueryOperator convertToQueryOperator(String opAsString) {
    if (opAsString.equals("eq")) {
      return QueryOperator.EQUALS;
    } else if (opAsString.equals("gt")) {
      return QueryOperator.GREATER_THAN;
    } else if (opAsString.equals("gteq")) {
      return QueryOperator.GREATER_THAN_OR_EQUAL;
    } else if (opAsString.equals("lt")) {
      return QueryOperator.LESS_THAN;
    } else if (opAsString.equals("lteq")) {
      return QueryOperator.LESS_THAN_OR_EQUAL;
    } else if (opAsString.equals("neq")) {
      return QueryOperator.NOT_EQUALS;
    } else if (opAsString.equals("like")) {
      return QueryOperator.LIKE;
    } else {
      throw new InvalidRequestException("Unsupported query variable operator parameter.");
    }
  }

}

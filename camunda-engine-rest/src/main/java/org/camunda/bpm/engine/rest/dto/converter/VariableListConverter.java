package org.camunda.bpm.engine.rest.dto.converter;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.QueryOperator;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * Reads a list of {@link VariableQueryParameterDto}s from a single parameter. Expects a given format (see method comments).
 * @author Thorben Lindhauer
 *
 */
public class VariableListConverter implements
    StringToTypeConverter<List<VariableQueryParameterDto>> {
  
  private static final String VARIABLE_NAME_JSON_PROPERTY = "name";
  private static final String OPERATOR_JSON_PROPERTY = "operator";
  private static final String VARIABLE_VALUE_JSON_PROPERTY = "value";

  /**
   * Expects a query parameter of format KEY_OPERATOR_VALUE, e.g. aVariable_eq_aValue
   */
  @Override
  public List<VariableQueryParameterDto> convertQueryParameterToType(String value) {
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

  /**
   * Expects a json array with one json object per variable to check. Each such object has three fields:
   * name, operator and value. E.g. [{ name: "aVariable", operator: "eq", value: "aValue" }]
   */
  @Override
  public List<VariableQueryParameterDto> convertFromJsonToType(String value) {
    JSONArray jsonArray = new JSONArray(value);
    
    List<VariableQueryParameterDto> list = new ArrayList<VariableQueryParameterDto>();
    for (int i = 0; i < jsonArray.length(); i++) {
      VariableQueryParameterDto queryVariable = new VariableQueryParameterDto();
      
      JSONObject parameter = jsonArray.getJSONObject(i);
      String variableName = parameter.getString(VARIABLE_NAME_JSON_PROPERTY);
      queryVariable.setVariableKey(variableName);
      
      QueryOperator op = convertToQueryOperator(parameter.getString(OPERATOR_JSON_PROPERTY));
      queryVariable.setOperator(op);
      
      Object variableValue = parameter.get(VARIABLE_VALUE_JSON_PROPERTY);
      queryVariable.setVariableValue(variableValue);
      list.add(queryVariable);
    }
    return list;
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

package org.camunda.bpm.engine.rest.util;

import static org.fest.assertions.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.junit.Test;

public class DtoUtilTest {
  
  @Test
  public void testDtoUtilToMap_String() throws Exception {
    // given
    String type = "String";
    
    String firstTestValue = "aTestValue";
    String firstTestVar = "firstTestVar";
    
    String secondTestValue = null;
    String secondTestVar = "secondTestVar";

    String thirdTestValue = "";
    String thirdTestVar = "thirdTestVar";

    Integer fourthTestValue = 5;
    String fourthTestVar = "fourthTestVar";
    
    Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
    
    variables.put(firstTestVar, new VariableValueDto(firstTestValue, type));
    variables.put(secondTestVar, new VariableValueDto(secondTestValue, type));
    variables.put(thirdTestVar, new VariableValueDto(thirdTestValue, type));
    variables.put(fourthTestVar, new VariableValueDto(fourthTestValue, type));
    
    // when
    Map<String, Object> result = DtoUtil.toMap(variables);
    
    // then
    assertThat(result.size()).isEqualTo(4);
    
    Object firstValue = result.get(firstTestVar);
    assertThat(firstValue).isInstanceOf(String.class);
    assertThat(firstValue).isEqualTo(firstTestValue);
    
    Object secondValue = result.get(secondTestVar);
    assertThat(secondValue).isNull();
    
    Object thirdValue = result.get(thirdTestVar);
    assertThat(thirdValue).isInstanceOf(String.class);
    assertThat(thirdValue).isEqualTo(thirdTestValue);
    
    Object fourthValue = result.get(fourthTestVar);
    assertThat(fourthValue).isInstanceOf(String.class);
    assertThat(fourthValue).isEqualTo("5");
  }
  
  @Test
  public void testDtoUtilToMap_Boolean() throws Exception {
    // given
    String type = "Boolean";
    
    Boolean firstTestValue = true;
    String firstTestVar = "firstTestVar";
    
    Boolean secondTestValue = false;
    String secondTestVar = "secondTestVar";

    String thirdTestValue = "true";
    String thirdTestVar = "thirdTestVar";

    String fourthTestValue = "false";
    String fourthTestVar = "fourthTestVar";
    
    String fifthTestValue = "abc";
    String fifthTestVar = "fifthTestVar";

    String sixthTestValue = null;
    String sixthTestVar = "sixthTestVar";
    
    Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
    
    variables.put(firstTestVar, new VariableValueDto(firstTestValue, type));
    variables.put(secondTestVar, new VariableValueDto(secondTestValue, type));
    variables.put(thirdTestVar, new VariableValueDto(thirdTestValue, type));
    variables.put(fourthTestVar, new VariableValueDto(fourthTestValue, type));
    variables.put(fifthTestVar, new VariableValueDto(fifthTestValue, type));
    variables.put(sixthTestVar, new VariableValueDto(sixthTestValue, type));
    
    // when
    Map<String, Object> result = DtoUtil.toMap(variables);
    
    // then
    assertThat(result.size()).isEqualTo(6);
    
    Object firstValue = result.get(firstTestVar);
    assertThat(firstValue).isInstanceOf(Boolean.class);
    assertThat(firstValue).isEqualTo(firstTestValue);
    
    Object secondValue = result.get(secondTestVar);
    assertThat(secondValue).isInstanceOf(Boolean.class);
    assertThat(secondValue).isEqualTo(secondTestValue);
    
    Object thirdValue = result.get(thirdTestVar);
    assertThat(thirdValue).isInstanceOf(Boolean.class);
    assertThat(thirdValue).isEqualTo(true);
    
    Object fourthValue = result.get(fourthTestVar);
    assertThat(fourthValue).isInstanceOf(Boolean.class);
    assertThat(fourthValue).isEqualTo(false);
    
    Object fifthValue = result.get(fifthTestVar);
    assertThat(fifthValue).isInstanceOf(Boolean.class);
    assertThat(fifthValue).isEqualTo(false);
    
    Object sixthValue = result.get(sixthTestVar);
    assertThat(sixthValue).isNull();
  }
  
  @Test
  public void testDtoUtilToMap_Integer() throws Exception {
    // given
    String type = "Integer";
    
    Integer firstTestValue = 123;
    String firstTestVar = "firstTestVar";
    
    String secondTestValue = "123";
    String secondTestVar = "secondTestVar";

    Integer thirdTestValue = null;
    String thirdTestVar = "thirdTestVar";
       
    Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
    
    variables.put(firstTestVar, new VariableValueDto(firstTestValue, type));
    variables.put(secondTestVar, new VariableValueDto(secondTestValue, type));
    variables.put(thirdTestVar, new VariableValueDto(thirdTestValue, type));
        
    // when
    Map<String, Object> result = DtoUtil.toMap(variables);
    
    // then
    assertThat(result.size()).isEqualTo(3);
    
    Object firstValue = result.get(firstTestVar);
    assertThat(firstValue).isInstanceOf(Integer.class);
    assertThat(firstValue).isEqualTo(firstTestValue);
    
    Object secondValue = result.get(secondTestVar);
    assertThat(secondValue).isInstanceOf(Integer.class);
    assertThat(secondValue).isEqualTo(123);
    
    Object thirdValue = result.get(thirdTestVar);
    assertThat(thirdValue).isNull();
  }
  
  @Test
  public void testDtoUtilToMap_Short() throws Exception {
    // given
    String type = "Short";
    
    Short firstTestValue = 123;
    String firstTestVar = "firstTestVar";
    
    String secondTestValue = "123";
    String secondTestVar = "secondTestVar";

    Short thirdTestValue = null;
    String thirdTestVar = "thirdTestVar";
       
    Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
    
    variables.put(firstTestVar, new VariableValueDto(firstTestValue, type));
    variables.put(secondTestVar, new VariableValueDto(secondTestValue, type));
    variables.put(thirdTestVar, new VariableValueDto(thirdTestValue, type));
        
    // when
    Map<String, Object> result = DtoUtil.toMap(variables);
    
    // then
    assertThat(result.size()).isEqualTo(3);
    
    Object firstValue = result.get(firstTestVar);
    assertThat(firstValue).isInstanceOf(Short.class);
    assertThat(firstValue).isEqualTo(firstTestValue);
    
    Object secondValue = result.get(secondTestVar);
    assertThat(secondValue).isInstanceOf(Short.class);
    assertThat(secondValue).isEqualTo((short) 123);
    
    Object thirdValue = result.get(thirdTestVar);
    assertThat(thirdValue).isNull();
  }
  
  @Test
  public void testDtoUtilToMap_Long() throws Exception {
    // given
    String type = "Long";
    
    Long firstTestValue = Long.valueOf(123);
    String firstTestVar = "firstTestVar";
    
    String secondTestValue = "123";
    String secondTestVar = "secondTestVar";

    Long thirdTestValue = null;
    String thirdTestVar = "thirdTestVar";
       
    Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
    
    variables.put(firstTestVar, new VariableValueDto(firstTestValue, type));
    variables.put(secondTestVar, new VariableValueDto(secondTestValue, type));
    variables.put(thirdTestVar, new VariableValueDto(thirdTestValue, type));
        
    // when
    Map<String, Object> result = DtoUtil.toMap(variables);
    
    // then
    assertThat(result.size()).isEqualTo(3);
    
    Object firstValue = result.get(firstTestVar);
    assertThat(firstValue).isInstanceOf(Long.class);
    assertThat(firstValue).isEqualTo(firstTestValue);
    
    Object secondValue = result.get(secondTestVar);
    assertThat(secondValue).isInstanceOf(Long.class);
    assertThat(secondValue).isEqualTo((long) 123);
    
    Object thirdValue = result.get(thirdTestVar);
    assertThat(thirdValue).isNull();
  }
  
  @Test
  public void testDtoUtilToMap_Double() throws Exception {
    // given
    String type = "Double";
    
    Double firstTestValue = Double.valueOf(123.456);
    String firstTestVar = "firstTestVar";
    
    String secondTestValue = "123.456";
    String secondTestVar = "secondTestVar";

    Double thirdTestValue = null;
    String thirdTestVar = "thirdTestVar";
       
    String fourthTestValue = "123";
    String fourthTestVar = "fourthTestVar";
    
    Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
    
    variables.put(firstTestVar, new VariableValueDto(firstTestValue, type));
    variables.put(secondTestVar, new VariableValueDto(secondTestValue, type));
    variables.put(thirdTestVar, new VariableValueDto(thirdTestValue, type));
    variables.put(fourthTestVar, new VariableValueDto(fourthTestValue, type));
        
    // when
    Map<String, Object> result = DtoUtil.toMap(variables);
    
    // then
    assertThat(result.size()).isEqualTo(4);
    
    Object firstValue = result.get(firstTestVar);
    assertThat(firstValue).isInstanceOf(Double.class);
    assertThat(firstValue).isEqualTo(firstTestValue);
    
    Object secondValue = result.get(secondTestVar);
    assertThat(secondValue).isInstanceOf(Double.class);
    assertThat(secondValue).isEqualTo((double) 123.456);
    
    Object thirdValue = result.get(thirdTestVar);
    assertThat(thirdValue).isNull();
    
    Object fourthValue = result.get(fourthTestVar);
    assertThat(fourthValue).isInstanceOf(Double.class);
    assertThat(fourthValue).isEqualTo((double) 123.0);
  }
  
  @Test
  public void testDtoUtilToMap_Date() throws Exception {
    // given
    String type = "Date";
    
    Date now = new Date();
    
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String firstTestValue = formatter.format(now);
    String firstTestVar = "firstTestVar";

    String secondTestValue = null;
    String secondTestVar = "secondTestVar";
   
    Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
    
    variables.put(firstTestVar, new VariableValueDto(firstTestValue, type));
    variables.put(secondTestVar, new VariableValueDto(secondTestValue, type));
        
    // when
    Map<String, Object> result = DtoUtil.toMap(variables);
    
    // then
    assertThat(result.size()).isEqualTo(2);
    
    Object firstValue = result.get(firstTestVar);
    assertThat(firstValue).isInstanceOf(Date.class);
    assertThat(formatter.format(firstValue)).isEqualTo(formatter.format(now));   
    
    Object secondValue = result.get(secondTestVar);
    assertThat(secondValue).isNull();

  }
}

package org.camunda.spin.impl.json.tree;

import java.io.IOException;

import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.DataFormatMapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonJacksonTreeDataFormatMapper implements DataFormatMapper {

  private final JsonJacksonTreeLogger LOG = SpinLogger.JSON_TREE_LOGGER;
  
  protected JsonJacksonTreeDataFormat format;
  
  public JsonJacksonTreeDataFormatMapper(JsonJacksonTreeDataFormat format) {
    this.format = format;
  }
  
  public Object mapJavaToInternal(Object parameter) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public <T> T mapInternalToJava(Object parameter, Class<T> type) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    T result = mapInternalToJava(parameter, javaType);
    return result;
  }

  public <T> T mapInternalToJava(Object parameter, String typeIdentifier) {
    JavaType javaType = format.constructJavaTypeFromCanonicalString(typeIdentifier);
    T result = mapInternalToJava(parameter, javaType);
    return result;
  }
  
  public <C> C mapInternalToJava(Object parameter, JavaType type) {
    JsonNode jsonNode = (JsonNode) parameter;
    ObjectMapper mapper = format.getConfiguredObjectMapper();
    try {
      return mapper.readValue(mapper.treeAsTokens(jsonNode), type);
    } catch (IOException e) {
      throw LOG.unableToDeserialize(jsonNode, type, e);
    }
  }

}

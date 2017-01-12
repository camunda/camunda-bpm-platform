package org.camunda.bpm.engine.rest.sub.impl;

import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class VariableResponseProvider {

  public Response getResponseForTypedVariable(TypedValue typedVariableValue, String id) {
    if (typedVariableValue instanceof BytesValue || ValueType.BYTES.equals(typedVariableValue.getType())) {
      return responseForByteVariable(typedVariableValue);
    } else if (ValueType.FILE.equals(typedVariableValue.getType())) {
      return responseForFileVariable((FileValue) typedVariableValue);
    } else {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, String.format("Value of variable with id %s is not a binary value.", id));
    }
  }


  /**
   * Creates a response for a variable of type {@link ValueType#FILE}.
   */
  protected Response responseForFileVariable(FileValue fileValue) {
    String type = fileValue.getMimeType() != null ? fileValue.getMimeType() : MediaType.APPLICATION_OCTET_STREAM;
    if (fileValue.getEncoding() != null) {
      type += "; charset=" + fileValue.getEncoding();
    }
    Object value = fileValue.getValue() == null ? "" : fileValue.getValue();
    return Response.ok(value, type).header("Content-Disposition", "attachment; filename=" + fileValue.getFilename()).build();
  }

  /**
   * Creates a response for a variable of type {@link ValueType#BYTES}.
   */
  protected Response responseForByteVariable(TypedValue variableInstance) {
    byte[] valueBytes = (byte[]) variableInstance.getValue();
    if (valueBytes == null) {
      valueBytes = new byte[0];
    }
    return Response.ok(new ByteArrayInputStream(valueBytes), MediaType.APPLICATION_OCTET_STREAM).build();
  }
}

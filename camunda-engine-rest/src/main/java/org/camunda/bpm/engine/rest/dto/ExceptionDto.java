package org.camunda.bpm.engine.rest.dto;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author nico.rehwaldt
 */
public class ExceptionDto {

  private String type;
  private String message;

  private String stacktrace;

  public ExceptionDto() {

  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public static ExceptionDto fromException(Exception e, boolean includeStacktrace) {

    ExceptionDto dto = new ExceptionDto();

    dto.type = e.getClass().getSimpleName();
    dto.message = e.getMessage();

    if (includeStacktrace) {
      StringWriter writer = new StringWriter();

      e.printStackTrace(new PrintWriter(writer));

      dto.stacktrace = writer.toString();
    }

    return dto;
  }
}

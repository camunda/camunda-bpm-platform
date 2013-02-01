package org.camunda.bpm.engine.rest.dto.converter;

import java.util.Date;

import org.joda.time.DateTime;

public class DateConverter implements StringToTypeConverter<Date> {

  @Override
  public Date convertQueryParameterToType(String value) {
    return DateTime.parse(value).toDate();
  }
}

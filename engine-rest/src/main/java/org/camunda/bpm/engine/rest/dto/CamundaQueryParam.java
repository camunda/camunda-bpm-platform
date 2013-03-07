package org.camunda.bpm.engine.rest.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.camunda.bpm.engine.rest.dto.converter.StringConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringToTypeConverter;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface CamundaQueryParam {
  public String value();
  public Class<? extends StringToTypeConverter<?>> converter() default StringConverter.class;
}

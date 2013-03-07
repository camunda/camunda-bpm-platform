package org.camunda.bpm.engine.rest.dto.converter;

/**
 * Implementations are used to convert http parameters from string to java types.
 * @author Thorben Lindhauer
 *
 * @param <T>
 */
public interface StringToTypeConverter<T> {

  T convertQueryParameterToType(String value);
}

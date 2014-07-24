package org.camunda.spin.spi;

/**
 * Maps a java object to the data format's internal representation and vice versa.
 * 
 * @author Thorben Lindhauer
 */
public interface DataFormatMapper {

  public Object mapJavaToInternal(Object parameter);
  
  public <T> T mapInternalToJava(Object parameter, Class<T> type);
  
  public <T> T mapInternalToJava(Object parameter, String typeIdentifier);
}

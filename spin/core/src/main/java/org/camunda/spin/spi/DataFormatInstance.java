package org.camunda.spin.spi;

import org.camunda.spin.Spin;

public interface DataFormatInstance<T extends Spin<?>> {

  DataFormat<T> getDataFormat();
  
  DataFormatReader getReader();
}

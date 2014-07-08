package org.camunda.spin.impl.json.tree;

import org.camunda.spin.spi.Configurable;

public interface JsonJacksonTreeConfigurable<R extends Configurable<R>> extends Configurable<R> {

  Boolean allowsNumericLeadingZeros();
  
  R allowNumericLeadingZeros(Boolean value);
}

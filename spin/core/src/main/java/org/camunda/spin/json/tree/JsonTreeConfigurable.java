package org.camunda.spin.json.tree;

import org.camunda.spin.spi.Configurable;

public interface JsonTreeConfigurable<R extends Configurable<R>> extends Configurable<R> {

  Boolean allowsNumericLeadingZeros();
  
  R allowNumericLeadingZeros(Boolean value);
}

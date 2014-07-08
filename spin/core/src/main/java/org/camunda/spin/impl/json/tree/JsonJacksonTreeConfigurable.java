package org.camunda.spin.impl.json.tree;

import org.camunda.spin.spi.Configurable;

public interface JsonJacksonTreeConfigurable<R extends Configurable<R>> extends Configurable<R> {

  Boolean allowsNumericLeadingZeros();
  
  R allowNumericLeadingZeros(Boolean value);
  
  Boolean allowsComments();
  
  R allowComments(Boolean value);
  
  Boolean allowsUnquotedFieldNames();
  
  R allowQuotedFieldNames(Boolean value);
  
  Boolean allowsSingleQuotes();
  
  R allowSingleQuotes(Boolean value);
  
  Boolean allowsBackslashEscapingAnyCharacter();
  
  R allowBackslashEscapingAnyCharacter(Boolean value);
  
  Boolean allowsNonNumericNumbers();
  
  R allowNonNumericNumbers(Boolean value);
}

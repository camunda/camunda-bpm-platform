/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.impl.json.tree;

import org.camunda.spin.spi.Configurable;

/**
 * Provides Jackson's parser configuration options.
 * 
 * @author Thorben Lindhauer
 */
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

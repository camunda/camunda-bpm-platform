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
package org.camunda.bpm.engine.impl.util.xml;

import org.camunda.bpm.engine.BpmnParseException;
import org.xml.sax.SAXParseException;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class Problem {

  protected String errorMessage;
  protected String resource;
  protected int line;
  protected int column;

  public Problem(SAXParseException e, String resource) {
    concatenateErrorMessages(e);
    this.resource = resource;
    this.line = e.getLineNumber();
    this.column = e.getColumnNumber();
  }
  
  public Problem(String errorMessage, String resourceName, Element element) {
    this.errorMessage = errorMessage;
    this.resource = resourceName;
    if (element!=null) {
      this.line = element.getLine();
      this.column = element.getColumn();
    }
  }

  public Problem(BpmnParseException exception, String resourceName) {
    concatenateErrorMessages(exception);
    this.resource = resourceName;
    Element element = exception.getElement();
    if (element != null) {
      this.line = element.getLine();
      this.column = element.getColumn();
    }
  }

  protected void concatenateErrorMessages(Throwable throwable) {
    while (throwable != null) {
      if (errorMessage == null) {
        errorMessage = throwable.getMessage();
      }
      else {
        errorMessage += ": " + throwable.getMessage();
      }
      throwable = throwable.getCause();
    }
  }

  public String toString() {
    return errorMessage+(resource!=null ? " | "+resource : "")+" | line "+line+" | column "+column;
  }
}

/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.util.xml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.camunda.bpm.engine.BpmnParseException;
import org.xml.sax.SAXParseException;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 */
public class Problem {

  protected String errorMessage;
  protected String resource;
  protected int line;
  protected int column;
  private Set<String> elementIds = new HashSet<String>();

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
      String id = element.attribute("id");
      if (id != null && id.length() > 0) {
        this.elementIds.add(id);
      }
    }
  }

  public Problem(String errorMessage, String resourceName, String[] elementIds) {
    this.errorMessage = errorMessage;
    this.resource = resourceName;
    if (elementIds != null) {
      this.elementIds.addAll(new HashSet<String>(Arrays.asList(elementIds)));
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
    StringBuilder string = new StringBuilder(errorMessage); 
    if (resource != null) {
      string.append(" | " + resource);
    }
    if (line > 0) {
      string.append(" | line " + line);
    }
    if (column > 0) {
      string.append(" | column " + column);
    }
    if (elementIds.size() > 0) {
      string.append(" | element");
      if (elementIds.size() > 1) {
        string.append('s');
      }
      string.append(' ');
      for (Iterator iterator = elementIds.iterator(); iterator.hasNext();) {
        string.append(iterator.next());
        if (iterator.hasNext()) {
          string.append(',');
        }
      }
    }
    return string.toString();
  }
}

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
package org.camunda.bpm.engine.impl.form.engine;

import java.io.StringWriter;
import java.util.Stack;

/**
 * <p>The {@link HtmlDocumentBuilder} is part of the {@link HtmlFormEngine}
 * and maintains a stack of element which are written out to a {@link StringWriter}.</p>
 *
 * <p>Actual writing of the html elements is delegated to the {@link HtmlElementWriter}.</p>
 *
 * @author Daniel Meyer
 *
 */
public class HtmlDocumentBuilder {

  protected HtmlWriteContext context = new HtmlWriteContext();

  protected Stack<HtmlElementWriter> elements = new Stack<HtmlElementWriter>();
  protected StringWriter writer = new StringWriter();

  public HtmlDocumentBuilder(HtmlElementWriter documentElement) {
    startElement(documentElement);
  }

  public HtmlDocumentBuilder startElement(HtmlElementWriter renderer) {
    renderer.writeStartTag(context);
    elements.push(renderer);
    return this;
  }

  public HtmlDocumentBuilder endElement() {
    HtmlElementWriter renderer = elements.pop();
    renderer.writeContent(context);
    renderer.writeEndTag(context);
    return this;
  }

  public String getHtmlString() {
    return writer.toString();
  }

  public class HtmlWriteContext {

    public StringWriter getWriter() {
      return writer;
    }

    public int getElementStackSize() {
      return elements.size();
    }
  }
}
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.impl.form.engine.HtmlDocumentBuilder.HtmlWriteContext;

/**
 * <p>Simple writer for html elements. Used by the {@link HtmlDocumentBuilder}.</p>
 *
 * @author Daniel Meyer
 *
 */
public class HtmlElementWriter {

  protected String tagName;

  /** selfClosing means that the element should not be rendered as a
   * start + end tag pair but as a single tag using "/" to close the tag
   * inline */
  protected boolean isSelfClosing;
  protected String textContent;
  protected Map<String, String> attributes = new LinkedHashMap<String, String>();

  public HtmlElementWriter(String tagName) {
    this.tagName = tagName;
    this.isSelfClosing = false;
  }

  public HtmlElementWriter(String tagName, boolean isSelfClosing) {
    this.tagName = tagName;
    this.isSelfClosing = isSelfClosing;
  }

  public void writeStartTag(HtmlWriteContext context) {
    writeLeadingWhitespace(context);
    writeStartTagOpen(context);
    writeAttributes(context);
    writeStartTagClose(context);
    writeEndLine(context);
  }

  public void writeContent(HtmlWriteContext context) {
    if(textContent != null) {
      writeLeadingWhitespace(context);
      writeTextContent(context);
      writeEndLine(context);
    }
  }

  public void writeEndTag(HtmlWriteContext context) {
    if(!isSelfClosing) {
      writeLeadingWhitespace(context);
      writeEndTagElement(context);
      writeEndLine(context);
    }
  }

  protected void writeEndTagElement(HtmlWriteContext context) {
    StringWriter writer = context.getWriter();
    writer.write("</");
    writer.write(tagName);
    writer.write(">");
  }

  protected void writeTextContent(HtmlWriteContext context) {
    StringWriter writer = context.getWriter();
    writer.write("  "); // add additional whitespace
    writer.write(textContent);
  }

  protected void writeStartTagOpen(HtmlWriteContext context) {
    StringWriter writer = context.getWriter();
    writer.write("<");
    writer.write(tagName);
  }

  protected void writeAttributes(HtmlWriteContext context) {
    StringWriter writer = context.getWriter();
    for (Entry<String, String> attribute : attributes.entrySet()) {
      writer.write(" ");
      writer.write(attribute.getKey());
      if(attribute.getValue() != null) {
        writer.write("=\"");
        String attributeValue = escapeQuotes(attribute.getValue());
        writer.write(attributeValue);
        writer.write("\"");
      }
    }
  }

  protected String escapeQuotes(String attributeValue){
    String escapedHtmlQuote = "&quot;";
    String escapedJavaQuote = "\"";
    return attributeValue.replaceAll(escapedJavaQuote, escapedHtmlQuote);
  }

  protected void writeEndLine(HtmlWriteContext context) {
    StringWriter writer = context.getWriter();
    writer.write("\n");
  }

  protected void writeStartTagClose(HtmlWriteContext context) {
    StringWriter writer = context.getWriter();
    if(isSelfClosing) {
      writer.write(" /");
    }
    writer.write(">");
  }

  protected void writeLeadingWhitespace(HtmlWriteContext context) {
    int stackSize = context.getElementStackSize();
    StringWriter writer = context.getWriter();
    for (int i = 0; i < stackSize; i++) {
      writer.write("  ");
    }
  }

  // builder /////////////////////////////////////

  public HtmlElementWriter attribute(String name, String value) {
    attributes.put(name, value);
    return this;
  }

  public HtmlElementWriter textContent(String text) {
    if(isSelfClosing) {
      throw new IllegalStateException("Self-closing element cannot have text content.");
    }
    this.textContent = text;
    return this;
  }

}

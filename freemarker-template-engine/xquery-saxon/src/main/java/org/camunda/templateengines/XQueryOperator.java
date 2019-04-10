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
package org.camunda.templateengines;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;

import org.w3c.dom.Document;

/**
 * 
 * XQuery transformation wrapper. Inherently thread safe.
 * 
 */

public class XQueryOperator {

  public static class XQueryOperatorBuilder {

    private Configuration configuration;
    private Properties outputProperties;
    private Boolean indent;
    private Object stylesheet;
    private String encoding = "UTF-8";

    public XQueryOperatorBuilder() {
    }

    public XQueryOperator build() throws Exception {
      if (configuration == null) {
        configuration = new Configuration();
      }

      if (outputProperties == null) {
        outputProperties = getDefaultOutputProperties();
      }

      if (indent != null) {
        outputProperties.setProperty(OutputKeys.INDENT, indent ? "yes" : "no");
      }

      StaticQueryContext staticQueryContext = configuration.newStaticQueryContext();

      XQueryExpression xQueryExpression;
      if (stylesheet instanceof byte[]) {
        xQueryExpression = staticQueryContext.compileQuery(new ByteArrayInputStream((byte[]) stylesheet), encoding);
      } else if (stylesheet instanceof Reader) {
        xQueryExpression = staticQueryContext.compileQuery((Reader) stylesheet);
      } else {
        throw new IllegalArgumentException();
      }

      return new XQueryOperator(configuration, xQueryExpression, outputProperties);
    }

    public XQueryOperatorBuilder withIndent(boolean indent) {
      this.indent = indent;

      return this;
    }

    private Properties getDefaultOutputProperties() {
      Properties outputProperties = new Properties();
      outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      outputProperties.setProperty(OutputKeys.INDENT, "no");

      return outputProperties;
    }

    public XQueryOperatorBuilder withConfiguration(Configuration configuration) {
      this.configuration = configuration;

      return this;
    }

    public XQueryOperatorBuilder withStylesheet(byte[] stylesheet) throws IOException {
      this.stylesheet = stylesheet;

      return this;
    }

    public XQueryOperatorBuilder withStylesheet(Reader stylesheet) throws IOException {
      this.stylesheet = stylesheet;

      return this;
    }

    public XQueryOperatorBuilder withStylesheetResource(String stylesheet) throws IOException {

      InputStream in = XQueryOperatorBuilder.class.getResourceAsStream(stylesheet);
      if (in == null) {
        throw new FileNotFoundException("Unknown resource " + stylesheet);
      }
      ByteArrayOutputStream bout = new ByteArrayOutputStream();

      byte[] buffer = new byte[4 * 1024];

      try {
        int read;
        do {
          read = in.read(buffer);
          if (read == -1) {
            break;
          }

          bout.write(buffer, 0, read);
        } while (true);
      } finally {
        in.close();
      }

      this.stylesheet = bout.toByteArray();

      return this;
    }

    public XQueryOperatorBuilder withOutputProperties(Properties properties) {
      this.outputProperties = properties;

      return this;
    }

    public XQueryOperatorBuilder withCharacterEncoding(String encoding) {
      this.encoding = encoding;

      return this;
    }

    public XQueryOperatorBuilder withExtensionFunctionDefinition(ExtensionFunctionDefinition function) {
      if (configuration == null) {
        configuration = new Configuration();
      }
      configuration.registerExtensionFunction(function);

      return this;
    }
  }

  public static XQueryOperatorBuilder builder() {
    return new XQueryOperatorBuilder();
  }

  // thread safe
  private Configuration configuration;
  private XQueryExpression xQueryExpression;
  private Properties outputProperties;

  public XQueryOperator(Configuration configuration, XQueryExpression xQueryExpression, Properties outputProperties) {
    this.configuration = configuration;
    this.xQueryExpression = xQueryExpression;
    this.outputProperties = outputProperties;
  }

  public void evaluate(Result result, Object... parameters) throws TransformerException {
    DynamicQueryContext context = new DynamicQueryContext(configuration);

    if (parameters.length % 2 != 0) {
      throw new IllegalArgumentException();
    }
    for (int i = 0; i < parameters.length; i += 2) {
      Object value = parameters[i + 1];

      Sequence sequence = mapValue(value);
      context.setParameter(new StructuredQName("", "", (String) parameters[i]), sequence);
    }

    evaluate(context, result, outputProperties);
  }

  /**
   * 
   * @see <a href="http://docs.camunda.org/latest/guides/user-guide/#process-engine-process-variables-supported-variable-values">http://docs.camunda.org/latest/guides/user-guide/#process-engine-process-variables-supported-variable-values</a>
   * 
   */

  private Sequence mapValue(Object value) throws XPathException {
    Sequence sequence;
    if (value instanceof Document) {
      sequence = new DocumentWrapper((Document) value, "", configuration);
    } else if (value instanceof CharSequence) {
      sequence = StringValue.makeStringValue((CharSequence) value);
    } else if (value instanceof Boolean) {
      sequence = (Boolean) value ? BooleanValue.TRUE : BooleanValue.FALSE;
    } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
      Number n = (Number) value;
      sequence = Int64Value.makeIntegerValue(n.longValue());
    } else if (value instanceof Double) {
      Double b = (Double) value;
      sequence = DoubleValue.makeDoubleValue(b);
    } else if (value instanceof Float) {
      Float b = (Float) value;
      sequence = FloatValue.makeFloatValue(b);
    } else if (value instanceof Date) {
      sequence = DateTimeValue.fromJavaDate((Date) value);
    } else {
      sequence = new ObjectValue(value);
    }
    return sequence;
  }

  public void evaluate(Result result, Map<String, Object> parameters) throws TransformerException {
    DynamicQueryContext context = new DynamicQueryContext(configuration);

    for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
      Object value = parameter.getValue();

      Sequence sequence = mapValue(value);
      context.setParameter(new StructuredQName("", "", parameter.getKey()), sequence);
    }

    evaluate(context, result, outputProperties);
  }

  public void evaluate(DynamicQueryContext env, Result result, Properties outputProperties) throws TransformerException {
    xQueryExpression.run(env, result, outputProperties);
  }

  public String evaluateToString(Object... parameters) throws TransformerException {
    StringWriter outWriter = new StringWriter();
    StreamResult result = new StreamResult(outWriter);

    evaluate(result, parameters);

    return outWriter.toString();
  }

  public String evaluateToString(Map<String, Object> parameters) throws TransformerException {
    StringWriter outWriter = new StringWriter();
    StreamResult result = new StreamResult(outWriter);

    evaluate(result, parameters);

    return outWriter.toString();
  }

}
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
package org.camunda.bpm.engine.impl.bpmn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.camunda.bpm.engine.BpmnParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.mapping.InputParameter;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.core.variable.mapping.OutputParameter;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ListValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.MapValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.NullValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptValueProvider;
import org.camunda.bpm.engine.impl.util.ScriptUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 * Helper methods to reused for common parsing tasks.
 */
public final class BpmnParseUtil {

  /**
   * Returns the camunda extension element in the camunda namespace
   * and the given name.
   *
    * @param element the parent element of the extension element
   * @param extensionElementName the name of the extension element to find
   * @return the extension element or null if not found
   */
  public static Element findCamundaExtensionElement(Element element, String extensionElementName) {
    Element extensionElements = element.element("extensionElements");
    if(extensionElements != null) {
      return extensionElements.elementNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, extensionElementName);
    } else {
      return null;
    }
  }

  /**
   * Returns the {@link IoMapping} of an element.
   *
   * @param element the element to parse
   * @return the input output mapping or null if non defined
   * @throws BpmnParseException if a input/output parameter element is malformed
   */
  public static IoMapping parseInputOutput(Element element) {
    Element inputOutputElement = element.elementNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "inputOutput");
    if(inputOutputElement != null) {
      IoMapping ioMapping = new IoMapping();
      parseCamundaInputParameters(inputOutputElement, ioMapping);
      parseCamundaOutputParameters(inputOutputElement, ioMapping);
      return ioMapping;
    }
    return null;
  }

  /**
   * Parses all input parameters of an input output element and adds them to
   * the {@link IoMapping}.
   *
   * @param inputOutputElement the input output element to process
   * @param ioMapping the input output mapping to add input parameters to
   * @throws BpmnParseException if a input parameter element is malformed
   */
  public static void parseCamundaInputParameters(Element inputOutputElement, IoMapping ioMapping) {
    List<Element> inputParameters = inputOutputElement.elementsNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "inputParameter");
    for (Element inputParameterElement : inputParameters) {
      parseInputParameterElement(inputParameterElement, ioMapping);
    }
  }

  /**
   * Parses all output parameters of an input output element and adds them to
   * the {@link IoMapping}.
   *
   * @param inputOutputElement the input output element to process
   * @param ioMapping the input output mapping to add input parameters to
   * @throws BpmnParseException if a output parameter element is malformed
   */
  public static void parseCamundaOutputParameters(Element inputOutputElement, IoMapping ioMapping) {
    List<Element> outputParameters = inputOutputElement.elementsNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "outputParameter");
    for (Element outputParameterElement : outputParameters) {
      parseOutputParameterElement(outputParameterElement, ioMapping);
    }
  }

  /**
   * Parses a input parameter and adds it to the {@link IoMapping}.
   *
   * @param inputParameterElement the input parameter element
   * @param ioMapping the mapping to add the element
   * @throws BpmnParseException if the input parameter element is malformed
   */
  public static void parseInputParameterElement(Element inputParameterElement, IoMapping ioMapping) {
    String nameAttribute = inputParameterElement.attribute("name");
    if(nameAttribute == null || nameAttribute.isEmpty()) {
      throw new BpmnParseException("Missing attribute 'name' for inputParameter", inputParameterElement);
    }

    ParameterValueProvider valueProvider = parseNestedParamValueProvider(inputParameterElement);

    // add parameter
    ioMapping.addInputParameter(new InputParameter(nameAttribute, valueProvider));
  }

  /**
   * Parses a output parameter and adds it to the {@link IoMapping}.
   *
   * @param outputParameterElement the output parameter element
   * @param ioMapping the mapping to add the element
   * @throws BpmnParseException if the output parameter element is malformed
   */
  public static void parseOutputParameterElement(Element outputParameterElement, IoMapping ioMapping) {
    String nameAttribute = outputParameterElement.attribute("name");
    if(nameAttribute == null || nameAttribute.isEmpty()) {
      throw new BpmnParseException("Missing attribute 'name' for outputParameter", outputParameterElement);
    }

    ParameterValueProvider valueProvider = parseNestedParamValueProvider(outputParameterElement);

    // add parameter
    ioMapping.addOutputParameter(new OutputParameter(nameAttribute, valueProvider));
  }

  /**
   * @throws BpmnParseException if the parameter is invalid
   */
  protected static ParameterValueProvider parseNestedParamValueProvider(Element element) {
    // parse value provider
    if(element.elements().size() == 0) {
      return parseParamValueProvider(element);

    } else if(element.elements().size() == 1) {
      return parseParamValueProvider(element.elements().get(0));

    } else {
      throw new BpmnParseException("Nested parameter can at most have one child element", element);
    }
  }

  /**
   * @throws BpmnParseException if the parameter is invalid
   */
  protected static ParameterValueProvider parseParamValueProvider(Element parameterElement) {

    // LIST
    if("list".equals(parameterElement.getTagName())) {
      List<ParameterValueProvider> providerList = new ArrayList<>();
      for (Element element : parameterElement.elements()) {
        // parse nested provider
        providerList.add(parseParamValueProvider(element));
      }
      return new ListValueProvider(providerList);
    }

    // MAP
    if("map".equals(parameterElement.getTagName())) {
      TreeMap<ParameterValueProvider, ParameterValueProvider> providerMap = new TreeMap<>();
      for (Element entryElement : parameterElement.elements("entry")) {
        // entry must provide key
        String keyAttribute = entryElement.attribute("key");
        if(keyAttribute == null || keyAttribute.isEmpty()) {
          throw new BpmnParseException("Missing attribute 'key' for 'entry' element", entryElement);
        }
        // parse nested provider
        providerMap.put(new ElValueProvider(getExpressionManager().createExpression(keyAttribute)), parseNestedParamValueProvider(entryElement));
      }
      return new MapValueProvider(providerMap);
    }

    // SCRIPT
    if("script".equals(parameterElement.getTagName())) {
      ExecutableScript executableScript = parseCamundaScript(parameterElement);
      if (executableScript != null) {
        return new ScriptValueProvider(executableScript);
      }
      else {
        return new NullValueProvider();
      }
    }

    String textContent = parameterElement.getText().trim();
    if(!textContent.isEmpty()) {
        // EL
        return new ElValueProvider(getExpressionManager().createExpression(textContent));
    } else {
      // NULL value
      return new NullValueProvider();
    }

  }

  /**
   * Parses a camunda script element.
   *
   * @param scriptElement the script element ot parse
   * @return the generated executable script
   * @throws BpmnParseException if the a attribute is missing or the script cannot be processed
   */
  public static ExecutableScript parseCamundaScript(Element scriptElement) {
    String scriptLanguage = scriptElement.attribute("scriptFormat");
    if (scriptLanguage == null || scriptLanguage.isEmpty()) {
      throw new BpmnParseException("Missing attribute 'scriptFormat' for 'script' element", scriptElement);
    }
    else {
      String scriptResource = scriptElement.attribute("resource");
      String scriptSource = scriptElement.getText();
      try {
        return ScriptUtil.getScript(scriptLanguage, scriptSource, scriptResource, getExpressionManager());
      }
      catch (ProcessEngineException e) {
        throw new BpmnParseException("Unable to process script", scriptElement, e);
      }
    }
  }


  public static Map<String, String> parseCamundaExtensionProperties(Element element){
    Element propertiesElement = findCamundaExtensionElement(element, "properties");
    if(propertiesElement != null) {
      List<Element> properties = propertiesElement.elementsNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "property");
      Map<String, String> propertiesMap = new HashMap<>();
      for (Element property : properties) {
        propertiesMap.put(property.attribute("name"), property.attribute("value"));
      }
      return propertiesMap;
    }
    return null;
  }

  protected static ExpressionManager getExpressionManager() {
    return Context.getProcessEngineConfiguration().getExpressionManager();
  }

}

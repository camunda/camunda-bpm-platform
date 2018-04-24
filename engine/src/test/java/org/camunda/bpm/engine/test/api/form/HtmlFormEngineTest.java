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
package org.camunda.bpm.engine.test.api.form;

import java.util.Map;

import org.camunda.bpm.engine.impl.form.engine.FormEngine;
import org.camunda.bpm.engine.impl.form.engine.HtmlDocumentBuilder;
import org.camunda.bpm.engine.impl.form.engine.HtmlElementWriter;
import org.camunda.bpm.engine.impl.form.engine.HtmlFormEngine;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
 *
 */
public class HtmlFormEngineTest extends PluggableProcessEngineTestCase {

  public void testIsDefaultFormEngine() {

    // make sure the html form engine is the default form engine:
    Map<String, FormEngine> formEngines = processEngineConfiguration.getFormEngines();
    assertTrue(formEngines.get(null) instanceof HtmlFormEngine);

  }

  public void testTransformNullFormData() {
    HtmlFormEngine formEngine = new HtmlFormEngine();
    assertNull(formEngine.renderStartForm(null));
    assertNull(formEngine.renderTaskForm(null));
  }

  public void testHtmlElementWriter() {

    String htmlString = new HtmlDocumentBuilder(new HtmlElementWriter("someTagName"))
      .endElement()
      .getHtmlString();
    assertHtmlEquals("<someTagName></someTagName>", htmlString);

    htmlString = new HtmlDocumentBuilder(new HtmlElementWriter("someTagName", true))
      .endElement()
      .getHtmlString();
    assertHtmlEquals("<someTagName />", htmlString);

    htmlString = new HtmlDocumentBuilder(new HtmlElementWriter("someTagName", true).attribute("someAttr", "someAttrValue"))
      .endElement()
      .getHtmlString();
    assertHtmlEquals("<someTagName someAttr=\"someAttrValue\" />", htmlString);

    htmlString = new HtmlDocumentBuilder(new HtmlElementWriter("someTagName").attribute("someAttr", "someAttrValue"))
      .endElement()
      .getHtmlString();
    assertHtmlEquals("<someTagName someAttr=\"someAttrValue\"></someTagName>", htmlString);

    htmlString = new HtmlDocumentBuilder(new HtmlElementWriter("someTagName").attribute("someAttr", null))
      .endElement()
      .getHtmlString();
    assertHtmlEquals("<someTagName someAttr></someTagName>", htmlString);

    htmlString = new HtmlDocumentBuilder(new HtmlElementWriter("someTagName").textContent("someTextContent"))
      .endElement()
      .getHtmlString();
    assertHtmlEquals("<someTagName>someTextContent</someTagName>", htmlString);

    htmlString = new HtmlDocumentBuilder(
        new HtmlElementWriter("someTagName"))
          .startElement(new HtmlElementWriter("someChildTag"))
          .endElement()
        .endElement()
    .getHtmlString();
    assertHtmlEquals("<someTagName><someChildTag></someChildTag></someTagName>", htmlString);

    htmlString = new HtmlDocumentBuilder(
        new HtmlElementWriter("someTagName"))
          .startElement(new HtmlElementWriter("someChildTag").textContent("someTextContent"))
          .endElement()
        .endElement()
    .getHtmlString();
    assertHtmlEquals("<someTagName><someChildTag>someTextContent</someChildTag></someTagName>", htmlString);

    htmlString = new HtmlDocumentBuilder(
        new HtmlElementWriter("someTagName").textContent("someTextContent"))
          .startElement(new HtmlElementWriter("someChildTag"))
          .endElement()
        .endElement()
    .getHtmlString();
    assertHtmlEquals("<someTagName><someChildTag></someChildTag>someTextContent</someTagName>", htmlString);

    // invalid usage

    try {
      new HtmlElementWriter("sometagname", true).textContent("sometextcontet");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("Self-closing element cannot have text content"));
    }

  }

  @Deployment
  public void testRenderEmptyStartForm() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    assertNull(formService.getRenderedStartForm(processDefinition.getId()));

  }

  @Deployment
  public void testRenderStartForm() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    String renderedForm = (String) formService.getRenderedStartForm(processDefinition.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testRenderStartForm.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testRenderEnumField() {

    runtimeService.startProcessInstanceByKey("HtmlFormEngineTest.testRenderEnumField");

    Task t = taskService.createTaskQuery()
      .singleResult();

    String renderedForm = (String) formService.getRenderedTaskForm(t.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testRenderEnumField.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testRenderTaskForm() {

    runtimeService.startProcessInstanceByKey("HtmlFormEngineTest.testRenderTaskForm");

    Task t = taskService.createTaskQuery()
      .singleResult();

    String renderedForm = (String) formService.getRenderedTaskForm(t.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testRenderTaskForm.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testRenderDateField() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    String renderedForm = (String) formService.getRenderedStartForm(processDefinition.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testRenderDateField.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testRenderDateFieldWithPattern() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    String renderedForm = (String) formService.getRenderedStartForm(processDefinition.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testRenderDateFieldWithPattern.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testLegacyFormPropertySupport() {

    runtimeService.startProcessInstanceByKey("HtmlFormEngineTest.testLegacyFormPropertySupport");

    Task t = taskService.createTaskQuery()
      .singleResult();

    String renderedForm = (String) formService.getRenderedTaskForm(t.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testLegacyFormPropertySupport.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testLegacyFormPropertySupportReadOnly() {

    runtimeService.startProcessInstanceByKey("HtmlFormEngineTest.testLegacyFormPropertySupportReadOnly");

    Task t = taskService.createTaskQuery()
      .singleResult();

    String renderedForm = (String) formService.getRenderedTaskForm(t.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testLegacyFormPropertySupportReadOnly.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testLegacyFormPropertySupportRequired() {

    runtimeService.startProcessInstanceByKey("HtmlFormEngineTest.testLegacyFormPropertySupportRequired");

    Task t = taskService.createTaskQuery()
      .singleResult();

    String renderedForm = (String) formService.getRenderedTaskForm(t.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testLegacyFormPropertySupportRequired.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  @Deployment
  public void testBusinessKey() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    String renderedForm = (String) formService.getRenderedStartForm(processDefinition.getId());

    String expectedForm = IoUtil.readFileAsString("org/camunda/bpm/engine/test/api/form/HtmlFormEngineTest.testBusinessKey.html");

    assertHtmlEquals(expectedForm, renderedForm);

  }

  public void assertHtmlEquals(String expected, String actual) {
    assertEquals(filterWhitespace(expected), filterWhitespace(actual));
  }

  protected String filterWhitespace(String tofilter) {
    return tofilter.replaceAll("\\n", "").replaceAll("\\s", "");
  }

}

package com.camunda.fox.cycle.connector;

import java.util.UUID;

import javax.ws.rs.FormParam;

import org.codehaus.jettison.json.JSONArray;

import com.camunda.fox.cycle.util.IoUtil;


public class CreateSignavioModelForm {
  
  @FormParam("name")
  private String name;
  
  private String parent;
  
  public CreateSignavioModelForm(String name, String parent) {
    this.name = name;
    this.parent = parent;
  }

  @FormParam("comment")
  public String getComment() {
    return "MyComment";
  }

  @FormParam("description")
  public String getDescription() {
    return "MyDescription";
  }

  @FormParam("glossary_xml")
  public String getGlossaryXml() {
    return new JSONArray().toString();
  }

  @FormParam("json_xml")
  public String getJsonXml() {
    String template = IoUtil.readFileAsString("com/camunda/fox/cycle/connector/emptyProcessModelTemplate.json");
    return template;
  }

  public String getName() {
    return name;
  }
  
  @FormParam("namespace")
  public String getNamespace() {
    return "http://b3mn.org/stencilset/bpmn2.0#";
  }
  
  @FormParam("parent")
  public String getParent() {
    return "/directory" + parent;
  }

  @FormParam("svg_xml")
  public String getSvgContent() {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>";
  }
  
  @FormParam("type")
  public String getType() {
    return "BPMN 2.0";
  }
  
  @FormParam("id")
  public String getId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
  
}

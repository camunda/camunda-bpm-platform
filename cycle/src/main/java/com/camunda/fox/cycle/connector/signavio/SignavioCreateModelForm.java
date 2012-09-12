package com.camunda.fox.cycle.connector.signavio;

import javax.ws.rs.FormParam;

import org.codehaus.jettison.json.JSONArray;


public class SignavioCreateModelForm {
  
  private String parent;
  @FormParam("id")
  private String id;
  @FormParam("name")
  private String name;
  @FormParam("comment")
  private String comment;
  @FormParam("description")
  private String description;
  @FormParam("json_xml")
  private String json_xml;
  @FormParam("svg_xml")
  private String svg_xml;
  
  public SignavioCreateModelForm() {
  }

  @FormParam("comment")
  public String getComment() {
    return this.comment;
  }
  
  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

  @FormParam("glossary_xml")
  public String getGlossaryXml() {
    return new JSONArray().toString();
  }

  public String getJsonXml() {
    return this.json_xml;
  }

  public void setJsonXml(String json_xml) {
    this.json_xml = json_xml;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  @FormParam("namespace")
  public String getNamespace() {
    return "http://b3mn.org/stencilset/bpmn2.0#";
  }
  
  @FormParam("parent")
  public String getParent() {
    return "/directory" + parent;
  }
  
  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getSVG_XML() {
    return this.svg_xml;
  }
  
  public void setSVG_XML(String svg_xml) {
    this.svg_xml = svg_xml;
  }
  
  @FormParam("type")
  public String getType() {
    return "BPMN 2.0";
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

}

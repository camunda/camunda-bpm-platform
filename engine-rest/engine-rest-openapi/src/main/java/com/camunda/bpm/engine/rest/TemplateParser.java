package com.camunda.bpm.engine.rest;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateParser {

  public static void main(String[] args) throws ParseException, IOException, TemplateException {

    Configuration cfg = new Configuration();

    System.getProperties();
    cfg.setDirectoryForTemplateLoading(new File("./src/main/templates/"));
    cfg.setDefaultEncoding("UTF-8");

    Template template = cfg.getTemplate("main.ftl");

    Map<String, Object> templateData = new HashMap<>();
    // templateData.put("msg", "Today is a beautiful day");

    try (StringWriter out = new StringWriter()) {

      template.process(templateData, out);

      // System.out.println(out.getBuffer().toString()); // without formatting

      // format json with Gson
      String jsonString = out.getBuffer().toString();
      String prettyJson = formatJsonString(jsonString);

      System.out.println(prettyJson);

      out.flush();
    }
  }

  private static String formatJsonString(String jsonString) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    JsonElement el = JsonParser.parseString(jsonString);
    String prettyJson = gson.toJson(el);
    return prettyJson;
  }

}

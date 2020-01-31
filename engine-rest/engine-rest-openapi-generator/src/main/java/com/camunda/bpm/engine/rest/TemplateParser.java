package com.camunda.bpm.engine.rest;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

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

    if (args.length != 3) {
      throw new RuntimeException("Must provide two arguments: <source template directory> <main template> <output directory>");
    }

    String sourceDirectory = args[0];
    String mainTemplate = args[1];
    String outputFile = args[2];

    Configuration cfg = new Configuration();

    System.getProperties();
    cfg.setDirectoryForTemplateLoading(new File(sourceDirectory));
    cfg.setDefaultEncoding("UTF-8");

    Template template = cfg.getTemplate(mainTemplate);

    Map<String, Object> templateData = new HashMap<>();
    // templateData.put("msg", "Today is a beautiful day");

    try (StringWriter out = new StringWriter()) {

      template.process(templateData, out);

      // System.out.println(out.getBuffer().toString()); // without formatting

      // format json with Gson
      String jsonString = out.getBuffer().toString();
      String prettyJson = formatJsonString(jsonString);

//      System.out.println(prettyJson);

      File outFile = new File(outputFile);
      FileUtils.forceMkdirParent(outFile);
      FileUtils.write(outFile, prettyJson, StandardCharsets.UTF_8);
    }
  }

  private static String formatJsonString(String jsonString) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    JsonElement el = JsonParser.parseString(jsonString);
    String prettyJson = gson.toJson(el);
    return prettyJson;
  }

}

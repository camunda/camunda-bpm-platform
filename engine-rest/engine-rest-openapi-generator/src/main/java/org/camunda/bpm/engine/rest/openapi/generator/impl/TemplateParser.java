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
package org.camunda.bpm.engine.rest.openapi.generator.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateParser {

  public static void main(String[] args) throws IOException, TemplateException {

    if (args.length != 4) {
      throw new RuntimeException("Must provide four arguments: <source template directory> <main template> <output directory> <intermediate output dir>");
    }

    String sourceDirectory = args[0];
    String mainTemplate = args[1];
    String outputFile = args[2];
    String debugFile = args[3];

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

    cfg.setDirectoryForTemplateLoading(new File(sourceDirectory));
    cfg.setDefaultEncoding("UTF-8");

    Template template = cfg.getTemplate(mainTemplate);

    Map<String, Object> templateData = createTemplateData();

    try (StringWriter out = new StringWriter()) {

      template.process(templateData, out);

      // create intermediate json file before the formatting
      String path = createOutputFile(debugFile);
      FileUtils.forceMkdir(new File(debugFile));
      Files.write(Paths.get(path), out.getBuffer().toString().getBytes());

      // format json with Gson
      String jsonString = out.getBuffer().toString();
      String formattedJson = formatJsonString(jsonString);

      File outFile = new File(outputFile);
      FileUtils.forceMkdir(outFile.getParentFile());
      Files.write(outFile.toPath(), formattedJson.getBytes());
    }

  }

  protected static Map<String, Object> createTemplateData() {
    Map<String, Object> templateData = new HashMap<>();

    String version = TemplateParser.class.getPackage().getImplementationVersion();

    if (version != null) {
      // docsVersion = 7.X.Y
      templateData.put("cambpmVersion", version);

      if (version.contains("SNAPSHOT")) {
        templateData.put("docsVersion", "develop");
      } else {
        // docsVersion = 7.X
        templateData.put("docsVersion", version.substring(0, version.lastIndexOf(".")));
      }
    } else {
      // only for debug cases 
      templateData.put("cambpmVersion", "develop");
      templateData.put("docsVersion", "develop");
    }

    return templateData;
  }

  protected static String formatJsonString(String jsonString) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    JsonParser jsonParser = new JsonParser();
    JsonElement json = jsonParser.parse(jsonString);
    String formattedJson = gson.toJson(json);

    return formattedJson;
  }

  protected static String createOutputFile(String debugFile) throws IOException {
    DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ss");

    return debugFile + "/intermediate-openapi-" + timeStampPattern.format(java.time.LocalDateTime.now()) + ".json";
  }

}

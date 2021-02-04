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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

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
      throw new RuntimeException(
          "Must provide four arguments: "
          + "<source template directory> "
          + "<main template> "
          + "<output directory> "
          + "<intermediate output dir>");
    }

    String sourceDirectory = args[0];
    String mainTemplate = args[1];
    String outputFile = args[2];
    String debugFile = args[3];

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

    cfg.setDirectoryForTemplateLoading(new File(sourceDirectory));
    cfg.setDefaultEncoding("UTF-8");

    Template template = cfg.getTemplate(mainTemplate);

    Map<String, Object> templateData = createTemplateData(sourceDirectory);

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

  protected static Map<String, Object> createTemplateData(String sourceDirectory)
      throws IOException {
    Map<String, Object> templateData = new HashMap<>();

    resolveVersions(templateData);
    Map<String, String> models = resolveModels(sourceDirectory);
    templateData.put("models", models);
    Map<String, List<String>> endpoints = resolvePaths(sourceDirectory);
    templateData.put("endpoints", endpoints);

    return templateData;
  }

  protected static String formatJsonString(String jsonString) {
    Gson gson = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create();

    JsonParser jsonParser = new JsonParser();
    JsonElement json = jsonParser.parse(jsonString);
    String formattedJson = gson.toJson(json);

    return formattedJson;
  }

  protected static String createOutputFile(String debugFile) throws IOException {
    DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ss");

    return debugFile + "/intermediate-openapi-"
        + timeStampPattern.format(java.time.LocalDateTime.now()) + ".json";
  }

  /**
   * Resolve the Camunda Platform version and the respective documentation version used in the links.
   */
  protected static void resolveVersions(Map<String, Object> templateData) {
    String version = TemplateParser.class.getPackage().getImplementationVersion();
  
    if (version != null) {
      // cambpmVersion = 7.X.Y
      templateData.put("cambpmVersion", version);
  
      if (version.contains("SNAPSHOT")) {
        templateData.put("docsVersion", "develop");
      } else if (version.contains("alpha")) {
        templateData.put("docsVersion", "latest");
      } else {
        // docsVersion = 7.X
        templateData.put("docsVersion", version.substring(0, version.lastIndexOf(".")));
      }
    } else {
      // only for debug cases 
      templateData.put("cambpmVersion", "develop");
      templateData.put("docsVersion", "develop");
    }
  }

  /**
   * 
   * @param sourceDirectory the template directory that stores the models
   * @return a map of model name and file path to it,
   * the map is ordered lexicographically by the model names
   */
  protected static Map<String, String> resolveModels(String sourceDirectory) {
    File modelsDir = new File(sourceDirectory + "/models");
    Collection<File> modelFiles = FileUtils.listFiles(
        modelsDir, 
        new RegexFileFilter("^(.*?)"), 
        DirectoryFileFilter.DIRECTORY
        );

    Map<String, String> models = new TreeMap<>();
    for (File file : modelFiles) {
      String modelName = FilenameUtils.removeExtension(file.getName());
      String filePath = file.getAbsolutePath();
      String modelPackage = filePath
          .substring(filePath.lastIndexOf("org"), filePath.lastIndexOf(File.separator));
      
      models.put(modelName, modelPackage);
    }
    
    return models;
  }

  /**
   * 
   * @param sourceDirectory the template directory that stores the endpoints
   * @return a map of endpoint path and HTTP methods pairs,
   * the map is ordered lexicographically by the endpoint paths
   * the list of methods is ordered as well
   */
  protected static Map<String, List<String>> resolvePaths(String sourceDirectory) {
    File endpointsDir = new File(sourceDirectory + "/paths");
    int endpointStartAt = endpointsDir.getAbsolutePath().length();
    Collection<File> endpointsFiles = FileUtils.listFiles(
        endpointsDir, 
        new RegexFileFilter("^(.*?)"), 
        DirectoryFileFilter.DIRECTORY
        );

    Map<String, List<String>> endpoints = new TreeMap<>();
    for (File file : endpointsFiles) {
      String endpointMethod = FilenameUtils.removeExtension(file.getName());
      String filePath = file.getAbsolutePath();
      String endpointPath = filePath
          .substring(endpointStartAt, filePath.lastIndexOf(File.separator))
          .replace(File.separator, "/");

      List<String> operations;
      if (endpoints.containsKey(endpointPath)) {
        operations = endpoints.get(endpointPath);
        operations.add(endpointMethod);
      } else {
        operations = new ArrayList<>();
        operations.add(endpointMethod);
        endpoints.put(endpointPath, operations);
      }

      if(operations.size() > 1) {
        Collections.sort(operations);
      }
    }

    return endpoints;
  }

}

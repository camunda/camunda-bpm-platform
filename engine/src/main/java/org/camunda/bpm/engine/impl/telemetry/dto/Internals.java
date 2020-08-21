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
package org.camunda.bpm.engine.impl.telemetry.dto;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Internals {

  public static final String SERIALIZED_APPLICATION_SERVER = "application-server";

  protected Database database;
  @SerializedName(value = SERIALIZED_APPLICATION_SERVER)
  protected ApplicationServer applicationServer;
  protected Map<String, Command> commands;

  public Internals(Database database, ApplicationServer server) {
    super();
    this.database = database;
    this.applicationServer = server;
    this.commands = new HashMap<>();
  }

  public Database getDatabase() {
    return database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

  public ApplicationServer getApplicationServer() {
    return applicationServer;
  }

  public void setApplicationServer(ApplicationServer applicationServer) {
    this.applicationServer = applicationServer;
  }

  public Map<String, Command> getCommands() {
    return commands;
  }

  public void setCommands(Map<String, Command> commands) {
    this.commands = commands;
  }

}

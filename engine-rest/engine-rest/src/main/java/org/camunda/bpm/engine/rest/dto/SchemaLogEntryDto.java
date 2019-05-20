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
package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.management.SchemaLogEntry;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogEntryDto {

  private String id;
  private Date timestamp;
  private String version;

  public static List<SchemaLogEntryDto> fromSchemaLogEntries(List<SchemaLogEntry> entries) {
    List<SchemaLogEntryDto> dtos = new ArrayList<>();
    for (SchemaLogEntry entry : entries) {
      dtos.add(new SchemaLogEntryDto(entry.getId(), entry.getTimestamp(), entry.getVersion()));
    }
    return dtos;
  }

  public SchemaLogEntryDto(String id, Date timestamp, String version) {
    this.id = id;
    this.timestamp = timestamp;
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}

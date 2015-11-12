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
package org.camunda.bpm.engine.rest.dto.repository;

/**
 *
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionDiagramDto {

  protected String id;
  protected String cmmnXml;

  public String getId() {
    return id;
  }

  public String getCmmnXml() {
    return cmmnXml;
  }

  public static CaseDefinitionDiagramDto create(String id, String cmmnXml) {
    CaseDefinitionDiagramDto dto = new CaseDefinitionDiagramDto();

    dto.id = id;
    dto.cmmnXml = cmmnXml;

    return dto;
  }

}

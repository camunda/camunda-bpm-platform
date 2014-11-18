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
package org.camunda.bpm.engine.rest.sub;

import org.camunda.bpm.engine.rest.dto.PatchVariablesDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Map;

public interface VariableResource {

  public final static String DESERIALIZE_VALUE_QUERY_PARAM = "deserializeValue";
  public final static String DESERIALIZE_VALUES_QUERY_PARAM = DESERIALIZE_VALUE_QUERY_PARAM + "s";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Map<String, VariableValueDto> getVariables(
      @QueryParam(DESERIALIZE_VALUES_QUERY_PARAM) @DefaultValue("true") boolean deserializeValues);

  @GET
  @Path("/{varId}")
  @Produces(MediaType.APPLICATION_JSON)
  VariableValueDto getVariable(
      @PathParam("varId") String variableName,
      @QueryParam(DESERIALIZE_VALUE_QUERY_PARAM) @DefaultValue("true") boolean deserializeValue);

  @GET
  @Path("/{varId}/data")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public InputStream getVariableBinary(@PathParam("varId") String variableName);

  @PUT
  @Path("/{varId}")
  @Consumes(MediaType.APPLICATION_JSON)
  void putVariable(@PathParam("varId") String variableName, VariableValueDto variable);

  @POST // using POST since PUT is not as widely supported for file uploads
  @Path("/{varId}/data")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  void setBinaryVariable(@PathParam("varId") String variableName, MultipartFormData multipartFormData);

  @DELETE
  @Path("/{varId}")
  void deleteVariable(@PathParam("varId") String variableName);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  void modifyVariables(PatchVariablesDto patch);
}

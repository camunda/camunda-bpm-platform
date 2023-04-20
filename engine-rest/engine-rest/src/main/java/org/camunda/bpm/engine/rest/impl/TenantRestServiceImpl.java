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
package org.camunda.bpm.engine.rest.impl;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Resources.TENANT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.rest.TenantRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.identity.TenantDto;
import org.camunda.bpm.engine.rest.dto.identity.TenantQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.TenantResource;
import org.camunda.bpm.engine.rest.sub.identity.impl.TenantResourceImpl;
import org.camunda.bpm.engine.rest.util.PathUtil;
import org.camunda.bpm.engine.rest.util.QueryUtil;;

public class TenantRestServiceImpl extends AbstractAuthorizedRestResource implements TenantRestService {

  public TenantRestServiceImpl(String engineName, final ObjectMapper objectMapper) {
    super(engineName, TENANT, ANY, objectMapper);
  }

  @Override
  public TenantResource getTenant(String id) {
    id = PathUtil.decodePathParam(id);
    return new TenantResourceImpl(getProcessEngine().getName(), id, relativeRootResourcePath, getObjectMapper());
  }

  @Override
  public List<TenantDto> queryTenants(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    TenantQueryDto queryDto = new TenantQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    TenantQuery query = queryDto.toQuery(getProcessEngine());

    List<Tenant> tenants = QueryUtil.list(query, firstResult, maxResults);

    return TenantDto.fromTenantList(tenants );
  }

  @Override
  public CountResultDto getTenantCount(UriInfo uriInfo) {
    TenantQueryDto queryDto = new TenantQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    TenantQuery query = queryDto.toQuery(getProcessEngine());
    long count = query.count();

    return new CountResultDto(count);
  }

  @Override
  public void createTenant(TenantDto dto) {

    if (getIdentityService().isReadOnly()) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Identity service implementation is read-only.");
    }

    Tenant newTenant = getIdentityService().newTenant(dto.getId());
    dto.update(newTenant);

    getIdentityService().saveTenant(newTenant);
  }

  @Override
  public ResourceOptionsDto availableOperations(UriInfo context) {

    UriBuilder baseUriBuilder = context.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(TenantRestService.PATH);

    ResourceOptionsDto resourceOptionsDto = new ResourceOptionsDto();

    // GET /
    URI baseUri = baseUriBuilder.build();
    resourceOptionsDto.addReflexiveLink(baseUri, HttpMethod.GET, "list");

    // GET /count
    URI countUri = baseUriBuilder.clone().path("/count").build();
    resourceOptionsDto.addReflexiveLink(countUri, HttpMethod.GET, "count");

    // POST /create
    if (!getIdentityService().isReadOnly() && isAuthorized(CREATE)) {
      URI createUri = baseUriBuilder.clone().path("/create").build();
      resourceOptionsDto.addReflexiveLink(createUri, HttpMethod.POST, "create");
    }

    return resourceOptionsDto;
  }

  protected IdentityService getIdentityService() {
    return getProcessEngine().getIdentityService();
  }

}

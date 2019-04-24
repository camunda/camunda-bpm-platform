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
package org.camunda.bpm.engine.rest.history;

import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceReportResultDto;
import org.camunda.bpm.engine.rest.dto.history.ReportResultDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Stefan Hentschel.
 */
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricTaskInstanceReportService {

  String PATH = "/report";

  /**
   * creates a historic task instance report
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricTaskInstanceReportResultDto> getTaskReportResults(@Context UriInfo uriInfo);

  /**
   * creates a historic task instance duration report.
   */
  @GET
  @Path("/duration")
  @Produces(MediaType.APPLICATION_JSON)
  List<ReportResultDto> getTaskDurationReportResults(@Context UriInfo uriInfo);
}

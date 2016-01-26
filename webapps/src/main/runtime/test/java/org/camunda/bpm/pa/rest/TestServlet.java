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
package org.camunda.bpm.pa.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.admin.impl.web.SetupResource;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;

/**
 *
 * This servlet allows the testsuite to check whether the database is clean.
 * If the database is not clean the schema is dropped and re-created.
 *
 * Invoke using GET /camunda/ensureCleanDb/{engineName}
 *
 * Response is JSON: {"clean":true}
 *
 * @author Daniel Meyer
 *
 */
@WebServlet(urlPatterns= {"/ensureCleanDb/*"})
public class TestServlet extends HttpServlet {

  public final static Logger log = Logger.getLogger(TestServlet.class.getName());

  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
      "ACT_GE_PROPERTY"
    );

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("content-type", "application/json");

    String requestURI = req.getRequestURI();

    int lastSlash = requestURI.lastIndexOf("/");
    String engineName = requestURI.substring(lastSlash+1, requestURI.length());

    ProcessEngine processEngine = BpmPlatform.getProcessEngineService().getProcessEngine(engineName);

    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    try {
      processEngineConfiguration.setAuthorizationEnabled(false);
      doClean(req, resp, processEngine);
   }
   finally {
     processEngineConfiguration.setAuthorizationEnabled(true);
   }

  }

  protected void doClean(HttpServletRequest req, HttpServletResponse resp, ProcessEngine processEngine) throws ServletException, IOException {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    deleteAdminUser(processEngine);

    ManagementService managementService = processEngine.getManagementService();

    Collection<Meter> meters = processEngineConfiguration.getMetricsRegistry().getMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }

    log.fine("verifying that db is clean after test");
    Map<String, Long> tableCounts = managementService.getTableCount();

    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(processEngineConfiguration.getDatabaseTablePrefix(), "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count != 0L) {
          outputMessage.append("  " + tableName + ": " + count + " record(s)\n");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.severe("\n");
      log.severe(outputMessage.toString());

      log.info("dropping and recreating db");

      CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutorTxRequired();
      commandExecutor.execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          PersistenceSession persistenceSession = commandContext.getSession(PersistenceSession.class);
          persistenceSession.dbSchemaDrop();
          persistenceSession.dbSchemaCreate();
          return null;
        }
      });

      createAdminUser(processEngine);
      resp.getWriter().write("{\"clean\": false}");
    }
    else {
      log.info("database was clean");

      createAdminUser(processEngine);
      resp.getWriter().write("{\"clean\": true}");
    }
  }

  protected void createAdminUser(ProcessEngine processEngine) {
    UserDto user = new UserDto();
    UserCredentialsDto userCredentialsDto = new UserCredentialsDto();
    userCredentialsDto.setPassword("admin");
    user.setCredentials(userCredentialsDto);

    UserProfileDto userProfileDto = new UserProfileDto();
    userProfileDto.setId("admin");
    userProfileDto.setFirstName("Steve");
    userProfileDto.setLastName("Hentschi");
    user.setProfile(userProfileDto);

    new SetupResource().createInitialUser(processEngine.getName(), user);
  }

  protected void deleteAdminUser(ProcessEngine processEngine) {

    IdentityService identityService = processEngine.getIdentityService();

    identityService.deleteMembership("admin", Groups.CAMUNDA_ADMIN);

    identityService.deleteGroup(Groups.CAMUNDA_ADMIN);
    identityService.deleteUser("admin");

  }

}

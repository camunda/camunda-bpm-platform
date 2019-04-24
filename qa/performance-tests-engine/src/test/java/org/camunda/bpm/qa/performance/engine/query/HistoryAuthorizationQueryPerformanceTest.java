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
package org.camunda.bpm.qa.performance.engine.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStepBehavior;
import org.camunda.bpm.qa.performance.engine.junit.AuthorizationPerformanceTestCase;
import org.camunda.bpm.qa.performance.engine.junit.PerfTestProcessEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.camunda.bpm.engine.authorization.Resources.*;
import static org.camunda.bpm.engine.authorization.Permissions.*;

/**
 * @author Daniel Meyer
 *
 */
@SuppressWarnings("rawtypes")
@RunWith(Parameterized.class)
public class HistoryAuthorizationQueryPerformanceTest extends AuthorizationPerformanceTestCase {

  @Parameter(0)
  public static String name;

  @Parameter(1)
  public static Query query;

  @Parameter(2)
  public static Resource resource;

  @Parameter(3)
  public static Permission[] permissions;

  @Parameter(4)
  public static Authentication authentication;

  static List<Object[]> queryResourcesAndPermissions;

  static List<Authentication> authentications;

  static {
    ProcessEngine processEngine = PerfTestProcessEngine.getInstance();
    HistoryService historyService = processEngine.getHistoryService();

    queryResourcesAndPermissions = Arrays.<Object[]>asList(
        new Object[] {
            "HistoricProcessInstanceQuery",
            historyService.createHistoricProcessInstanceQuery(),
            PROCESS_DEFINITION,
            new Permission[] { READ_HISTORY }
        },
        new Object[] {
            "HistoricActivityInstanceQuery",
            historyService.createHistoricActivityInstanceQuery(),
            PROCESS_DEFINITION,
            new Permission[] { READ_HISTORY }
        }
    );

    authentications = Arrays.asList(
        new Authentication(null, Collections.<String>emptyList()){
          @Override
          public String toString() {
            return "without authentication";
          }
        },
        new Authentication("test", Collections.<String>emptyList()){
          @Override
          public String toString() {
            return "with authenticated user without groups";
          }
        },
        new Authentication("test", Arrays.asList("g0", "g1")) {
          @Override
          public String toString() {
            return "with authenticated user and 2 groups";
          }
        },
        new Authentication("test", Arrays.asList("g0", "g1", "g2", "g3", "g4", "g5", "g6", "g7", "g8", "g9")) {
          @Override
          public String toString() {
            return "with authenticated user and 10 groups";
          }
        }
    );

  }

  @Parameters(name="{0} - {4}")
  public static Iterable<Object[]> params() {
    final ArrayList<Object[]> params = new ArrayList<Object[]>();

    for (Object[] queryResourcesAndPermission : queryResourcesAndPermissions) {
      for (Authentication authentication : authentications) {
        Object[] array = new Object[queryResourcesAndPermission.length + 1];
        System.arraycopy(queryResourcesAndPermission, 0, array, 0, queryResourcesAndPermission.length);
        array[queryResourcesAndPermission.length] = authentication;
        params.add(array);
      }
    }

    return params;
  }

  @Before
  public void createAuthorizations() {
    AuthorizationService authorizationService = engine.getAuthorizationService();
    List<Authorization> auths = authorizationService.createAuthorizationQuery().list();
    for (Authorization authorization : auths) {
      authorizationService.deleteAuthorization(authorization.getId());
    }

    userGrant("test", resource, permissions);
    for (int i = 0; i < 5; i++) {
      grouptGrant("g"+i, resource, permissions);
    }
    engine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
  }

  @Test
  public void queryList() {
    performanceTest().step(new PerfTestStepBehavior() {
      public void execute(PerfTestRunContext context) {
        try {
          engine.getIdentityService().setAuthentication(authentication);
          query.listPage(0, 15);
        } finally {
          engine.getIdentityService().clearAuthentication();
        }
      }
    }).run();
  }

  @Test
  public void queryCount() {
    performanceTest().step(new PerfTestStepBehavior() {
      public void execute(PerfTestRunContext context) {
        try {
          engine.getIdentityService().setAuthentication(authentication);
          query.count();
        } finally {
          engine.getIdentityService().clearAuthentication();
        }
      }
    }).run();
  }

}

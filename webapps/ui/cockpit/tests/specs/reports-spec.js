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

'use strict';

var testHelper = require('../../../common/tests/test-helper');

var reportsPage = require('../pages/reports');

describe('Cockpit Reports Spec', function() {
  describe('reports page', function() {
    before(function() {
      return testHelper([], function() {
        reportsPage.navigateToWebapp('Cockpit');
        reportsPage.authentication.userLogin('admin', 'admin');
        reportsPage.navigateTo();
      });
    });

    it('shows that no report is available', function() {
      // then
      reportsPage.isActive();
      expect(reportsPage.noReportsAvailableHint().isPresent()).to.eventually.be
        .true;
    });
  });
});

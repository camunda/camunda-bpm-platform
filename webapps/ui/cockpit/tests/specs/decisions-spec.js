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
var setupFile = require('./dashboard-setup');

var decisionsPage = require('../pages/decisions');

describe('Cockpit Decisions Dashboard Spec', function() {
  describe('dashboard page navigation', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {
        decisionsPage.navigateToWebapp('Cockpit');
        decisionsPage.authentication.userLogin('admin', 'admin');
        decisionsPage.goToSection('Decisions');
      });
    });

    describe('deploy decision and validate', function() {
      before(function() {
        return testHelper(setupFile.setup4, true);
      });

      it('should show deployed decision', function() {
        // when
        decisionsPage.navigateTo();

        // then
        expect(
          decisionsPage.deployedDecisionsList.decisionCountHeader()
        ).to.eventually.eql('1 decision definition deployed');
        expect(
          decisionsPage.deployedDecisionsList.decisionsList().count()
        ).to.eventually.eql(1);
        expect(
          decisionsPage.deployedDecisionsList.decisionName(0)
        ).to.eventually.eql('Assign Approver');
      });
    });

    describe('deploy decision without name', function() {
      before(function() {
        return testHelper(setupFile.setup5, true);
      });

      it('should show decision key', function() {
        // when
        decisionsPage.navigateTo();

        // then
        expect(
          decisionsPage.deployedDecisionsList.decisionCountHeader()
        ).to.eventually.eql('2 decision definitions deployed');
        expect(
          decisionsPage.deployedDecisionsList.decisionsList().count()
        ).to.eventually.eql(2);
        expect(
          decisionsPage.deployedDecisionsList.decisionName(1)
        ).to.eventually.eql('invoice-approver');
      });
    });
  });

  describe('multi tenancy', function() {
    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {
        decisionsPage.navigateToWebapp('Cockpit');
        decisionsPage.authentication.userLogin('admin', 'admin');
        decisionsPage.goToSection('Decisions');
      });
    });

    it('should show tenant ids of decision definitions', function() {
      expect(
        decisionsPage.deployedDecisionsList.decisionsList().count()
      ).to.eventually.eql(2);

      expect(decisionsPage.deployedDecisionsList.tenantId(0)).to.eventually.eql(
        ''
      );
      expect(decisionsPage.deployedDecisionsList.tenantId(1)).to.eventually.eql(
        'tenant1'
      );
    });
  });
});

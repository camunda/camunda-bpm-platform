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
var setupFile = require('./process-setup');

var dashboardPage = require('../pages/dashboard');
var processesPage = require('../pages/processes');
var definitionPage = require('../pages/process-definition');

describe('Cockpit Process Definition Filter Spec', function() {
  afterEach(function() {
    definitionPage.search.clearSearch();
  });

  describe('filter by variable', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
      });
    });

    it('should filter by number', function() {
      // when
      definitionPage.search.createSearch('Variable', '=', '1.5', 'test');

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(2);
    });

    it('should filter by string', function() {
      // when
      definitionPage.search.createSearch(
        'Variable',
        '=',
        'abc dfg',
        'myString'
      );

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(2);
      expect(
        definitionPage.processInstancesTab.businessKey(1).getText()
      ).to.eventually.eql('Instance2');
    });

    it('should add like filter', function() {
      // when
      definitionPage.search.createSearch('Variable', 'like', '123', 'myString');

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(1);
      expect(
        definitionPage.processInstancesTab.businessKey(0).getText()
      ).to.eventually.eql('Instance1');
    });
  });

  describe('filter by business key and variable', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
      });
    });

    it('should add business key filter', function() {
      // when
      definitionPage.search.createSearch('Business Key', 'myBusinessKey');

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(1);
      expect(
        definitionPage.processInstancesTab.businessKey(0).getText()
      ).to.eventually.eql('myBusinessKey');
    });

    it('should combine variable filter and business key filter', function() {
      // when
      definitionPage.search.createSearch('Variable', '>', '1.49', 'test');

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(2);

      // when
      definitionPage.search.createSearch('Business Key', 'Instance1');

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(1);
      expect(
        definitionPage.processInstancesTab.businessKey(0).getText()
      ).to.eventually.eql('Instance1');
    });
  });

  describe.skip('filtering with long expressions', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Cockpit');
        dashboardPage.authentication.userLogin('admin', 'admin');
        dashboardPage.goToSection('Processes');
        processesPage.deployedProcessesList.selectProcess(0);
      });
    });

    afterEach(function() {
      definitionPage.filter.removeVariableFilter(0);
    });

    it('should filter date', function() {
      // when
      definitionPage.search.createSearch(
        'Variable',
        '=',
        '2011-11-11T11:11:11',
        'myDate'
      );

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(1);
      expect(
        definitionPage.processInstancesTab.businessKey(0).getText()
      ).to.eventually.eql('myBusinessKey');
    });

    it('should filter long variable', function() {
      // when
      definitionPage.search.createSearch(
        'Variable',
        '=',
        '1234567890987654321',
        'extraLong'
      );

      // then
      expect(
        definitionPage.processInstancesTab.table().count()
      ).to.eventually.eql(1);
      expect(
        definitionPage.processInstancesTab.businessKey(0).getText()
      ).to.eventually.eql('Instance1');
    });
  });
});

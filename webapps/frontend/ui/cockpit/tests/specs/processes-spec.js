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

var processesPage = require('../pages/processes');

describe('Cockpit Processes Dashboard Spec', function() {
  describe('dashboard page navigation', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {
        processesPage.navigateToWebapp('Cockpit');
        processesPage.authentication.userLogin('admin', 'admin');
        processesPage.goToSection('Processes');
      });
    });

    it('should validate processes plugin', function() {
      // then
      processesPage.isActive();
      expect(
        processesPage.deployedProcessesList.processCountHeader()
      ).to.eventually.eql('1 process definition deployed');
    });

    it('should validate process previews tab', function() {
      // when
      processesPage.deployedProcessesPreviews.switchTab();

      // then
      expect(
        processesPage.deployedProcessesPreviews.processesPreviews().count()
      ).to.eventually.eql(1);
    });

    it('should validate prosess list tab', function() {
      // when
      processesPage.deployedProcessesList.switchTab();

      // then
      expect(
        processesPage.deployedProcessesList.processesList().count()
      ).to.eventually.eql(1);
      expect(
        processesPage.deployedProcessesList.processName(0)
      ).to.eventually.eql('Failing Process');
      expect(
        processesPage.deployedProcessesList.runningInstances(0)
      ).to.eventually.eql('0');
    });

    it('should not display report column in process list tab', function() {
      // then
      expect(processesPage.deployedProcessesList.getReportColumn().isPresent())
        .to.eventually.be.false;
    });

    describe('start instance and validate', function() {
      before(function() {
        return testHelper(setupFile.setup2, true);
      });

      it('should count number of processes', function() {
        // when
        processesPage.navigateTo();

        // then
        expect(
          processesPage.deployedProcessesList.processCountHeader()
        ).to.eventually.eql('1 process definition deployed');
        expect(
          processesPage.deployedProcessesList.processesList().count()
        ).to.eventually.eql(1);
        expect(
          processesPage.deployedProcessesList.runningInstances(0)
        ).to.eventually.eql('1');
      });
    });

    describe('deploy process and validate', function() {
      before(function() {
        return testHelper(setupFile.setup3, true);
      });

      it('should validate process list', function() {
        // when
        processesPage.navigateTo();

        // then
        expect(
          processesPage.deployedProcessesList.processCountHeader()
        ).to.eventually.eql('2 process definitions deployed');
        expect(
          processesPage.deployedProcessesList.processesList().count()
        ).to.eventually.eql(2);
        expect(
          processesPage.deployedProcessesList.runningInstances(1)
        ).to.eventually.eql('1');
        expect(
          processesPage.deployedProcessesList.processName(1)
        ).to.eventually.eql('processWithSubProcess');
      });

      it('should validate process previews', function() {
        // when
        processesPage.deployedProcessesPreviews.switchTab();

        // then
        expect(
          processesPage.deployedProcessesPreviews.processesPreviews().count()
        ).to.eventually.eql(2);
      });
    });
  });

  describe('multi tenancy', function() {
    before(function() {
      return testHelper(setupFile.multiTenancySetup, function() {
        processesPage.navigateToWebapp('Cockpit');
        processesPage.authentication.userLogin('admin', 'admin');
        processesPage.goToSection('Processes');
      });
    });

    it('should show tenant ids of process definitions', function() {
      expect(
        processesPage.deployedProcessesList.processesList().count()
      ).to.eventually.eql(2);

      expect(processesPage.deployedProcessesList.tenantId(0)).to.eventually.eql(
        ''
      );
      expect(processesPage.deployedProcessesList.tenantId(1)).to.eventually.eql(
        'tenant1'
      );
    });

    it('should aggregate process instance count by tenant id', function() {
      expect(
        processesPage.deployedProcessesList.processesList().count()
      ).to.eventually.eql(2);

      expect(
        processesPage.deployedProcessesList.runningInstances(0)
      ).to.eventually.eql('1');
      expect(
        processesPage.deployedProcessesList.runningInstances(1)
      ).to.eventually.eql('1');
    });
  });
});

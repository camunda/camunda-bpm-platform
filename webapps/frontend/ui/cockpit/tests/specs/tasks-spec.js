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
var setupFile = require('./task-setup');

var tasksPage = require('../pages/tasks');

describe('Cockpit Tasks Dashboard Spec', function() {
  describe('dashboard page navigation', function() {
    before(function() {
      return testHelper(setupFile.setup1, function() {
        // when
        tasksPage.navigateToWebapp('Cockpit');
        tasksPage.authentication.userLogin('admin', 'admin');
        tasksPage.goToSection('Human Tasks');
      });
    });

    describe('open tasks statistics', function() {
      it('should show total open tasks', function() {
        // then
        expect(
          tasksPage.openTasksStatistics.taskStatisticsTableHeadCount()
        ).to.eventually.eql('3');
      });

      it('should show task counts for assigned/claimed tasks', function() {
        // then
        expect(
          tasksPage.openTasksStatistics.taskStatisticLabel(0)
        ).to.eventually.eql('assigned to a user');
        expect(
          tasksPage.openTasksStatistics.taskStatisticCount(0)
        ).to.eventually.eql('1');
      });

      it('should show tasks counts for tasks with a group', function() {
        // then
        expect(
          tasksPage.openTasksStatistics.taskStatisticLabel(1)
        ).to.eventually.eql('assigned to 1 or more groups');
        expect(
          tasksPage.openTasksStatistics.taskStatisticCount(1)
        ).to.eventually.eql('1');
      });

      it('should show tasks counts for tasks without a user or a group', function() {
        // then
        expect(
          tasksPage.openTasksStatistics.taskStatisticLabel(2)
        ).to.eventually.eql('unassigned');
        expect(
          tasksPage.openTasksStatistics.taskStatisticCount(2)
        ).to.eventually.eql('1');
      });
    });

    describe('open task count by group table', function() {
      it('should contain a number groups', function() {
        // then
        expect(
          tasksPage.openTasksStatistics.taskGroupList()
        ).to.eventually.have.length(3);
      });

      it('should show a group entry', function() {
        // then
        expect(
          tasksPage.openTasksStatistics.taskGroupName(0)
        ).to.eventually.eql('without group');
        expect(
          tasksPage.openTasksStatistics.taskGroupCount(0)
        ).to.eventually.eql('1');
      });

      it('should show an info that a task can have multiple groups', function() {
        // then
        expect(
          tasksPage.openTasksStatistics.multipleGroupsInfo()
        ).to.eventually.eql(true);
      });
    });
  });
});

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

var Base = require('./tasks-plugin');

module.exports = Base.extend({
  listObject: function() {
    return element(by.css('.tasks-dashboard'));
  },

  taskStatisticsTable: function() {
    return this.listObject().element(by.css('#open-task-statistics'));
  },

  taskGroupTable: function() {
    return this.listObject().element(by.css('#task-group-counts'));
  },

  taskStatisticsList: function() {
    return this.taskStatisticsTable().all(
      by.repeater('taskStatistic in taskStatistics')
    );
  },

  taskGroupList: function() {
    return this.taskGroupTable().all(by.repeater('taskGroup in taskGroups'));
  },

  taskStatisticsTableHeadCount: function() {
    return this.taskStatisticsTable()
      .element(by.binding('{{ openTasksCount }}'))
      .getText();
  },

  taskStatisticLabel: function(item) {
    return this.taskStatisticsList()
      .get(item)
      .element(by.binding('{{ taskStatistic.label }}'))
      .getText();
  },

  taskStatisticCount: function(item) {
    return this.taskStatisticsList()
      .get(item)
      .element(by.binding('{{ taskStatistic.count }}'))
      .getText();
  },

  taskGroupName: function(item) {
    return this.taskGroupList()
      .get(item)
      .element(by.binding('{{ formatGroupName(taskGroup.groupName) }}'))
      .getText();
  },

  taskGroupCount: function(item) {
    return this.taskGroupList()
      .get(item)
      .element(by.binding('{{ taskGroup.taskCount }}'))
      .getText();
  },

  multipleGroupsInfo: function() {
    return this.listObject()
      .element(by.css('#multiple-groups-info'))
      .isDisplayed();
  }
});

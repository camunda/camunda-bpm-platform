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
    return this.taskStatisticsTable().all(by.repeater('taskStatistic in taskStatistics'));
  },

  taskGroupList: function() {
    return this.taskGroupTable().all(by.repeater('taskGroup in taskGroups'));
  },

  taskStatisticsTableHeadCount : function() {
    return this.taskStatisticsTable().element(by.binding('{{ openTasksCount }}')).getText();
  },

  taskStatisticLabel: function(item) {
    return this.taskStatisticsList().get(item).element(by.binding('{{ taskStatistic.label }}')).getText();
  },

  taskStatisticCount: function(item) {
    return this.taskStatisticsList().get(item).element(by.binding('{{ taskStatistic.count }}')).getText();
  },

  taskGroupName: function(item) {
    return this.taskGroupList().get(item).element(by.binding('{{ formatGroupName(taskGroup.groupName) }}')).getText();
  },

  taskGroupCount: function(item) {
    return this.taskGroupList().get(item).element(by.binding('{{ taskGroup.taskCount }}')).getText();
  },

  multipleGroupsInfo : function() {
    return this.listObject().element(by.css('#multiple-groups-info')).isDisplayed();
  }

});

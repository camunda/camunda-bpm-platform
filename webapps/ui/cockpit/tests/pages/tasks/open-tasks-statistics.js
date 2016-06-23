'use strict';

var Base = require('./tasks-plugin');

module.exports = Base.extend({

  listObject: function() {
    return this.pluginObject().element(by.css('.open-tasks'));
  },

  taskStatisticsTable: function() {
    return this.listObject().element(by.css('#open-task-statistics'));
  },

  taskGroupTable: function() {
    return this.listObject().element(by.css('#task-group-counts'));
  },

  taskStatisticsList: function() {
    return this.taskStatisticsTable().all(by.repeater('taskCount in taskCountStatistic'));
  },
  
  taskGroupList: function() {
    return this.taskGroupTable().all(by.repeater('taskCountObj in taskCountObjects'));
  },

  taskStatisticsTableHeadCount : function() {
    return this.taskStatisticsTable().element(by.binding('{{ openTasksCount }}')).getText();
  },

  taskStatisticLabel: function(item) {
    return this.taskStatisticsList().get(item).element(by.binding('{{ taskCount.label }}')).getText();
  },

  taskStatisticCount: function(item) {
    return this.taskStatisticsList().get(item).element(by.binding('{{ taskCount.count }}')).getText();
  },
  
  taskGroupName: function(item) {
    return this.taskGroupList().get(item).element(by.binding('{{ formatGroupName(taskCountObj.groupName) }}')).getText();
  },
  
  taskGroupCount: function(item) {
    return this.taskGroupList().get(item).element(by.binding('{{ taskCountObj.taskCount }}')).getText();
  },

  multipleGroupsInfo : function() {
    return this.listObject().element(by.css('#multiple-groups-info')).isDisplayed();
  }

});

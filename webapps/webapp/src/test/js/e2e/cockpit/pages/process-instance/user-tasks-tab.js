'use strict';

var Base = require('./../table');
var repeater = 'tabProvider in processInstanceTabs';
var tabIndex = 3;

module.exports = Base.extend({

  selectUserTasksTab: function() {
    this.selectTab(repeater, tabIndex);
  },

  userTasksTabName: function() {
    return this.tabName(repeater, tabIndex);
  },

  isUserTasksTabSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).toMatch('ng-scope active');
  },

  isUserTasksTabNotSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).not.toMatch('ng-scope active');
  },

  userTasksTable: function() {
    this.selectUserTasksTab();
    return element.all(by.repeater('userTask in userTasks'));
  },

  selectUserTask: function(item) {
    this.userTasksTable().get(item).element(by.binding('userTask.name')).click();
  }

});
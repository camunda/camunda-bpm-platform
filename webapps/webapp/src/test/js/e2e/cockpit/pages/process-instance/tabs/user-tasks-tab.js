'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 3,
  tabLabel: 'User Tasks',
  tableRepeater: 'userTask in userTasks',

  userTaskName: function(item) {
    return this.tableItem(item, 'userTask.instance.name').getText();
  },

  selectUserTask: function(item) {
    this.tableItem(item, 'userTask.instance.name').click();
  }

});
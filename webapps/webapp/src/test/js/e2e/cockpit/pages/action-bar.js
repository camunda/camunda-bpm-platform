'use strict';

var SideBar = require('./base');

module.exports = SideBar.extend({

  actionButtons: function() {
    return element.all(by.repeater(this.barRepeater));
  },

  getActionButton: function(item) {
    return this.actionButtons().get(item).element(by.css('[ng-click="openDialog()"]'));
  },

  clickActionButton: function(item) {
    this.actionButtons().get(item).element(by.css('[ng-click="openDialog()"]')).click();
  }

});
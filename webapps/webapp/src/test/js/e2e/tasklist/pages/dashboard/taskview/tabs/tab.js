'use strict';

var Tab = require('./../current-task');

module.exports = Tab.extend({

  tabs: function() {
    return element.all(by.repeater('taskDetailTab in taskDetailTabs'));
  },

  selectTab: function () {
    this.tabs().get(this.tabIndex).click();
  }

});
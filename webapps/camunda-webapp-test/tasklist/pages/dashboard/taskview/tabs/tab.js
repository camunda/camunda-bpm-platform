'use strict';

var Tab = require('./../current-task');

module.exports = Tab.extend({

  tabs: function() {
    return element.all(by.repeater('taskDetailTab in taskDetailTabs'));
  },

  selectTab: function () {
    var theTabElement = this.tabs().get(this.tabIndex);
    this.waitForElementToBeVisible(theTabElement, 5000);
    theTabElement.click();
  }

});

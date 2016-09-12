'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/decision-instance/:decision',

  pageHeader: function() {
    this.waitForElementToBeVisible(element(by.css('.ctn-header h1')));
    return element(by.css('.ctn-header h1'));
  },

  processInstanceLink: function() {
    return element(by.css('.super-process-instance-id'));
  },

  gotoProcessInstanceButton: function() {
    return this.processInstanceLink().element(by.css('a'));
  },

  gotoProcessInstance: function() {
    return this.gotoProcessInstanceButton().click();
  }

});

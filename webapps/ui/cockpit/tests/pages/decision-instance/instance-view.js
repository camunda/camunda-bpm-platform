'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/decision-instance/:decision',

  pageHeader: function() {
    this.waitForElementToBeVisible(element(by.css('.ctn-header h1')));
    return element(by.css('.ctn-header h1'));
  },

  pageHeaderDecisionInstanceId: function() {
    return this.pageHeader().getText().then(function(fullString) {
      return fullString.replace('<', '').replace('>', '');
    });
  }

});

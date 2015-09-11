'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/decision-instance/:decision',

  pageHeader: function() {
    return element(by.binding('decisionInstance.id'));
  },

  pageHeaderDecisionInstanceId: function() {
    return this.pageHeader().getText().then(function(fullString) {
      return fullString.replace('<', '').replace('>', '');
    });
  }

});

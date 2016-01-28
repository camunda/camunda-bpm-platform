'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/decision-definition/:decision',

  pageHeader: function() {
    return element(by.css('.ctn-header h1'));
  },

  pageHeaderDecisionDefinitionName: function() {
    return element(by.binding('decisionDefinition.key')).getText().then(function(fullString) {
      return fullString.replace('DECISION DEFINITION\n', '');
    });
  }

});

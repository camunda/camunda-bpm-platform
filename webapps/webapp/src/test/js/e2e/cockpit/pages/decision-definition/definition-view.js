'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/decision-definition/:decision',

  pageHeader: function() {
    return element(by.binding('decisionDefinition.key'));
  },

  fullPageHeaderDecisionDefinitionName: function() {
    return this.pageHeader().getText();
  },

  pageHeaderDecisionDefinitionName: function() {
    return element(by.binding('decisionDefinition.key')).getText().then(function(fullString) {
      return fullString.replace('DECISION DEFINITION\n', '');
    });
  }

});

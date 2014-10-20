'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/process-definition/:process/runtime',

  pageHeader: function() {
    return element(by.binding('processDefinition.key'));
  },

  fullPageHeaderProcessDefinitionName: function() {
    return this.pageHeader().getText();
  },

  pageHeaderProcessDefinitionName: function() {
    return element(by.binding('processDefinition.key')).getText().then(function(fullString) {
      return fullString.replace('PROCESS DEFINITION\n', '');
    });
  }

});
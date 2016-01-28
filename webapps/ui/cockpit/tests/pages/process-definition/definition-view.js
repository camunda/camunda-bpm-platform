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
  },

  isDefinitionSuspended: function() {
    return element(by.css('.ctn-header .badge'))
      .getAttribute('class')
      .then(function(classes) {
        return classes.indexOf('ng-hide') === -1;
      });
  },

  getReportLink: function() {
    return element(by.css('a.report-link'));
  }

});

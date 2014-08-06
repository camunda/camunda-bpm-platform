'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/process-definition/:process/runtime',

  pageHeaderProcessDefinitionName: function() {
    return element(by.binding('processDefinition.key')).getText();
  }

});
'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/process-instance/:instance/runtime',

  pageHeaderProcessInstanceName: function() {
    return element(by.binding('processInstance.id')).getText();
  }

});
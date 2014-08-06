'use strict';


var Base = require('./definition-view');

module.exports = Base.extend({

  processInstanceTable: function() {
    return element.all(by.repeater('processInstance in processInstances'));
  },

  selectProcessInstance: function(item) {
    this.processInstanceTable().get(item).element(by.binding('processInstance.id')).click();
  }

});

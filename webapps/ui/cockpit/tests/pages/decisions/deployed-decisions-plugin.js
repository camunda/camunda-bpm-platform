'use strict';

var Base = require('./../dashboard/dashboard-view');

module.exports = Base.extend({

  pluginList: function () {
    return element.all(by.css('.dashboard-view'));
  },

  pluginObject: function() {
    return this.pluginList().get(0);
  },

  decisionCountHeader: function() {
    return this.pluginObject().element(by.binding('{{decisionCount}}')).getText();
  }

});

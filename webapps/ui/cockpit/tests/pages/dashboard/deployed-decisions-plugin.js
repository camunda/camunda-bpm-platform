'use strict';

var Base = require('./dashboard-view');

module.exports = Base.extend({

  pluginObject: function() {
    return this.pluginList().get(1);
  },

  decisionCountHeader: function() {
    return this.pluginObject().element(by.binding('{{decisionCount}}')).getText();
  }

});

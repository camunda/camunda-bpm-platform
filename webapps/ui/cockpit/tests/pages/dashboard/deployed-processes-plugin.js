'use strict';

var Base = require('./dashboard-view');

module.exports = Base.extend({

  pluginObject: function() {
    return this.pluginList().get(0);
  },

  processCountHeader: function() {
    return this.pluginObject().element(by.binding('{{ statistics.length }}')).getText();
  },

  switchTab: function() {
    element(by.css('[select="selectTab(\'' + this.tabLabel.toLowerCase() +  '\')"]')).click();
  }

});

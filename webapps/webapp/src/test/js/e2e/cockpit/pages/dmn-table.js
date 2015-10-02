'use strict';

var Base = require('./base');

module.exports = Base.extend({

  tableElement: function() {
    return element(by.css('[cam-widget-dmn-viewer]'));
  },

  row: function(idx) {
    return this.tableElement().all(by.css('tbody > tr')).get(idx);
  }

});

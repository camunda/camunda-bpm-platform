'use strict';

var Base = require('./adminSetup');

module.exports = Base.extend({

  statusMessage: function() {
    return element(by.css('.alert')).getText();
  }
});
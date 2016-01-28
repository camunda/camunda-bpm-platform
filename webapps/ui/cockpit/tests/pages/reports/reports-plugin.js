'use strict';

var Page = require('./reports-view');

module.exports = Page.extend({

  pluginObject: function() {
    return element(by.css('[cam-reports-plugin]'));
  }

});

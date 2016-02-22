'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/decision-definition/:decision',

  pageHeader: function() {
    return element(by.css('.ctn-header h1'));
  }
});

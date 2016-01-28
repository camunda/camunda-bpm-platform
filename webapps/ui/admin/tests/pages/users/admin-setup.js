'use strict';

var Base = require('./new-base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/setup/#/setup',

  createNewAdminButton: function() {
    return element(by.css('.btn.btn-primary'));
  },

  statusMessage: function() {
    return element(by.css('.alert')).getText();
  }

});
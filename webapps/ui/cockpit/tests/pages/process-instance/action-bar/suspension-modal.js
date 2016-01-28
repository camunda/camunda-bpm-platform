  'use strict';

var Base = require('./../../base');

module.exports = Base.extend({

  suspendButton: function() {
    return element(by.css('.modal-footer [ng-click="updateSuspensionState()"]'));
  },

  okButton: function() {
    return element(by.css('.modal-footer [ng-click="close(status)"]:not(.ng-hide)'));
  }

});

  'use strict';

var Base = require('./../../base');

module.exports = Base.extend({

  cancelButton: function() {
    return element(by.css('.modal-footer [ng-click="cancelProcessInstance()"]'));
  },

  okButton: function() {
    return element(by.css('.modal-footer [ng-click="close(status)"]:not(.ng-hide)'));
  }

});

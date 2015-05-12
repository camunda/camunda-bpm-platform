'use strict';

var ActionBar = require('./../../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  suspendButton: function() {
    return this.getActionButton(3);
  },

  suspensionModalSuspendButton: function() {
    return element(by.css('.modal-footer [ng-click="updateSuspensionState()"]'));
  },

  suspensionModalOkButton: function() {
    return element(by.css('.modal-footer [ng-click="close(status)"]:not(.ng-hide)'));
  },

  suspendInstance: function() {
    var that = this;
    this.suspendButton().click().then(function() {
      browser.sleep(500);
      that.suspensionModalSuspendButton().click().then(function(){
        browser.sleep(500);
        that.suspensionModalOkButton().click();
      });
    });
  },

  activateInstance: function() {
    this.suspendInstance();
  }

});

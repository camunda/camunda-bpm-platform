'use strict';

var ActionBar = require('./../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  cancelButton: function() {
    return this.getActionButton(0);
  },

  clickCancelButton: function() {
    this.clickActionButton(0);
  },

  retryButton: function() {
    return this.getActionButton(1);
  },

  clickRetryButton: function() {
    this.clickActionButton(1);
  },

  addVariableButton: function() {
    return this.getActionButton(2);
  },

  clickAddVariableButton: function() {
    this.clickActionButton(2);
  },

  suspendButton: function() {
    return this.getActionButton(3);
  },

  clickSuspendButton: function() {
    this.clickActionButton(3);
  },

  clickActivateButton: function() {
    this.clickActionButton(3);
  },

  suspensionModalSuspendButton: function() {
    return element(by.css('.modal-footer [ng-click="updateSuspensionState()"]'));
  },

  clickSuspensionModalSuspendButton: function() {
    this.suspensionModalSuspendButton().click();
  },

  clickSuspensionModalActivateButton: function() {
    this.clickSuspensionModalSuspendButton();
  },

  suspensionModalOkButton: function() {
    return element(by.css('.modal-footer [ng-click="close(status)"]:not(.ng-hide)'));
  },

  clickSuspensionModalOkButton: function() {
    this.suspensionModalOkButton().click();
  },

  suspendInstance: function() {
    var that = this;
    this.suspendButton().click().then(function() {
      that.suspensionModalSuspendButton().click().then(function(){
        that.clickSuspensionModalOkButton();
      });
    });
  },

  activateInstance: function() {
    this.suspendInstance();
  }

});

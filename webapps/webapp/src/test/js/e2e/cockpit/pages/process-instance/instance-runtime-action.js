'use strict';

var ActionBar = require('./../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  cancelButton: function() {
    return this.getActionButton(0);
  },

  retryButton: function() {
    return this.getActionButton(1);
  },

  addVariableButton: function() {
    return this.getActionButton(2);
  },

  addVariableModalAddButton: function() {
    return element(by.css('[ng-click="save()"]'));
  },

  addVariableModalOkButton: function() {
    return element(by.css('.modal-footer [ng-click="close()"]:not(.ng-hide)'));
  },

  addVariable: function(name, type, value) {
    var that = this;

    var submitFct = function() {
      that.addVariableModalAddButton().click().then(function() {
        that.addVariableModalOkButton().click();
      });
    };

    this.addVariableButton().click().then(function() {
      element(by.model('newVariable.name')).sendKeys(name).then(function() {
        element(by.css('.modal-body [ng-model="newVariable.type"]')).element(by.cssContainingText('option', type)).click().then(function() {
          if(value) {
            if(typeof value === 'object') {
              element(by.model('variable.valueInfo.objectTypeName')).sendKeys(value.objectTypeName);
              element(by.model('variable.valueInfo.serializationDataFormat')).sendKeys(value.serializationDataFormat);
              element(by.model('variable.value')).sendKeys(value.value).then(submitFct);
            } else {
              element(by.model('variable.value')).sendKeys(value).then(submitFct);
            }
          } else {
            submitFct();
          }
        });
      });
    });
  },

  suspendButton: function() {
    return this.getActionButton(3);
  },

  clickSuspendButton: function() {
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

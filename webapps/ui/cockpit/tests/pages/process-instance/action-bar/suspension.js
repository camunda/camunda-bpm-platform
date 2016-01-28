'use strict';

var ActionBar = require('./../../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  suspendInstanceButton: function() {
    return this.getActionButton(3);
  },

  suspendInstance: function() {
    var modal = this.modal;
    this.suspendInstanceButton().click().then(function() {
      browser.sleep(500);
      modal.suspendButton().click().then(function(){
        browser.sleep(500);
        modal.okButton().click();
      });
    });
  },

  activateInstance: function() {
    this.suspendInstance();
  }

});

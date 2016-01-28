'use strict';

var ActionBar = require('./../../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'actionProvider in processDefinitionActions',

  suspendDefinitionButton: function() {
    return this.getActionButton(0);
  },

  suspendDefinition: function() {
    var modal = this.modal;
    this.suspendDefinitionButton().click().then(function() {
      browser.sleep(500);
      modal.suspendButton().click().then(function(){
        browser.sleep(500);
        modal.okButton().click();
      });
    });
  },

  activateDefinition: function() {
    this.suspendDefinition();
  }

});

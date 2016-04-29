'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/tenant-create',

  newTenantIdInput: function(inputValue) {
    var inputField = element(by.model('tenant.id'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  newTenantNameInput: function (inputValue) {
    var inputField = element(by.model('tenant.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  createNewTenantButton: function () {
    return element(by.css('[ng-click="createTenant()"]'));
  },

  createNewTenant: function (tenantId, tenantName) {
    this.newTenantIdInput(tenantId);
    this.newTenantNameInput(tenantName);
    this.createNewTenantButton().click();
  }
});

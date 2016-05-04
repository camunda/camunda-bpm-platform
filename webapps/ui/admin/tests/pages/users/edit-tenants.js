'use strict';

var Page = require('./edit-base');

var formElement = element(by.css('form[name="updateTenantMemberships"]'));

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/users/:user?tab=tenants',

  subHeader: function() {
    return formElement.element(by.css('.h4')).getText();
  },

  tenantList: function() {
    return formElement.all(by.repeater('tenant in tenantList'));
  },

  tenantId: function(idx) {
    return this.tenantList().get(idx).element(by.binding('{{ tenant.id }}')).getText();
  },

  openAddTenantModal: function() {
    var theElement = element(by.css('.modal-header'));
    this.addTenantButton().click();
    this.waitForElementToBeVisible(theElement, 5000);
  },

  addTenantButton: function() {
    return element(by.css('[ng-click="openCreateTenantMembershipDialog()"]'));
  },

  removeTenant: function(idx) {
    this.tenantList().get(idx).element(by.css('[ng-click="removeTenant(tenant.id)"]')).click();
  }
});

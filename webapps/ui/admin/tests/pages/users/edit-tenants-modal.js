'use strict';

var Page = require('./edit-tenants');

module.exports = Page.extend({

  pageHeader: function() {
    return element(by.css('.modal-header')).getText();
  },

  tenantList: function() {
    return element.all(by.repeater('tenant in availableTenants'));
  },

  selectTenant: function(idx) {
    this.tenantList().get(idx).element(by.model('tenant.checked')).click();
  },

  tenantId: function(idx) {
    return this.tenantList().get(idx).element(by.css('.tenant-id a'));
  },

  tenantName: function(idx) {
    return this.tenantList().get(idx).element(by.css('.tenant-name'));
  },

  addSelectedTenantButton: function() {
    return element(by.css('[ng-click="createUserMemberships()"]'));
  },

  cancelButton: function() {
    return element(by.css('[ng-click="close()"]'));
  },

  okButton: function() {
    return element(by.css('[ng-click="close(status)"]'));
  },

  selectAllCheckbox: function() {
    return element(by.css('[ng-click="checkAllTenants()"'));
  },

  addTenants: function() {
    var that = this;
    var theElement = this.tenantList().get(0);

    this.waitForElementToBeVisible(theElement, 5000);
    this.selectAllCheckbox().click();
    this.addSelectedTenantButton().click().then(function() {
      that.okButton().click();
    });
  },

  addTenant: function(idx) {
    var that = this;
    var theElement = this.tenantList().get(idx);

    this.waitForElementToBeVisible(theElement, 5000);
    this.selectTenant(idx);
    this.addSelectedTenantButton().click().then(function() {
      that.okButton().click();
    });
  }

});

'use strict';

var Page = require('./../base');

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/tenants/:tenant?tab=tenant',

  updateTenantButton: function() {
    return element(by.css('[ng-click="updateTenant()"]'));
  },

  tenantNameInput: function(inputValue) {
    var inputField = element(by.model('tenant.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  deleteTenantButton: function() {
    return element(by.css('[ng-click="deleteTenant()"]'));
  },

  deleteTenantAlert: function() {
    return browser.switchTo().alert();
  },

  deleteTenant: function() {
    this.deleteTenantButton().click();
    element(by.css('.modal-footer [ng-click="$close()"]')).click();
  },

  selectUserNavbarItem: function(navbarItem) {
    var index = [
      'Information',
      'Groups',
      'Users'
    ];
    
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (itemIndex) {
      item = element(by.css('aside ul li:nth-child(' + itemIndex + ')'));
    } else {
      item = element(by.css('aside ul li:nth-child(1)'));
    }

    item.click();
    return item;
  }

});

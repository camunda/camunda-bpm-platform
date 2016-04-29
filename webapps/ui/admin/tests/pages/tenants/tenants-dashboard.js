'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/tenants',

  newTenantButton: function() {
    return element(by.css('[ng-show="availableOperations.create"] > a'));
  },

  tenantList: function() {
    return element.all(by.repeater('tenant in tenantList'));
  },

  tenantId: function(idx) {
    return this.tenantList().get(idx).element(by.css('.tenant-id > a'));
  },

  tenantName: function(idx) {
    return this.tenantList().get(idx).element(by.binding('{{ tenant.name }}'));
  },

  selectTenantByEditLink: function(idx) {
    return this.tenantList().get(idx).element(by.linkText('Edit')).click();
  },

  selectTenantByNameLink: function(idx) {
    return this.tenantId(idx).click();
  }

});

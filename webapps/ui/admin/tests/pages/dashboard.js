'use strict';

var Page = require('./base');
var AuthenticationPage = require('../../../common/tests/pages/authentication');

var DashboardPage = Page.extend({
  url: '/camunda/app/admin/default/#/',

  section: function (name) {
    return element(by.css('[data-plugin-id="' + name + '"]'));
  },

  sectionLink: function (name, linkText) {
    return this.section(name).element(by.cssContainingText('li > a', linkText));
  }
});

module.exports = new DashboardPage();
module.exports.authentication = new AuthenticationPage();

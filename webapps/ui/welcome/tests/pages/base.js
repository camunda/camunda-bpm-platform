'use strict';

var Page = require('../../../common/tests/pages/page');

module.exports = Page.extend({
  navbar: function () {
    return element(by.css('[cam-widget-header]'));
  },

  navbarItems: function () {
    return this.navbar().all(by.css('[ng-transclude] > ul > li'));
  },

  navbarItem: function (idx) {
    return this.navbarItems().get(idx);
  },

  navbarItemClick: function () {
    return this.navbarItem().element(by.css('a')).click();
  }
});

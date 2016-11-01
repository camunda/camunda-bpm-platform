'use strict';

var Page = require('../../../common/tests/pages/page');

module.exports = Page.extend({

  suspendedBadge: function() {
    return element(by.css('.ctn-header .badge'));
  },

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
  },

  navbarDropDown: function () {
    return this.navbar().all(by.css('[ng-transclude] > ul > li.dropdown'));
  },

  navbarDropDownItems: function () {
    return this.navbarDropDown().all(by.css('.dropdown-menu > li'));
  },

  navbarDropDownItem: function (idx) {
    return this.navbarDropDownItems().get(idx);
  },

  navbarDropDownItemClick: function (idx) {
    this.navbarDropDown().click();
    return this.navbarDropDownItem(idx).click();
  },

  goToSection: function (name) {
    return this.navbar().element(by.cssContainingText('[ng-transclude] > ul > li a', name)).click();
  }
});

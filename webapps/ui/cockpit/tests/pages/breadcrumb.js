'use strict';

var Base = require('./base');

module.exports = Base.extend({

  crumb: function(index) {
    return element(by.css('.cam-breadcrumb [data-index="' + index + '"] a.text'));
  },

  selectCrumb: function(index) {
    this.breadcrumb(index).click();
  },

  activeCrumb: function () {
    return element(by.css('.cam-breadcrumb li.active > .text'));
  },

  activeCrumbViewSwitcher: function() {
    return element(by.css('.cam-breadcrumb li.active .switcher'));
  },

  activeCrumbViewSwitcherCurrent: function() {
    return element(by.css('.cam-breadcrumb li.active .switcher .current'));
  },

  activeCrumbViewSwitcherLink: function() {
    return element(by.css('.cam-breadcrumb li.active .switcher a'));
  },

  activeCrumbDropdown: function () {
    return element(by.css('.cam-breadcrumb li.active > .dropdown'));
  },

  activeCrumbDropdownLabel: function () {
    return element(by.css('.cam-breadcrumb li.active .dropdown-toggle'));
  },

  activeCrumbDropdownOpen: function () {
    return this.activeCrumbDropdownLabel().click();
  },

  activeCrumbDropdownSelect: function (what) {
    var self = this;
    return self.activeCrumbDropdownOpen().then(function () {
      self.activeCrumbDropdown()
        .element(by.cssContainingText('.dropdown-menu > li > a', what))
          .click();
    });
  }
});

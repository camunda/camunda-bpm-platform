'use strict';

var Page = require('./../base');

var groupsSection = element(by.id('groups'));

module.exports = Page.extend({

   selectAuthorizationNavbarItem: function(navbarItem) {
    var index = [
      'Application',
      'Authorization',
      'Group',
      'Group Membership',
      'User'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 2;

    if (itemIndex)
      item = groupsSection.element(by.css('.sidebar-nav ul li:nth-child(' + itemIndex + ')'));
    else
      item = groupsSection.element(by.css('.sidebar-nav ul li:nth-child(1)'));

    item.click();
    return item;
  },

  boxHeader: function() {
    return groupsSection.element(by.css('[ng-controller="AuthorizationCreateController"] legend')).getText();
  },

  createNewButton: function() {
    return groupsSection.element(by.css('[ng-click="toggleCreateNewForm()"]'));
  }

});
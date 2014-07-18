'use strict';

var Page = require('./../base');

module.exports = Page.extend({

  /**
  Select Profile in users side navbar
  @memberof cam.test.e2e.admin.pages.editUser

  @param {string} navbarItem
  @return {!webdriver.promise.Promise}  - A promise of the selected element
  */
  selectUserNavbarItem: function(navbarItem) {
    var index = [
      'Profile',
      'Account',
      'Groups'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (itemIndex)
      item = element(by.css('.sidebar-nav ul li:nth-child(' + itemIndex + ')'));
    else
      item = element(by.css('.sidebar-nav ul li:nth-child(1)'));

    item.click();
    return item;
  }

});

'use strict';

var Base = require('./../../base');

module.exports = Base.extend({
  dialog: function () {
    return element(by.css('.identity-link-modal .modal-content'));
  },



  header: function () {
    return this.dialog().element(by.css('.modal-header'));
  },

  title: function() {
    return this.header().element(by.css('.modal-title')).getText();
  },



  body: function () {
    return this.dialog().element(by.css('.modal-body'));
  },

  elements: function() {
    return this.body().all(by.repeater('(delta, identityLink) in identityLinks'));
  },

  elementName: function(idx) {
    return this.elements().get(idx).element(by.css('.id')).getText();
  },

  nameInput: function () {
    return this.body().element(by.css('[ng-model="newItem"]'));
  },

  addNameButton: function() {
    return this.body().element(by.css('[ng-click="addItem()"]'))
  },

  clickAddNameButton: function() {
    return this.addNameButton().click();
  },

  deleteNameButton: function(name) {
    var self = this;
    return this.getNameIndex(name).then(function (idx) {
      return self.elements().get(idx).element(by.css('.action-button'));
    });
  },

  clickDeleteNameButton: function(name) {
    return this.deleteNameButton(name).then(function(elem) {
      return elem.click();
    });
  },

  getNameIndex: function(name) {
    return this.findElementIndexInRepeater('(delta, identityLink) in identityLinks', by.css('.id'), name).then(function(idx) {
      return idx;
    });
  },



  footer: function () {
    return this.dialog().element(by.css('.modal-footer'));
  },

  closeButton: function () {
    return this.footer().element(by.cssContainingText('.btn', 'Close'));
  },

  clickCloseButton: function () {
    return this.closeButton().click();
  }

});

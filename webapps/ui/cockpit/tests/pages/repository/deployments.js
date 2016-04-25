'use strict';

var Page = require('./repository-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('section.deployments'));
  },


  // deployments ///////////////////////////////////////////

  deploymentList: function() {
    return this.formElement().all(by.repeater('(delta, deployment) in deployments'));
  },

  deploymentName: function(idx) {
    return this.deploymentList().get(idx).element(by.css('.name')).getText();
  },

  deploymentNameElement: function(idx) {
    return this.deploymentList().get(idx).element(by.binding('deployment.name'));
  },

  deploymentSource: function(idx) {
    return this.deploymentList().get(idx).element(by.css('.source')).getText();
  },

  deploymentTenantId: function(idx) {
    return this.deploymentList().get(idx).element(by.css('.tenant-id')).getText();
  },

  selectDeployment: function(idxOrName) {
    var that = this;
    function callPageObject(idx) {
      that.deploymentList().get(idx).click();
      that.waitForElementToBeVisible(element(by.css('[cam-resources] .resources')));
    }

    if (typeof idxOrName === 'number') {
      callPageObject(idxOrName);
    } else {
      this.getDeploymentIndex(idxOrName).then(callPageObject);
    }
  },

  getDeploymentIndex: function(deploymentName) {
    return this.findElementIndexInRepeater('(delta, deployment) in deployments', by.css('.deployment .name'), deploymentName).then(function(idx) {
      return idx;
    });
  },

  deploymentStatus: function(idx) {
    return this.deploymentList().get(idx).getAttribute('class');
  },

  isDeploymentSelected: function(idx) {
    return this.deploymentStatus(idx).then(function(matcher) {
      if (matcher.indexOf('active') !== -1) {
        return true;
      }
      return false;
    });
  },

  focusDeployment: function(idx) {
    return browser.actions().mouseMove(this.deploymentList().get(idx)).perform();
  },

  // sorting //////////////////////////////////////////////

  sortingElement: function() {
    return this.formElement().element(by.css('[cam-deployments-sorting-choices]'));
  },

  sortingBy: function() {
    return this.sortingElement().element(by.css('.sort-by')).getText();
  },

  changeSortingBy: function(sortBy) {
    var self = this;
    this.sortingElement().element(by.css('.dropdown')).click().then(function() {
      self.sortingElement().element(by.cssContainingText('.sort-by-choice', sortBy)).click();
    });
  },

  changeSortingDirection: function() {
    this.sortingElement().element(by.css('[ng-click="changeOrder()"]')).click();
  },

  sortingDirection: function() {
    return this.sortingElement().element(by.css('.sort-direction')).getAttribute('class');
  },

  isSortingDescending: function() {
    return this.sortingDirection().then(function(matcher) {
      if (matcher.indexOf('-down') !== -1) {
        return true;
      }
      return false;
    });
   },

   isSortingAscending: function() {
    return this.sortingDirection().then(function(matcher) {
      if (matcher.indexOf('-up') !== -1) {
        return true;
      }
      return false;
    });
   },


   // search ////////////////////////////////////////////////

  searchElement: function() {
    return this.formElement().element(by.css('[cam-widget-search]'));
  },

  searchList: function() {
    return this.searchElement().all(by.repeater('search in searches'));
  },

  searchInputField: function() {
    return this.searchElement().element(by.css('.main-field'));
  },

  searchTypeDropdown: function(type) {
    return this.searchElement().element(by.cssContainingText('ul > li', type));
  },

  createSearch: function(type, operator, value, isDateValue) {
    var arity = arguments.length;
    if (arity <= 3) {
      operator = false;
      value = arguments[1];
      isDateValue = arguments[2];
    }
    this.searchElement().element(by.css('.main-field')).click();
    this.searchTypeDropdown(type).click();

    if(value) {
      if (isDateValue) {
        element(by.css('.cam-widget-inline-field > button[ng-click="changeType()"]')).click();
      }
      this.searchList().last().element(by.model('editValue')).sendKeys(value, protractor.Key.ENTER);
    }

    if (operator) {
      this.searchList().last().element(by.css('[value="operator.value"]')).click();
      this.searchList().last().element(by.cssContainingText('[value="operator.value"] .dropdown-menu li', operator)).click();
    }
  },

  deleteSearch: function(index) {
    this.searchList().get(index).element(by.css('.remove-search')).click();
  },

  getType: function(index) {
    return this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="type.value"]')).getText();
  },

  changeType: function(index, type) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="type.value"]')).click();
    this.searchList().get(index).element(by.cssContainingText('ul > li', type)).click();
  },

  getOperator: function(index) {
    return this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="operator.value"]')).getText();
  },

  changeOperator: function(index, operator) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="operator.value"]')).click();
    this.searchList().get(index).element(by.cssContainingText('ul > li', operator)).click();
  },

  getValue: function(index) {
    return this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="value.value"]')).getText();
  },

  changeValue: function(index, value, isDateValue) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="value.value"]')).click();

    if (isDateValue) {
      if (isDateValue) {
        element(by.css('.cam-widget-inline-field > button[ng-click="changeType()"]')).click();
      }
    }

    this.searchList().get(index).element(by.model('editValue')).sendKeys(value, protractor.Key.ENTER);
  },


  // delete deployment modal ///////////////////////////////////

  openDeleteDeployment: function(idx) {
    var self = this;

    this.focusDeployment(idx).then(function() {
      var elem = self.deploymentList().get(idx).element(by.css('[ng-click="deleteDeployment($event, deployment)"]'));
      elem.click();
      self.waitForElementToBeVisible(elem, 5000);
    });
  },

  modalHeader: function() {
    return element(by.css('.modal-header'));
  },

  modalTitle: function() {
    return this.modalHeader().element(by.css('.modal-title')).getText();
  },

  modalContent: function() {
    return element(by.css('.modal-body'));
  },

  modalFooter: function() {
    return element(by.css('.modal-footer'));
  },

  closeButton: function() {
    return this.modalFooter().element(by.css('[ng-click="$dismiss()"]'));
  },

  closeModal: function() {
    var theElement = this.modalContent();
    this.closeButton().click();
    this.waitForElementToBeNotPresent(theElement, 5000);
  },

  deleteButton: function() {
    return this.modalFooter().element(by.css('[ng-click="deleteDeployment()"]'));
  },

  deleteDeployment: function() {
    var theElement = this.modalContent();
    this.deleteButton().click();
    this.waitForElementToBeNotPresent(theElement, 5000);
  },

  infobox: function() {
    return this.modalContent().element(by.css('.alert.alert-info'));
  },

  cascadeCheckbox: function () {
    return this.modalContent().element(by.css('[name="cascade"'));
  },

  skipCustomListenersCheckbox: function () {
    return this.modalContent().element(by.css('[name="skipCustomListeners"]'));
  }

});

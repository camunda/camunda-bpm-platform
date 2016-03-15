'use strict';

var Base = require('./../../base');

module.exports = Base.extend({

  formElement: function() {
    return element(by.css('.modal-content'));
  },

  formHeader: function() {
    return this.formElement()
      .element(by.css('.modal-title')).getText();
  },

  notificationList: function() {
    return this.formElement()
      .all(by.repeater('notification in notifications'));
  },

  notificationStatus: function(idx) {
    return this.notificationList().get(idx)
     .element(by.css('.status')).getText();
  },

  notificationMessage: function(idx) {
    return this.notificationList().get(idx)
     .element(by.css('.message')).getText();
  },

  selectPanelByKey: function (key) {
    var selecta = 'accordion [is-open="accordion.' + key + '"]';
    var btnSelecta = selecta + ' [ng-click="toggleOpen()"]';
    var theElement = element(by.css('.panel-collapse.in'));
    var EC = protractor.ExpectedConditions;
    var isPresent = EC.visibilityOf(theElement);

    element(by.css(btnSelecta)).click();
    browser.wait(isPresent, 5000);
  },

  isPanelOpen: function(key) {
    var selecta = 'accordion [is-open="accordion.' + key + '"]';
    var bdySelecta = selecta + ' .panel-body';
    return element(by.css(bdySelecta)).isDisplayed();
  },

  // general
  nameInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  colorPicker: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.properties.color'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  descriptionInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.properties.description'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  priorityInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.properties.priority'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  autoRefreshCheckbox: function() {
    return element(by.model('filter.properties.refresh'));
  },

  // permissions
  permissionPageElement: function() {
    return element(by.css('[cam-tasklist-filter-modal-form-permission]'));
  },

  permissionHelpText: function() {
    return element(by.css('[is-open="accordion.permission"] .task-filter-hint.text-help')).getText();
  },

  accessibleByAllUsersCheckBox: function() {
    return this.permissionPageElement()
      .element(by.model('isGlobalReadAuthorization'));
  },

  newPermissionPageElement: function() {
    return this.permissionPageElement()
      .element(by.css('.new-permission'));
  },

  permissionList: function() {
    return this.permissionPageElement()
      .all(by.repeater('auth in getReadAuthorizations(authorizations)'));
  },

  addPermissionButton: function() {
    return element(by.css('[ng-click="addReadPermission()"]'));
  },

  addPermission: function(type, id) {
    var that = this;

    this.addPermissionButton().click().then(function() {
      that.selectPermissionType(type);
      that.permissionIdInput(id);
    });
  },

  removePermissionButton: function(idx) {
    return this.permissionList().get(idx)
      .element(by.css('[ng-click="removeReadPermission(auth)"]'));
  },

  removePermission: function(idx) {
    this.removePermissionButton(idx).click();
  },

  permissionTypeButton: function() {
    return this.newPermissionPageElement()
      .element(by.css('[ng-click="switchType()"]'));
  },

  selectPermissionType: function(permissionType) {
    var that = this;

    this.getPermissionType().then(function(currentType) {
      if (currentType !== permissionType )  {
        that.permissionTypeButton().click();
      }
    });
  },

  getPermissionType: function(idx) {
    if (arguments.length === 1) {
      return this.permissionList().get(idx)
        .element(by.css('.fake-button .glyphicon'))
        .getAttribute('class').then(function(classes) {
          if (classes.indexOf('glyphicon-user') !== -1) {
            return 'user';
          } else {
            return 'group';
          }
        });
    } else {
      return this.permissionTypeButton()
        .getAttribute('tooltip').then(function(tooltips) {
          if (tooltips.indexOf('user') !== -1) {
            return 'user';
          } else {
            return 'group';
          }
        });
    }
  },

  permissionIdInput: function(inputValue) {
    var inputField = this.newPermissionPageElement()
                      .element(by.model('newPermission.id'));

    if (arguments.length === 1)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  getPermissionId: function(idx) {
    if (arguments.length === 1) {
      return this.permissionList().get(idx)
        .element(by.css('.form-control-static')).getText();
    } else {
      return this.permissionIdInput()
        .getAttribute('value');
    }
  },

  permissionIdHelpText: function() {
    return this.newPermissionPageElement()
      .element(by.css('.help-block:not(.ng-hide)')).getText();
  },

  // criteria
  criteriaPageElement: function() {
    return element(by.css('[is-open="accordion.criteria"]'));
  },

  criteriaHelpText: function() {
    return this.criteriaPageElement().element(by.css('.task-filter-hint.text-help')).getText();
  },

  addCriterionButton: function() {
    return this.criteriaPageElement().element(by.css('[ng-click="addCriterion()"]'));
  },

  removeCriterionButton: function(idx) {
    return this.criterionList().get(idx).element(by.css('[ng-click="removeCriterion(delta)"]'));
  },

  criterionList: function() {
    return this.criteriaPageElement().all(by.repeater('(delta, queryParam) in query'));
  },

  selectCriterionKey: function(item, group, key) {
    this.criterionList().get(item).element(by.cssContainingText('optgroup[label="' + group + '"] > option', key)).click();
  },

  criterionKeyInput: function(idx, inputKey) {
    var inputField = this.criterionList().get(idx).element(by.model('queryParam.key'));

    if (arguments.length === 2)
      inputField.sendKeys(inputKey);

    return inputField;
  },

  criterionKeyHelpText: function(idx) {
    return this.criterionList().get(idx)
      .element(by.css('.help-block:not(.ng-hide)')).getText();
  },

  criterionValueInput: function(idx, inputValue) {
    var inputField = this.criterionList().get(idx).element(by.model('queryParam.value'));

    if (arguments.length === 2)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  includeAssignedTasksCheckbox: function () {
    return this.criteriaPageElement().element(by.css('[ng-model="filter.includeAssignedTasks"]'));
  },
  
  addCriterion: function(group, key, value) {
    var self = this;

    this.addCriterionButton().click().then(function() {
      self.criterionList().count().then(function(items) {
        items = items -1;
        self.selectCriterionKey(items, group, key);
        
        if(value) {
          self.criterionValueInput(items, value);
        }
      });
    });
  },

  editCriterion: function(idx, group, key, value) {
    this.selectCriterionKey(idx, group, key);
    this.criterionValueInput(idx).clear();
    this.criterionValueInput(idx, value);
  },

  // variables
  variablePageElement: function() {
    return element(by.css('[cam-tasklist-filter-modal-form-variable]'));
  },

  variableHelpText: function() {
    return element(by.css('[is-open="accordion.variable"]'))
      .element(by.css('.task-filter-hint.text-help')).getText();
  },

  showUndefinedVariablesCheckBox: function() {
    return this.variablePageElement()
      .element(by.model('filter.properties.showUndefinedVariable'));
  },

  addVariableButton: function() {
    return this.variablePageElement()
      .element(by.css('[ng-click="addVariable()"]'));
  },

  removeVariableButton: function(idx) {
    return this.variableList().get(idx)
      .element(by.css('[ng-click="removeVariable(delta)"]'));
  },

  variableList: function() {
    return this.variablePageElement()
      .all(by.repeater('(delta, variable) in variables'));
  },

  variableNameInput: function(idx, inputValue) {
    var inputField = this.variableList().get(idx)
                      .element(by.model('variable.name'));

    if (arguments.length === 2)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  variableLabelInput: function(idx, inputValue) {
    var inputField = this.variableList().get(idx)
                      .element(by.model('variable.label'));

    if (arguments.length === 2)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  addVariable: function(name, label) {
    var self = this;

    this.addVariableButton().click().then(function() {
      self.variableList().count().then(function(items) {
        items = items -1;
        self.variableNameInput(items, name);
        self.variableLabelInput(items, label);
      });
    });
  }

});

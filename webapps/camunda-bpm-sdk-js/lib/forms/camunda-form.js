/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
/* global CamSDK, require, localStorage: false */

/**
 * For all API client related
 * @namespace CamSDK.form
 */

var moment = require('moment');

var $ = require('./dom-lib');

var VariableManager = require('./variable-manager');

var InputFieldHandler = require('./controls/input-field-handler');

var ChoicesFieldHandler = require('./controls/choices-field-handler');

var FileDownloadHandler = require('./controls/file-download-handler');

var ErrorButtonHandler = require('./controls/error-button-handler');

var EscalationButtonHandler = require('./controls/escalation-button-handler');

var BaseClass = require('./../base-class');

var constants = require('./constants');

var Events = require('./../events');

function extend(dest, add) {
  for (var key in add) {
    dest[key] = add[key];
  }
  return dest;
}

/**
 * A class to help handling embedded forms
 *
 * @class
 * @memberof CamSDk.form
 *
 * @param {Object.<String,*>} options
 * @param {Cam}               options.client
 * @param {String}            [options.taskId]
 * @param {String}            [options.processDefinitionId]
 * @param {String}            [options.processDefinitionKey]
 * @param {Element}           [options.formContainer]
 * @param {Element}           [options.formElement]
 * @param {Object}            [options.urlParams]
 * @param {String}            [options.formUrl]
 */
function CamundaForm(options) {
  if (!options) {
    throw new Error('CamundaForm need to be initialized with options.');
  }

  var done = (options.done =
    options.done ||
    function(err) {
      if (err) throw err;
    });

  if (options.client) {
    this.client = options.client;
  } else {
    this.client = new CamSDK.Client(options.clientConfig || {});
  }

  if (
    !options.taskId &&
    !options.processDefinitionId &&
    !options.processDefinitionKey
  ) {
    return done(
      new Error(
        "Cannot initialize Taskform: either 'taskId' or 'processDefinitionId' or 'processDefinitionKey' must be provided"
      )
    );
  }

  this.taskId = options.taskId;
  if (this.taskId) {
    this.taskBasePath = this.client.baseUrl + '/task/' + this.taskId;
  }
  this.processDefinitionId = options.processDefinitionId;
  this.processDefinitionKey = options.processDefinitionKey;

  this.formElement = options.formElement;
  this.containerElement = options.containerElement;
  this.formUrl = options.formUrl;

  if (!this.formElement && !this.containerElement) {
    return done(
      new Error(
        "CamundaForm needs to be initilized with either 'formElement' or 'containerElement'"
      )
    );
  }

  if (!this.formElement && !this.formUrl) {
    return done(
      new Error(
        "Camunda form needs to be intialized with either 'formElement' or 'formUrl'"
      )
    );
  }

  /**
   * A VariableManager instance
   * @type {VariableManager}
   */
  this.variableManager = new VariableManager({
    client: this.client
  });

  /**
   * An array of FormFieldHandlers
   * @type {FormFieldHandlers[]}
   */
  this.formFieldHandlers = options.formFieldHandlers || [
    InputFieldHandler,
    ChoicesFieldHandler,
    FileDownloadHandler,
    ErrorButtonHandler,
    EscalationButtonHandler
  ];

  this.businessKey = null;

  this.fields = [];

  this.scripts = [];

  this.options = options;

  // init event support
  Events.attach(this);

  this.initialize(done);
}

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.initializeHandler = function(FieldHandler) {
  var self = this;
  var selector = FieldHandler.selector;

  $(selector, self.formElement).each(function() {
    self.fields.push(new FieldHandler(this, self.variableManager, self));
  });
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.initialize = function(done) {
  done =
    done ||
    function(err) {
      if (err) throw err;
    };
  var self = this;

  // check whether form needs to be loaded first
  if (this.formUrl) {
    this.client.http.load(this.formUrl, {
      accept: '*/*',
      done: function(err, result) {
        if (err) {
          return done(err);
        }

        try {
          self.renderForm(result);
          self.initializeForm(done);
        } catch (error) {
          done(error);
        }
      },
      data: extend({noCache: Date.now()}, this.options.urlParams || {})
    });
  } else {
    try {
      this.initializeForm(done);
    } catch (error) {
      done(error);
    }
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.renderForm = function(formHtmlSource) {
  // apppend the form html to the container element,
  // we also wrap the formHtmlSource to limit the risks of breaking
  // the structure of the document
  $(this.containerElement)
    .html('')
    .append('<div class="injected-form-wrapper">' + formHtmlSource + '</div>');

  // extract and validate form element
  var formElement = (this.formElement = $('form', this.containerElement));
  if (formElement.length !== 1) {
    throw new Error('Form must provide exaclty one element <form ..>');
  }
  if (!formElement.attr('name')) {
    formElement.attr('name', '$$camForm');
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.initializeForm = function(done) {
  var self = this;

  // handle form scripts
  this.initializeFormScripts();

  // initialize field handlers
  this.initializeFieldHandlers();

  // execute the scripts
  this.executeFormScripts();

  // fire form loaded
  this.fireEvent('form-loaded');

  this.fetchVariables(function(err, result) {
    if (err) {
      throw err;
    }

    // merge the variables
    self.mergeVariables(result);

    // retain original server values for dirty checking
    self.storeOriginalValues(result);

    // fire variables fetched
    self.fireEvent('variables-fetched');

    // restore variables from local storage
    self.restore();

    // fire variables-restored
    self.fireEvent('variables-restored');

    // apply the variables to the form fields
    self.applyVariables();

    // fire variables applied
    self.fireEvent('variables-applied');

    // invoke callback
    done(null, self);
  });
};

CamundaForm.prototype.initializeFieldHandlers = function() {
  for (var FieldHandler in this.formFieldHandlers) {
    this.initializeHandler(this.formFieldHandlers[FieldHandler]);
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.initializeFormScripts = function() {
  var formScriptElements = $(
    'script[' + constants.DIRECTIVE_CAM_SCRIPT + ']',
    this.formElement
  );
  for (var i = 0; i < formScriptElements.length; i++) {
    this.scripts.push(formScriptElements[i].text);
  }
};

CamundaForm.prototype.executeFormScripts = function() {
  for (var i = 0; i < this.scripts.length; i++) {
    this.executeFormScript(this.scripts[i]);
  }
};

CamundaForm.prototype.executeFormScript = function(script) {
  /*eslint-disable */
  /* jshint unused: false */
  (function(camForm) {

    /* jshint evil: true */
    eval(script);
    /* jshint evil: false */

  })(this);
  /*eslint-enable */
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 *
 * Store the state of the form to localStorage.
 *
 * You can prevent further execution by hooking
 * the `store` event and set `storePrevented` to
 * something truthy.
 */
CamundaForm.prototype.store = function(callback) {
  var formId = this.taskId || this.processDefinitionId || this.caseInstanceId;

  if (!formId) {
    if (typeof callback === 'function') {
      return callback(new Error('Cannot determine the storage ID'));
    } else {
      throw new Error('Cannot determine the storage ID');
    }
  }

  this.storePrevented = false;
  this.fireEvent('store');
  if (this.storePrevented) {
    return;
  }

  try {
    // get values from form fields
    this.retrieveVariables();

    // build the local storage object
    var store = {date: Date.now(), vars: {}};
    for (var name in this.variableManager.variables) {
      if (this.variableManager.variables[name].type !== 'Bytes') {
        store.vars[name] = this.variableManager.variables[name].value;
      }
    }

    // store it
    localStorage.setItem('camForm:' + formId, JSON.stringify(store));
  } catch (error) {
    if (typeof callback === 'function') {
      return callback(error);
    } else {
      throw error;
    }
  }
  this.fireEvent('variables-stored');
  if (typeof callback === 'function') {
    callback();
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 * @return {Boolean} `true` if there is something who can be restored
 */
CamundaForm.prototype.isRestorable = function() {
  var formId = this.taskId || this.processDefinitionId || this.caseInstanceId;

  if (!formId) {
    throw new Error('Cannot determine the storage ID');
  }

  // verify the presence of an entry
  if (!localStorage.getItem('camForm:' + formId)) {
    return false;
  }

  // unserialize
  var stored = localStorage.getItem('camForm:' + formId);
  try {
    stored = JSON.parse(stored);
  } catch (error) {
    return false;
  }

  // check the content
  if (!stored || !Object.keys(stored).length) {
    return false;
  }

  return true;
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 *
 * Restore the state of the form from localStorage.
 *
 * You can prevent further execution by hooking
 * the `restore` event and set `restorePrevented` to
 * something truthy.
 */
CamundaForm.prototype.restore = function(callback) {
  var stored;
  var vars = this.variableManager.variables;
  var formId = this.taskId || this.processDefinitionId || this.caseDefinitionId;

  if (!formId) {
    if (typeof callback === 'function') {
      return callback(new Error('Cannot determine the storage ID'));
    } else {
      throw new Error('Cannot determine the storage ID');
    }
  }

  // no need to go further if there is nothing to restore
  if (!this.isRestorable()) {
    if (typeof callback === 'function') {
      return callback();
    }
    return;
  }

  try {
    // retrieve the values from localStoarge
    stored = localStorage.getItem('camForm:' + formId);
    stored = JSON.parse(stored).vars;
  } catch (error) {
    if (typeof callback === 'function') {
      return callback(error);
    } else {
      throw error;
    }
  }

  // merge the stored values on the variableManager.variables
  for (var name in stored) {
    if (vars[name]) {
      vars[name].value = stored[name];
    } else {
      vars[name] = {
        name: name,
        value: stored[name]
      };
    }
  }

  if (typeof callback === 'function') {
    callback();
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.submit = function(callback) {
  var formId = this.taskId || this.processDefinitionId;

  // fire submit event (event handler may prevent submit from being performed)
  this.submitPrevented = false;
  this.fireEvent('submit');
  if (this.submitPrevented) {
    var err = new Error('camForm submission prevented');
    this.fireEvent('submit-failed', err);
    return callback && callback(err);
  }

  try {
    // get values from form fields
    this.retrieveVariables();
  } catch (error) {
    return callback && callback(error);
  }

  var self = this;
  this.transformFiles(function() {
    // submit the form variables
    self.submitVariables(function(err, result) {
      if (err) {
        self.fireEvent('submit-failed', err);
        return callback && callback(err);
      }

      // clear the local storage for this form
      localStorage.removeItem('camForm:' + formId);

      self.fireEvent('submit-success');
      return callback && callback(null, result);
    });
  });
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.error = function(errorCode, errorMessage, callback) {
  var formId = this.taskId || this.processDefinitionId;

  this.errorPrevented = false;
  this.fireEvent('error');
  if (this.errorPrevented) {
    var err = new Error('camForm error prevented');
    this.fireEvent('error-failed', err);
    return callback && callback(err);
  }

  try {
    // get values from form fields
    this.retrieveVariables();
  } catch (error) {
    return callback && callback(error);
  }

  var self = this;
  this.transformFiles(function() {
    // submit the form variables
    var data = {
      variables: self.parseVariables(),
      id: self.taskId,
      errorCode: errorCode,
      errorMessage: errorMessage
    };
    self.client.resource('task').bpmnError(data, function(err, result) {
      if (err) {
        self.fireEvent('error-failed', err);
        return callback && callback(err);
      }

      // clear the local storage for this form
      localStorage.removeItem('camForm:' + formId);

      self.fireEvent('error-success');
      return callback && callback(null, result);
    });
  });
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.escalate = function(escalationCode, callback) {
  var formId = this.taskId || this.processDefinitionId;

  this.escalationPrevented = false;
  this.fireEvent('escalation');
  if (this.escalationPrevented) {
    var err = new Error('camForm escalation prevented');
    this.fireEvent('escalation-failed', err);
    return callback && callback(err);
  }

  try {
    // get values from form fields
    this.retrieveVariables();
  } catch (error) {
    return callback && callback(error);
  }

  var self = this;
  this.transformFiles(function() {
    // submit the form variables
    var data = {
      variables: self.parseVariables(),
      id: self.taskId,
      escalationCode: escalationCode
    };
    self.client.resource('task').bpmnEscalation(data, function(err, result) {
      if (err) {
        self.fireEvent('escalation-failed', err);
        return callback && callback(err);
      }

      // clear the local storage for this form
      localStorage.removeItem('camForm:' + formId);

      self.fireEvent('escalation-success');
      return callback && callback(null, result);
    });
  });
};

CamundaForm.prototype.transformFiles = function(callback) {
  var that = this;
  var counter = 1;

  var callCallback = function() {
    if (--counter === 0) {
      callback();
    }
  };

  var bytesToSize = function(bytes) {
    if (bytes === 0) return '0 Byte';
    var k = 1000;
    var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    var i = Math.floor(Math.log(bytes) / Math.log(k));
    return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
  };

  for (var i in this.fields) {
    var element = this.fields[i].element[0];
    if (element.getAttribute('type') === 'file') {
      var fileVar = that.variableManager.variables[that.fields[i].variableName];

      if (typeof FileReader === 'function' && element.files.length > 0) {
        if (
          element.files[0].size >
          (parseInt(element.getAttribute('cam-max-filesize'), 10) || 5000000)
        ) {
          throw new Error(
            'Maximum file size of ' +
              bytesToSize(
                parseInt(element.getAttribute('cam-max-filesize'), 10) ||
                  5000000
              ) +
              ' exceeded.'
          );
        }
        var reader = new FileReader();
        /* jshint ignore:start */
        reader.onloadend = (function(i, element, fileVar) {
          return function(e) {
            var binary = '';
            var bytes = new Uint8Array(e.target.result);
            var len = bytes.byteLength;
            for (var j = 0; j < len; j++) {
              binary += String.fromCharCode(bytes[j]);
            }

            fileVar.value = btoa(binary);

            // set file metadata as value info
            if (fileVar.type.toLowerCase() === 'file') {
              fileVar.valueInfo = {
                filename: element.files[0].name,
                mimeType: element.files[0].type
              };
            }

            callCallback();
          };
        })(i, element, fileVar);
        /* jshint ignore:end */
        reader.readAsArrayBuffer(element.files[0]);
        counter++;
      } else {
        fileVar.value = '';
        fileVar.valueInfo = {
          filename: ''
        };
      }
    }
  }

  callCallback();
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.fetchVariables = function(done) {
  done = done || function() {};
  var names = this.variableManager.variableNames();
  if (names.length) {
    var data = {
      names: names,
      deserializeValues: false
    };

    // pass either the taskId, processDefinitionId or processDefinitionKey
    if (this.taskId) {
      data.id = this.taskId;
      this.client.resource('task').formVariables(data, done);
    } else {
      data.id = this.processDefinitionId;
      data.key = this.processDefinitionKey;
      this.client.resource('process-definition').formVariables(data, done);
    }
  } else {
    done();
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.parseVariables = function() {
  var varManager = this.variableManager;
  var vars = varManager.variables;

  // The default display value is different from the original value in varManager
  this.fields.forEach(function(field) {
    if (vars[field.variableName]) {
      vars[field.variableName].defaultValue = field.originalValue;

      if (
        field.originalValue === '' ||
        typeof field.originalValue === 'undefined'
      ) {
        vars[field.variableName].defaultValue = vars[field.variableName].value;
      }
    }
  });

  var variableData = {};
  for (var v in vars) {
    // only submit dirty variables
    // LIMITATION: dirty checking is not performed for complex object variables

    var val = vars[v].value;

    // We want implicit type conversion in this case, the defaultValue is always a string
    if (varManager.isDirty(v) || vars[v].defaultValue != val) {
      // if variable is JSON, serialize

      if (varManager.isJsonVariable(v)) {
        val = JSON.stringify(val);
      }

      // if variable is Date, add timezone info
      if (val && varManager.isDateVariable(v)) {
        val = moment(val, moment.ISO_8601).format('YYYY-MM-DDTHH:mm:ss.SSSZZ');
      }

      variableData[v] = {
        value: val,
        type: vars[v].type,
        valueInfo: vars[v].valueInfo
      };
    }
  }
  return variableData;
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.submitVariables = function(done) {
  done = done || function() {};

  var data = {variables: this.parseVariables()};

  // pass either the taskId, processDefinitionId or processDefinitionKey
  if (this.taskId) {
    data.id = this.taskId;
    this.client.resource('task').submitForm(data, done);
  } else {
    var businessKey =
      this.businessKey ||
      this.formElement.find('input[type="text"][cam-business-key]').val();
    if (businessKey) {
      data.businessKey = businessKey;
    }
    data.id = this.processDefinitionId;
    data.key = this.processDefinitionKey;
    this.client.resource('process-definition').submitForm(data, done);
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.storeOriginalValues = function(variables) {
  for (var v in variables) {
    this.variableManager.setOriginalValue(v, variables[v].value);
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.mergeVariables = function(variables) {
  var vars = this.variableManager.variables;

  for (var v in variables) {
    if (vars[v]) {
      for (var p in variables[v]) {
        vars[v][p] = vars[v][p] || variables[v][p];
      }
    } else {
      vars[v] = variables[v];
    }
    // check whether the variable provides JSON payload. If true, deserialize
    if (this.variableManager.isJsonVariable(v)) {
      vars[v].value = JSON.parse(variables[v].value);
    }

    // generate content url for file and bytes variables
    var type = vars[v].type;
    if (!!this.taskBasePath && (type === 'Bytes' || type === 'File')) {
      vars[v].contentUrl =
        this.taskBasePath + '/variables/' + vars[v].name + '/data';
    }

    this.variableManager.isVariablesFetched = true;
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.applyVariables = function() {
  for (var i in this.fields) {
    this.fields[i].applyValue();
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.retrieveVariables = function() {
  for (var i in this.fields) {
    this.fields[i].getValue();
  }
};

/**
 * @memberof CamSDK.form.CamundaForm.prototype
 */
CamundaForm.prototype.fireEvent = function(eventName, obj) {
  this.trigger(eventName, obj);
};

/**
 * @memberof CamSDK.form.CamundaForm
 */
CamundaForm.$ = $;

CamundaForm.VariableManager = VariableManager;
CamundaForm.fields = {};
CamundaForm.fields.InputFieldHandler = InputFieldHandler;
CamundaForm.fields.ChoicesFieldHandler = ChoicesFieldHandler;

/**
 * @memberof CamSDK.form.CamundaForm
 */
CamundaForm.cleanLocalStorage = function(timestamp) {
  for (var i = 0; i < localStorage.length; i++) {
    var key = localStorage.key(i);
    if (key.indexOf('camForm:') === 0) {
      var item = JSON.parse(localStorage.getItem(key));
      if (item.date < timestamp) {
        localStorage.removeItem(key);
        i--;
      }
    }
  }
};

/**
 * @memberof CamSDK.form.CamundaForm
 * @borrows CamSDK.BaseClass.extend as extend
 * @name extend
 * @type {Function}
 */
CamundaForm.extend = BaseClass.extend;

module.exports = CamundaForm;

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

var CamSDK = require('../../../lib/index-browser.js');

var request = require('superagent');
var mockConfig = require('../../superagent-mock-config');

describe('The local storage', function() {
  /* global jQuery: false */

  var $ = jQuery;
  var $simpleFormDoc;
  var camClient;
  var testProcessId = '__test__';

  var superagentMock;
  before(function() {
    superagentMock = require('superagent-mock')(request, mockConfig);
  });

  after(function() {
    superagentMock.unset();
  });

  before(function(done) {
    jQuery.ajax('/base/test/karma/forms/form-localstorage.html', {
      success: function(data) {
        $simpleFormDoc = jQuery('<div id="test-form">'+ data +'</div>');
        // the following lines allow to see the form in the browser
        var _$top = $(top.document);
        _$top.find('#test-form').remove();
        _$top.find('#browsers').after($simpleFormDoc);

        camClient = new CamSDK.Client({
          apiUri: 'engine-rest/engine'
        });

        done();
      },

      error: done
    });
  });



  it('stores', function(done) {
    var camForm;

    localStorage.removeItem('camForm:'+ testProcessId);

    function ready(err) {
      if (err) { return done(err); }

      expect(camForm.isRestorable()).not.to.be.ok;

      $('#stringVar', camForm.formElement).val('stringValue');

      camForm.store();

      var storedVars;
      try {
        storedVars = JSON.parse(localStorage.getItem('camForm:'+ testProcessId)).vars;
      }
      catch (e) {
        return done(e);
      }

      expect(storedVars.stringVar).to.eql('stringValue');

      expect(camForm.isRestorable()).to.be.ok;

      done();
    }

    camForm = new CamSDK.Form({
      client: camClient,
      processDefinitionId: testProcessId,
      formElement: $simpleFormDoc.find('form[cam-form]'),
      done: function() {window.setTimeout(ready);}
    });
  });


  it('restores', function(done) {
    var camForm;

    localStorage.setItem('camForm:'+ testProcessId, JSON.stringify({
      vars: {
        stringVar: 'fromStorage'
      }
    }));

    function ready(err) {
      if (err) { return done(err); }

      expect(camForm.isRestorable()).to.be.ok;

      camForm.restore();

      expect(camForm.variableManager.variables.stringVar).to.be.ok;

      camForm.applyVariables();

      expect($('#stringVar', camForm.formElement).val()).to.eql('fromStorage');

      done();
    }

    camForm = new CamSDK.Form({
      client: camClient,
      processDefinitionId: testProcessId,
      formElement: $simpleFormDoc.find('form[cam-form]'),
      done: function() {window.setTimeout(ready);}
    });
  });


  describe('handling on submission', function() {
    it('is kept upon failed response from server', function(done) {
      var camForm;

      function ready(err) {
        if (err) { return done(err); }

        camForm.submitVariables = function(cb) {
          cb(new Error('Murphy inna di place'), {});
        };

        // given a failed form submission
        camForm.submit(function(_err) {
          expect(_err).to.be.ok;

          expect(localStorage.getItem('camForm:'+ testProcessId)).to.be.ok;

          done();
        });
      }

      localStorage.setItem('camForm:'+ testProcessId, JSON.stringify({
        date: 1467107571159,
        vars: {
          stringVar: 'fromStorage'
        }
      }));

      camForm = new CamSDK.Form({
        client: camClient,
        processDefinitionId: testProcessId,
        formElement: $simpleFormDoc.find('form[cam-form]'),
        done: function() {window.setTimeout(ready);}
      });
    });


    it('is wiped upon successful response from server', function(done) {
      var camForm;

      function ready(err) {
        if (err) { return done(err); }

        camForm.submitVariables = function(cb) {
          cb();
        };


        // given a successful form submission
        camForm.submit(function(_err) {
          expect(_err).not.to.be.ok;

          expect(localStorage.getItem('camForm:'+ testProcessId)).not.to.be.ok;

          done();
        });
      }

      localStorage.setItem('camForm:'+ testProcessId, JSON.stringify({
        date: 1467107571159,
        vars: {
          stringVar: 'fromStorage'
        }
      }));

      camForm = new CamSDK.Form({
        client: camClient,
        processDefinitionId: testProcessId,
        formElement: $simpleFormDoc.find('form[cam-form]'),
        done: function() {window.setTimeout(ready);}
      });
    });
  });
});

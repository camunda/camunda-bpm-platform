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

describe('The form', function() {
  /* global jQuery: false */
  var $ = jQuery;
  var $simpleFormDoc;
  var camForm, camClient, procDef;

  var superagentMock;
  before(function() {
    superagentMock = require('superagent-mock')(request, mockConfig);
  });

  after(function() {
    superagentMock.unset();
  });

  before(function(done) {
    jQuery.ajax('/base/test/karma/forms/form-simple.html', {
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


  it('needs a process definition', function(done) {
    camClient.resource('process-definition').list({}, function(err, result) {
      if (err) { return done(err); }

      procDef = result.items.pop();

      expect(procDef.id).to.be.ok;

      done();
    });
  });

  it('gets the process definition with a promise', function() {
    return camClient.resource('process-definition').list({}).then(
      function(result) {
        procDef = result.items.pop();
        expect(procDef.id).to.be.ok;
      }
    );
  });


  it('exists globally', function() {
    expect(CamSDK.Form).to.be.a('function');
  });


  it('has a DOM library', function() {
    expect(CamSDK.Form.$).to.be.ok;
  });


  it('initialize', function(done) {
    expect(camClient).to.be.ok;

    function prepare() {
      camForm = new CamSDK.Form({
        client:               camClient,
        processDefinitionId:  procDef.id,
        formElement:          $simpleFormDoc.find('form[cam-form]'),
        done:                 function() {window.setTimeout(initialized);}
      });
    }

    function initialized() {

      expect(camForm.formFieldHandlers).to.be.an('array');

      expect(camForm.fields).to.be.an('array');

      var $el = $simpleFormDoc.find('input[type="text"]');

      expect($el.length).to.eql(1);

      expect($el.val()).to.be.ok;

      done();
    }

    expect(prepare).not.to.throw();
  });


  it('submits the form', function(done) {

    function formSubmitted(err, result) {
      if (err) { return done(err); }

      expect(result.links).to.be.ok;

      expect(result.definitionId).to.eql(procDef.id);

      var stored = mockConfig.mockedData.processInstanceFormVariables[result.id];
      expect(stored).to.be.ok;

      expect(stored.stringVar).to.be.ok;

      expect(stored.stringVar.type).to.eql('String');

      expect(stored.stringVar.value).to.eql('updated');

      done();
    }

    function formReady(err) {
      if (err) { return done(err); }

      var $el = $simpleFormDoc.find('input[type="text"]');

      expect($el.length).to.eql(1);

      $el.val('updated');

      camForm.submit(formSubmitted);
    }


    camForm = new CamSDK.Form({
      client:               camClient,
      processDefinitionId:  procDef.id,
      formElement:          $simpleFormDoc.find('form[cam-form]'),
      done:                 function() {window.setTimeout(formReady);}
    });
  });


  describe('choices field', function() {
    before(function(done) {
      camForm = new CamSDK.Form({
        client:               camClient,
        processDefinitionId:  procDef.id,
        formElement:          $simpleFormDoc.find('form[cam-form]'),
        done:                 done
      });
    });

    describe('single choice', function() {
      var $select;
      beforeEach(function() {
        $select = $simpleFormDoc.find('select[cam-variable-name]:not([multiple])');
      });


      it('can be `select`', function() {
        expect($select.length).to.eql(1);

        expect(camForm.formFieldHandlers).to.be.an('array');

        expect(camForm.fields).to.be.an('array');
      });
    });


    describe('multiple choices', function() {
      var $select;
      before(function() {
        $select = $simpleFormDoc.find('select[cam-variable-name][multiple]');
      });


      it('can be `select[multiple]`', function() {
        expect($select.length).to.eql(1);

        expect(camForm.formFieldHandlers).to.be.an('array');

        expect(camForm.fields).to.be.an('array');
      });
    });
  });
});

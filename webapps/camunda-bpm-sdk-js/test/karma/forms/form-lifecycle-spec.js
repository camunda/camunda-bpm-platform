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
  var camClient, procDef;

  var superagentMock;
  before(function() {
    superagentMock = require('superagent-mock')(request, mockConfig);
  });

  after(function() {
    superagentMock.unset();
  });

  before(function(done) {
    jQuery.ajax('/base/test/karma/forms/form-lifecycle.html', {
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


  it('prepares the testing environemnt', function() {
    expect(CamSDK).to.be.an('object');

    expect(camClient).to.be.ok;
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


  it('initialize', function(done) {
    function ready(err) {
      if (err) { return done(err); }

      // expect variable created by script to be present
      var customVar = camForm.variableManager.variable('customVar');
      expect(customVar.name).to.eql('customVar');
      expect(customVar.type).to.eql('String');

      // expect form field to be populated
      expect($('#customField', camForm.formElement).val()).to.eql('someValue');


      // given that we do not change the value of the custom field
      camForm.submit();

      // we expect the submit callback to prevent the submit of the form
      expect(camForm.submitPrevented).to.be.ok;

      // if we change the value of the form field
      $('#customField', camForm.formElement).val('updated');

      // and re-attempt submit
      camForm.submit();

      // we expect the submit to pass
      expect(camForm.submitPrevented).to.not.be.ok;

      done();
    }

    var camForm = new CamSDK.Form({
      client: camClient,
      processDefinitionId: procDef.id,
      formElement: $simpleFormDoc.find('form[cam-form]'),
      done: function() {window.setTimeout(ready);}
    });
  });
});

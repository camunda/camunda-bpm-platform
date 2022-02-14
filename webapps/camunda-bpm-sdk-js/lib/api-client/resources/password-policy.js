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

var AbstractClientResource = require('./../abstract-client-resource');

/**
 * Password Policy Resource
 * @class
 * @memberof CamSDK.client.resource
 * @augments CamSDK.client.AbstractClientResource
 */
var PasswordPolicy = AbstractClientResource.extend();

/**
 * Path used by the resource to perform HTTP queries
 * @type {String}
 */
PasswordPolicy.path = 'identity/password-policy';

/**
 * Fetch the active password policy.
 *
 * @param {Function} done
 */
PasswordPolicy.get = function(done) {
  return this.http.get(this.path, {
    done: done
  });
};

/**
 * Validate a password against the password policy
 *
 * @param {Object}   [params]
 * @param {String}   [params.password]  Password to be validated
 * @param {Function} done
 */
PasswordPolicy.validate = function(params, done) {
  if (typeof params === 'string') {
    params = {password: params};
  }
  return this.http.post(this.path, {
    data: params,
    done: done
  });
};

module.exports = PasswordPolicy;

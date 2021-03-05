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

const checker = require('license-checker');

const PRODUCTION_LICENSES = [
    '0BSD',
    'Apache-2.0',
    'BSD-2-Clause',
    'BSD-3-Clause',
    'ISC',
    'MIT',
    'Unlicense'
];


const ALLOWED_PACKAGES = [
    'emitter-component@1.1.1' // uses MIT, but uses wrong licenseFile field
];

const parseResults = (ALLOWED_LICENSES, resolve, reject) =>
    function (err, packages) {
        if (err) {
            throw err;
        } else {
            const entries = Object.entries(packages);
            let licenseWarning = '';

            for (const [id, info] of entries) {
                if (ALLOWED_PACKAGES.includes(id)) {
                    continue;
                }

                let licenses = info.licenses;
                let hasMultipleLicenses = false;

                if (typeof licenses === 'string') {
                    licenses = licenses.replace(/\(|\)|\*/g, '');
                    if (licenses.includes('AND')) {
                        licenses = licenses.split(' AND ');
                        hasMultipleLicenses = true;
                    } else {
                        licenses = licenses.split(' OR ');
                    }
                }

                licenses = typeof licenses === 'object' ? licenses : [licenses];

                let approved = hasMultipleLicenses
                    ? licenses.every(license => ALLOWED_LICENSES.includes(license))
                    : licenses.some(license => ALLOWED_LICENSES.includes(license));

                if (!approved) {
                    licenseWarning += `${id} uses ${licenses.join(' OR/AND ')}\n`;
                }
            }

            if (licenseWarning) {
                reject(licenseWarning);
            } else {
                resolve();
            }
        }
    };


checker.init(
    {
        start: '.',
        excludePrivatePackages: true,
        production: true
    },
    parseResults(PRODUCTION_LICENSES, ()=>console.log("gg"), warn => console.log(warn))
);


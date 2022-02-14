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
const path = require("path");

const PRODUCTION_LICENSES = [
    'Apache-2.0',
    '0BSD',
    'BSD-2-Clause',
    'BSD-3-Clause',
    'ISC',
    'MIT',
    'Unlicense',
];


const ALLOWED_PACKAGES = [
    'emitter-component@1.1.1' // uses MIT, but does not provide license field https://github.com/component/emitter/blob/master/LICENSE
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

/*
Main entry point for license check
 */
if (require.main === module) {
    checker.init(
        {
            start: path.resolve(__dirname, ".."),
            excludePrivatePackages: true,
            production: true
        },
        parseResults(PRODUCTION_LICENSES,
            () => console.log("License check passed"),
            warn => {
                console.warn("License check did not pass");
                console.warn(warn);
                process.exit(1)
            })
    );
}

/*
Main entry point for license book generation
 */
module.exports.writeThirdPartyNotice = function (dependencies){
    const licenseBook = {};
    const licenses = dependencies.dependencies;
    for (const license of licenses) {
        if (license.name === "swagger-ui") {
            // see https://jira.camunda.com/browse/OB-16
            license.licenseText =
                "\n-----------------------------------------------------------\n" +
                `swagger-UI-dist (Licensed under ${PRODUCTION_LICENSES.join(', ')}) \n` +
                "The swagger-ui-dist NPM webjar is comprised of a multitude of minified ECMAscript \n" +
                "and CSS libraries aggregated from several projects, under different licences. \n" +
                "For license information please see swagger-ui-es-bundle.js.LICENSE.txt.\n" +
                "------------------------------------------------------------\n\n" +
                license.licenseText
        }
        license.licenseShort = license.licenseName;
        delete license.licenseName;
        license.outdated = false;
        licenseBook[license.name + '@' + license.version] = license;
    }
    return JSON.stringify(licenseBook, null, 2)
}
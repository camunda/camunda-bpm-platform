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

const devRegex = /<!-- dev -->(((?!\/dev).|\n)*)<!-- \/dev -->/g;
const prodRegex = /<!-- prod(((?!-->).|\n)*)-->/g;
const fs = require("fs");

let content = fs.readFileSync(__dirname + "/../public/index.html", "utf-8");

content = content.replace(devRegex, "<!-- dev$1-->");
content = content.replace(prodRegex, "<!-- prod -->$1<!-- /prod -->");

fs.writeFileSync(__dirname + "/../public/index.html", content, "UTF-8");

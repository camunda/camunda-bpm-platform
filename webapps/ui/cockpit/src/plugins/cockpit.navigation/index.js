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

import navFactory from "utils/cockpitNavigationFactory";

export const processes = navFactory(
  "processes",
  "Processes",
  100,
  /(process|migration)/
);
export const decisions = navFactory("decisions", "Decisions", 90, "decision");
export const tasks = navFactory("tasks", "Human Tasks", 80);
export const repository = navFactory("repository", "Deployments", -5);
export const batch = navFactory("batch", "Batches", -6, /batch(?!\/)/);

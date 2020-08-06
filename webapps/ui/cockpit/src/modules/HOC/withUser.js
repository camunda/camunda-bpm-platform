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

import React, { createContext, useContext, useState, useEffect } from "react";

import { get } from "utils/request";

const UserContext = createContext();

let previousUrl;

window.addEventListener("hashchange", function(event) {
  if (event.newURL.includes("#/login")) {
    previousUrl = event.oldURL;
  }
});

export function UserProvider({ children }) {
  const [user, setUser] = useState(null);

  const refreshUser = async () => {
    try {
      const user = await (await get("%ADMIN_API%/auth/user/%ENGINE%")).json();

      user.profile = await (
        await get(`%ENGINE_API%/user/${user.userId}/profile`)
      ).json();

      if (
        user &&
        ((previousUrl && window.location.href.includes("/dashboard")) || // redirect after login
          window.location.href.includes("/login")) // Navigated to login when logged in
      ) {
        window.location.href = previousUrl || "#/dashboard";
        previousUrl = null;
      }

      setUser(user);
      return user;
    } catch (err) {
      if (err.status === 404) {
        window.location.href = "#/login";
      }
      setUser(null);
    }
  };

  useEffect(() => {
    refreshUser();
  }, []);

  return (
    <UserContext.Provider value={{ user, refreshUser }}>
      {children}
    </UserContext.Provider>
  );
}

export default Component => props => (
  <Component {...useContext(UserContext)} {...props} />
);

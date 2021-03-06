/*
 * @(#) AuthUtil.java Algem Web App 1.5.0 12/10/16
 *
 * Copyright (c) 2015-2016 Musiques Tangentes. All Rights Reserved.
 *
 * This file is part of Algem Web App.
 * Algem Web App is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Algem Web App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Algem Web App. If not, see <http://www.gnu.org/licenses/>.
 */

package net.algem.util;

import net.algem.security.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.5.0
 * @since 1.5.0 12/10/16
 */
public class AuthUtil {

  public static boolean isAdministrativeMember() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      for (GrantedAuthority a : auth.getAuthorities()) {
        if (String.valueOf(Profile.User.getId()).equals(a.toString())
          || String.valueOf(Profile.Admin.getId()).equals(a.toString())) {
          return true;
        }
      }
    }
    return false;
  }

}

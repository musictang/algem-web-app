/*
 * @(#)UserValidator.java	1.0.0 11/02/13
 *
 * Copyright (c) 2013 Musiques Tangentes. All Rights Reserved.
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
 *
 */
package net.algem.security;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * User's validator.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.0
 * @since 1.0.0 11/02/13
 * @deprecated 
 */
public class UserValidator implements Validator {

  @Override
	public boolean supports(Class clazz) {
		return clazz.isAssignableFrom(User.class);
	}

	/**
	 * Validates user's values.
	 *
	 * @param obj user instance
	 * @param errors
	 */
  @Override
	public void validate(Object obj, Errors errors) {
		User user = (User) obj;
        String login = user.getLogin();
        String pass = user.getPassword();
		if (login == null || login.length() < 2 || login.length() > 8) {
			errors.rejectValue("login", "login.required", "login.invalid");
		}
        if (pass == null || pass.length() < 6 || pass.length() > 64) {
			errors.rejectValue("password", "password.required", "password.invalid");
		}

	}
}

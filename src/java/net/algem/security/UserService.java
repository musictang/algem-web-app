/*
 * @(#)UserService.java	1.0.6 18/11/15
 *
 * Copyright (c) 2013 Musiques Tangentes. All Rights Reserved.
 *
 * This file is part of Algem Agenda.
 * Algem Agenda is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Algem Agenda is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Algem Agenda. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.algem.security;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.algem.contact.Person;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.0 11/02/13
 */
@Component
public interface UserService
{
  /**
   * Checks if these login and pass belong to a valid user.
   * @param login username
   * @param pass clear password
   * @return true if user is valid
   */
  public boolean authenticate(String login, String pass);

  /**
   * Gets authentication info based on column value.
   * The column may be the pass or the salt
   * @param login username
   * @param col name of the column in the user table
   * @return an array of bytes
   */
  public byte[] findAuthInfo(String login, String col);

  /**
   * Checks if this {@code item} is enabled for this {@code user}.
   * @param item some item (ie menu item)
   * @param user user id
   * @return true if item is authorized
   */
  public boolean authorize(String item, int user);

  /**
   * Creates a new user.
   * @param u the user to create
   * @throws SQLException
   */
  public void create(User u) throws SQLException;

  /**
   * Updates current user.
   * @param u the user to update
   * @throws SQLException
   */
  public void update(User u) throws SQLException;

  /**
   * Gets some user by his id.
   * @param id user's id
   * @return some user or null if no user was found
   */
  public User findUserById(int id);

  /**
   * Gets some user by his login (or username).
   * @param login username
   * @return some user or null if no user was found
   */
  public User findUserByLogin(String login);

  public User findUserByEmail(String email);

  /**
   * Gets the list of users corresponding to this user id or this user login.
   * @param u current user
   * @return a list of users or null if no user was found
   */
  public List<User> exist(User u);

  /**
   * Checks if this user is really of type person.
   * @param u current user
   * @return true if this user is a person
   */
  public boolean isPerson(User u);

  public Person getPersonFromUser(int u);

  public List<Map<String, Boolean>> getAcl(int userId);

  /**
   * Records a token in user table to recover password.
   * @param userId current user id
   * @param token crypted string
   */
  public void setToken(int userId, String token);

  public PasswordResetToken getToken(int userId);

  public void updatePassword(int userId, String password);

  /**
   * Checks if the token transmitted belongs to this {@code userId}.
   * @param userId user's id
   * @param token temporary crypted string
   * @return true if token exists
   */
  public boolean hasToken(int userId, String token);
}


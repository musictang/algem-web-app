/*
 * @(#)UserService.java	1.6.2 03/05/17
 *
 * Copyright (c) 2016 Musiques Tangentes. All Rights Reserved.
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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.algem.config.Config;
import net.algem.contact.Person;
import net.algem.group.Group;
import net.algem.planning.ScheduleElement;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.2
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
  public void create(User u) throws UserException;

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

  /**
   * Gets some children by his parent id 
   * @param login username
   * @return some user or null if no user was found
   */
  public List<User> findChildrenById(int id);

  /**
   * Find a user by his email.
   *
   * @param email the email of the user
   * @return a user
   */
  public User findAuthenticatedUser(String email, int id);

  /**
   * Gets the list of users corresponding to this user id or this user login.
   * @param u the user to check
   * @return a list of users or null if no user was found
   */
  public List<User> exist(User u);

  /**
   * Checks if this user {@code u} is really of type person.
   * @param u the user to check
   * @return true if this user is a person
   */
  public boolean isPerson(User u);

  public Person getPersonFromUser(int u);

  public boolean isMember(String login, String startDate, String endDate);

  public boolean isTeacher(int id);

  public List<Group> getGroups(String login);

  public boolean hasPass(String login);

  public List<Map<String, Boolean>> getAcl(int userId);

  public List<ScheduleElement> getFollowUp(int userId, Date from, Date to);
  /**
   * Records a token in custom table to recover password.
   * @param userId current user id
   * @param token crypted string
   */
  public void setToken(int userId, String token);

  /**
   * Gets the token of the user {@code userId} if any.
   * @param userId user id
   * @return a token when result is not empty
   */
  public PasswordResetToken getToken(int userId);

  public void updatePassword(int userId, String password);

  public Config getConf(String key);

}


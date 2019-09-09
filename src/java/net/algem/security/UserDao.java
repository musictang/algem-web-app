/*
 * @(#) UserDao.java Algem Web App 1.1.0 19/02/2016
 *
 * Copyright (c) 2015 Musiques Tangentes. All Rights Reserved.
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

package net.algem.security;

import java.util.Date;
import java.util.List;
import java.util.Map;
import net.algem.contact.Email;
import net.algem.contact.Person;
import net.algem.group.Group;
import net.algem.planning.ScheduleElement;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.1.0 19/02/2016
 */
public interface UserDao {

  List<User> exist(int id, String login);

  User find(String login);

  List<User> findAll();

  User findAuthenticated(final String email, final int id);

  User findById(int id);

  int findPass(String userName);

  List<Email> getEmailsFromContact(final int id);

  List<Group> getGroups(String login);

  Person getPersonFromUser(final int userId);

  int getTeacher(int userId);

  boolean isMemberOnYear(String login, String start, String end);

  boolean isPerson(User u);

  List<Map<String, Boolean>> listMenuAccess(int userId);

  byte[] findAuthInfo(String login, String col);

  void createAccount(final User user);

  void setToken(final int userId, final String token);

  PasswordResetToken getToken(final int userId);

  void updatePassword(final int userId, final UserPass pass);

  List<ScheduleElement> getFollowUp(int userId, Date from, Date to);
}

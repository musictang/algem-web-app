/*
 * @(#)CommonUserService.java	1.0.6 30/11/15
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
 *
 */
package net.algem.security;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.algem.contact.Person;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.6 18/11/15
 */
@Service
public class CommonUserService
  implements UserService {

  private EncryptionService encryptionService;

  @Autowired
  private UserDao dao;

  public void setDao(UserDao dao) {
    this.dao = dao;
  }

  @Override
  public boolean authenticate(String login, String pass) {
    try {
      byte[] salt = findAuthInfo(login, "clef");// find salt in BD
      byte[] encryptedPassword = findAuthInfo(login, "pass");
      return encryptionService.authenticate(pass, encryptedPassword, salt);
    } catch (UserException ex) {
      return false;
    }
  }

  @Override
  public boolean authorize(String item, int user) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void create(User u) throws SQLException {
    if (encryptionService == null) {
      encryptionService = new PasswordEncryptionService();
    }
    try {
      UserPass pass = encryptionService.createPassword(u.getPassword());
      u.setPass(pass);
      dao.createAccount(u);
    } catch (UserException ex) {
      Logger.getLogger(CommonUserService.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void update(User u) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public User findUserById(int id) {
    return dao.findById(id);
  }

  @Override
  public User findUserByLogin(String login) {
    return dao.find(login);
  }

  @Override
  public User findUserByEmail(String email) {
    try {
      return dao.findByEmail(email);
    } catch(DataAccessException ex) {
      return null;
    }
  }

  @Override
  public byte[] findAuthInfo(String login, String colName) {
    return Base64.decodeBase64(dao.findAuthInfo(login, colName));
  }

  @Override
  public boolean isPerson(User u) {
    try {
    return dao.isPerson(u);
    } catch(DataAccessException ex) {
      return false;
    }
  }

  @Override
  public Person getPersonFromUser(int u) {
    Person p = dao.getPersonFromUser(u);
    if (p != null) {
      p.setEmail(dao.getEmailsFromContact(u));
    }
    return p;
  }

  @Override
  public List<User> exist(User u) {
    return dao.exist(u.getId(), u.getLogin());
  }

  @Override
  public List<Map<String, Boolean>> getAcl(int userId) {
    return dao.listMenuAccess(userId);
  }

  public void setToken(int userId, String token) {
    dao.setToken(userId, token);
  }

  public boolean hasToken(int userId, String token) {
    String t = null;
    try {
      PasswordResetToken savedToken = dao.getToken(userId);
//      Calendar cal = Calendar.getInstance();
//    if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
//      if (tk.getCreation())
    } catch(DataAccessException ex) {
      Logger.getLogger(CommonUserService.class.getName()).log(Level.SEVERE, null, ex);
    }
    if (t == null) return false;
    return t.equals(token);
  }

  @Override
  public PasswordResetToken getToken(int userId) {
    return dao.getToken(userId);
  }

  @Override
  public void updatePassword(int userId, String password) {
    try {
      UserPass pass = encryptionService.createPassword(password);
      dao.updatePassword(userId, pass);
    } catch (UserException ex) {
      Logger.getLogger(CommonUserService.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


}

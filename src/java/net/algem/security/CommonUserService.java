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
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
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

  public UserDao getDao() {
    return dao;
  }

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
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

  private byte[] findAuthInfo(String login, String colName) {
//    byte[] info = null;
//      int id = 0;
//      try {
//        Integer.parseInt(login);
//      } catch (NumberFormatException ex) {
//        id = -1;
//      }
    return Base64.decodeBase64(dao.findAuthInfo(login, colName));

  }


}

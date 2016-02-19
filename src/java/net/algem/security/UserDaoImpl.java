/*
 * @(#)UserDaoImpl.java	1.1.0 17/02/16
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.algem.config.ConfigIO;
import net.algem.config.ConfigKey;
import net.algem.contact.Email;
import net.algem.contact.Person;
import net.algem.contact.PersonIO;
import net.algem.contact.TeacherIO;
import net.algem.group.Group;
import net.algem.group.GroupIO;
import net.algem.util.AbstractGemDao;
import net.algem.util.Constants;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.0.0 11/02/13
 */
@Repository
public class UserDaoImpl
  extends AbstractGemDao implements UserDao 
        
{

  public static final String TABLE = "login";
  public static final String T_TOKEN = "jeton_login";
  public static final String T_PASSCARD = "carteabopersonne";

  @Autowired
  private ConfigIO configIO;

  public UserDaoImpl() {
  }

  @Override
  public List<User> findAll() {
    String query = "SELECT * FROM login";
    return jdbcTemplate.query(query, new RowMapper<User>() {

      @Override
      public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getFromRS(rs);
      }
    });
  }


  @Override
  public User find(String login) {
    String query = "SELECT l.idper,l.login,l.profil,p.nom,p.prenom FROM " 
            + TABLE + " l INNER JOIN " + PersonIO.TABLE + " p ON (l.idper = p.id) WHERE l.login = ?";
    return jdbcTemplate.queryForObject(query, new RowMapper<User>() {
      @Override
      public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getFromRS(rs);
      }
    }, login);

  }

  @Override
  public List<User> exist(int id, String login) {
    String query = "SELECT idper,login FROM " + TABLE + " WHERE idper = ? OR login = ?";
    return jdbcTemplate.query(query, new RowMapper<User>() {

      @Override
      public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User u = new User();
        u.setId(rs.getInt(1));
        u.setLogin(rs.getString(2));
        return u;
      }
    }, id, login);
  }

  @Override
  public User findById(int id) {
    String query = "SELECT l.idper,l.login,l.profil,p.nom,p.prenom FROM "
      + TABLE + " l INNER JOIN " + PersonIO.TABLE + " p ON (l.idper = p.id) "
      + "WHERE l.idper = ? AND (p.ptype = " + Person.PERSON + " OR p.ptype = " + Person.ROOM + ")";
    return jdbcTemplate.queryForObject(query, new RowMapper<User>() {

      @Override
      public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getFromRS(rs);
      }
    }, id);
  }

  @Override
  public User findByEmail(final String email) {
    String query = "SELECT l.idper,l.login,l.profil FROM "
            + TABLE + " l INNER JOIN email e ON (l.idper = e.idper)"
            + " WHERE e.email = ?";
    return jdbcTemplate.queryForObject(query, new RowMapper<User>()
    {
      @Override
      public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User u = new User();
        u.setId(rs.getInt(1));
        u.setLogin(rs.getString(2));
        u.setProfile(getProfileFromId(rs.getShort(3)));
        u.setEmail(email);
        return u;
      }
    }, email);
  }

  @Override
  public boolean isPerson(User u) {
    String query = "SELECT id FROM " + PersonIO.TABLE
      + " WHERE id = ? AND (ptype = " + Person.PERSON + " OR ptype = " + Person.ROOM + ")";
    return jdbcTemplate.queryForObject(query, Integer.class, u.getId()) > 0;
  }

  @Override
  public boolean isMemberOnYear(String login, String start, String end) {
    try {
      MapSqlParameterSource params = new MapSqlParameterSource();
      params.addValue("login", login);
      params.addValue("start", new java.sql.Date(Constants.DATE_FORMAT.parse(start).getTime()));
      params.addValue("end", new java.sql.Date(Constants.DATE_FORMAT.parse(end).getTime()));
      params.addValue("accounts", getMemberShipAccounts());
//      String debug = login +","+start+","+end+","+getMemberShipAccounts();
//      Logger.getLogger(UserDaoImpl.class.getName()).log(Level.INFO, debug);
      String query = "SELECT e.paye FROM echeancier2 e JOIN " + TABLE + " l ON (e.adherent = l.idper)"
        + " WHERE l.login = :login AND e.echeance BETWEEN :start AND :end AND e.compte IN(:accounts)";
      List<Boolean> result = namedJdbcTemplate.query(query, params, new RowMapper<Boolean>() {

        @Override
        public Boolean mapRow(ResultSet rs, int i) throws SQLException {
          return rs.getBoolean(1);
        }
      });
      for(Boolean b : result) {
        if (b) {
          return true;
        }
      }
      return false;
    } catch (ParseException | DataAccessException ex) {
      Logger.getLogger(UserDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }

  }

  @Override
  public int findPass(String userName) {
    String query = "SELECT c.idper FROM " + T_PASSCARD + " c JOIN " + TABLE + " l ON (c.idper = l.idper)"
            + " WHERE l.login = ? AND c.restant > 0 ORDER BY c.id DESC LIMIT 1";
    return jdbcTemplate.queryForObject(query, Integer.class, userName);
  }

  @Override
  public Person getPersonFromUser(final int userId) {
    String query = "SELECT nom,prenom,ptype FROM " + PersonIO.TABLE + " WHERE id = ?";
    return jdbcTemplate.queryForObject(query, new RowMapper<Person>() {

      @Override
      public Person mapRow(ResultSet rs, int i) throws SQLException {
        Person p = new Person();
        p.setId(userId);
        p.setName(rs.getString(1));
        p.setFirstName(rs.getString(2));
        p.setType(rs.getInt(3));
        return p;
      }
    }, userId);
  }

  @Override
  public List<Email> getEmailsFromContact(final int id) {
    String query = "SELECT email,archive,idx FROM email WHERE idper = ?";
    return jdbcTemplate.query(query, new RowMapper<Email>() {
      @Override
      public Email mapRow(ResultSet rs, int i) throws SQLException {
        Email e = new Email();
        e.setIdper(id);
        e.setEmail(rs.getString(1));
        e.setArchive(rs.getBoolean(2));
        e.setIndex(rs.getShort(3));
        return e;
      }
    }, id);
  }

  @Override
  public List<Group> getGroups(String login) {
    String query = "SELECT g.id,g.nom FROM " + GroupIO.TABLE + " g WHERE g.id IN ("
      + "SELECT gd.id FROM " + GroupIO.TABLE_DET + " gd JOIN " + TABLE + " l on l.idper = gd.musicien"
      + " AND l.login = ?)";

    return jdbcTemplate.query(query, new RowMapper<Group>() {

      @Override
      public Group mapRow(ResultSet rs, int i) throws SQLException {
        Group g = new Group();
        g.setId(rs.getInt(1));
        g.setName(rs.getString(2));
        return g;
      }

    }, login);
  }

  @Override
  public int getTeacher(int userId) {
    String query = "SELECT idper FROM " + TeacherIO.TABLE + " WHERE idper = ? AND actif = TRUE";
    return jdbcTemplate.queryForObject(query, Integer.class, userId);
  }

  @Override
  public List<Map<String, Boolean>> listMenuAccess(int userId) {
    String query = "SELECT m.label, a.autorisation FROM  menu2 m JOIN menuaccess a ON m.id = a.idmenu WHERE a.idper = ?";
    return jdbcTemplate.query(query, new RowMapper<Map<String, Boolean>>() {

      @Override
      public Map<String, Boolean> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put(rs.getString(1), rs.getBoolean(2));
        return map;
      }
    }, userId);

  }


  @Override
  public byte[] findAuthInfo(String login, String col) {
    int id = getIdFromLogin(login);
    String query = "SELECT " + col + " FROM " + TABLE + " WHERE idper = ? OR login = ?";
    String result = jdbcTemplate.queryForObject(query, String.class, new Object[]{id, login});
    return Base64.decodeBase64(result);
  }

  @Override
  public void createAccount(final User user) {
    jdbcTemplate.update(new PreparedStatementCreator() {

      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("INSERT INTO " + TABLE + " VALUES(?,?,?,?,?)");
        ps.setInt(1, user.getId());
        ps.setString(2, user.getLogin());
        ps.setInt(3, Profile.Member.getId());
        ps.setString(4, Base64.encodeBase64String(user.getPass().getPass()));
        ps.setString(5, Base64.encodeBase64String(user.getPass().getKey()));

        return ps;
      }
    });
  }

  @Transactional
  @Override
  public void setToken(final int userId, final String token) {
    deleteToken(userId);
    createToken(userId, token);
  }


  @Override
  public PasswordResetToken getToken(final int userId) {
    String query = "SELECT jeton,creadate FROM " + T_TOKEN + " WHERE idper = ?";

    return jdbcTemplate.queryForObject(query, new RowMapper<PasswordResetToken>() {

      @Override
      public PasswordResetToken mapRow(ResultSet rs, int i) throws SQLException {
        PasswordResetToken token = new PasswordResetToken(userId);
        token.setToken(rs.getString(1));
        token.setCreation(rs.getTimestamp(2).getTime());
        return token;
      }
    }, userId);
  }

  public void deleteToken(final int userId) {
    String sql = "DELETE FROM " + T_TOKEN + " WHERE idper = ?";
    jdbcTemplate.update(sql, new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setInt(1, userId);
      }
    });
  }

  @Transactional
  @Override
  public void updatePassword(final int userId, final UserPass pass) {
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("UPDATE login " + TABLE + " SET pass = ?, clef=? WHERE idper = ?");
        ps.setString(1, Base64.encodeBase64String(pass.getPass()));
        ps.setString(2, Base64.encodeBase64String(pass.getKey()));
        ps.setInt(3, userId);
        return ps;
      }
    });
    deleteToken(userId);
  }

  private User getFromRS(ResultSet rs) throws SQLException {
    User u = new User();
    u.setId(rs.getInt(1));
    u.setLogin(rs.getString(2));
    u.setProfile(getProfileFromId(rs.getShort(3)));
    u.setName(rs.getString(4));
    u.setFirstName(rs.getString(5));

    return u;
  }

  private Profile getProfileFromId(int id) {
    switch (id) {

      case 1:
        return Profile.User;
      case 2:
        return Profile.Teacher;
      case 3:
        return Profile.Public;
      case 4:
        return Profile.Admin;
      case 10:
        return Profile.Visitor;
      case 11:
        return Profile.Member;
      default:
        return Profile.Visitor;
    }
  }

  /**
   * Gets the encrypted password.
   *
   * @param salt base64-encoded salt
   * @param pass base64-encoded pass
   * @return user pass info
   */
  private UserPass getUserPass(String pass, String salt) {

    byte[] b64pass = Base64.decodeBase64(pass);
    byte[] b64salt = Base64.decodeBase64(salt);

    return new UserPass(b64pass, b64salt);
  }

  private int getIdFromLogin(String login) {
    try {
      return Integer.parseInt(login);
    } catch (NumberFormatException nfe) {
      return -1;
    }
  }

  private void createToken(final int userId, final String token) {
    String query = "INSERT INTO " + T_TOKEN + " VALUES(?,?,?)";
    jdbcTemplate.update(query, new PreparedStatementSetter() {

      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setInt(1, userId);
        ps.setString(2, token);
        ps.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
      }
    });

  }

  private List<Integer> getMemberShipAccounts() {
    List<Integer> accounts = new ArrayList<>();
    int a1 = configIO.findAccount(ConfigKey.MEMBERSHIP_ACCOUNT.getKey());
    int a2 = configIO.findAccount(ConfigKey.PRO_MEMBERSHIP_ACCOUNT.getKey());

    accounts.add(a1);
    if (a2 > 0) {
      accounts.add(a2);
    }
    return accounts;
  }

}
/*
 * @(#)CustomJdbcUserService.java	1.5.2 05/01/17
 *
 * Copyright (c) 2015-2017 Musiques Tangentes. All Rights Reserved.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.5.2
 * @since 1.0.6 25/11/2015
 */
public class CustomJdbcUserService
        extends JdbcDaoImpl
{

  private String customUsersByUsernameQuery = "SELECT trim(l.login),l.pass,l.clef FROM login l INNER JOIN personne p ON (l.idper = p.id) WHERE trim(l.login) = ?";
  private String customAuthoritiesByUsernameQuery = "SELECT profil FROM login WHERE trim(login) = ?";

  public String getCustomUsersByUsernameQuery() {
    return customUsersByUsernameQuery;
  }

  public void setCustomUsersByUsernameQuery(String customUsersByUsernameQuery) {
    this.customUsersByUsernameQuery = customUsersByUsernameQuery;
  }

  public String getCustomAuthoritiesByUsernameQuery() {
    return customAuthoritiesByUsernameQuery;
  }

  public void setCustomAuthoritiesByUsernameQuery(String customAuthoritiesByUsernameQuery) {
    this.customAuthoritiesByUsernameQuery = customAuthoritiesByUsernameQuery;
  }

  /**
   * Executes the SQL <tt>usersByUsernameQuery</tt> and returns a list of UserDetails objects.
   * There should normally only be one matching user.
   *
   * @param login
   * @return
   */
  @Override
  protected List<UserDetails> loadUsersByUsername(String login) {
//    int idper = getIdFromLogin(login);

    return getJdbcTemplate().query(customUsersByUsernameQuery, new Object[]{login}, new RowMapper<UserDetails>()
    {
      public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {

        String login = rs.getString(1);
        String password = rs.getString(2);
        String salt = rs.getString(3);

        boolean enabled = true;

        if (password == null || password.isEmpty()) {
          password = " ";
        }
        CustomUserDetails cu = new CustomUserDetails(login, password, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES);
        cu.setSalt(salt == null || salt.isEmpty() ? " " : salt);

        return cu;
      }

    });
  }

  protected List<GrantedAuthority> loadUserAuthorities(String login) {
//    int idper = getIdFromLogin(login);

    return getJdbcTemplate().query(customAuthoritiesByUsernameQuery, new Object[]{login}, new RowMapper<GrantedAuthority>()
    {
      public GrantedAuthority mapRow(ResultSet rs, int rowNum) throws SQLException {
        String roleName = String.valueOf(rs.getInt(1));
//        String roleName = rs.getString(1);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        return authority;
      }
    });

  }

  @Override
  protected UserDetails createUserDetails(String username, UserDetails userFromUserQuery, List<GrantedAuthority> combinedAuthorities) {
    String returnUsername = userFromUserQuery.getUsername();
    CustomUserDetails cu = new CustomUserDetails(returnUsername, userFromUserQuery.getPassword(), userFromUserQuery.isEnabled(),
            true, true, true, combinedAuthorities);
    cu.setSalt(((CustomUserDetails) userFromUserQuery).getSalt());
//    cu.setFullName(((CustomUserDetails) userFromUserQuery).getFullName());
    return cu;
  }

  private int getIdFromLogin(String login) {
    try {
      return Integer.parseInt(login);
    } catch (NumberFormatException nfe) {
      return -1;
    }
  }

}

/*
 * @(#)PersonIO.java	1.0.2 28/01/14
 *
 * Copyright (c) 2014 Musiques Tangentes. All Rights Reserved.
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
package net.algem.contact;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import net.algem.util.AbstractGemDao;
import net.algem.util.AuthUtil;
import net.algem.util.GemConstants;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.2
 * @since 1.0.0 11/02/13
 */
@Repository
public class PersonIO
  extends AbstractGemDao {

  public static final String TABLE = "personne";


  public List<Person> findEstablishments(String where, String login) {
    boolean adminAccess = AuthUtil.isAdministrativeMember();
    String query = "SELECT p.id, p.nom FROM " + TABLE + " p";

    if (adminAccess) {
      query += " JOIN " + GemConstants.ESTAB_TABLE + " e ON (p.id = e.id) "
        + " JOIN login l ON (e.idper = l.idper)"
        + " WHERE p.ptype = " + Person.ESTABLISHMENT
        + " AND e.actif = TRUE"
        + " AND l.login = ?";
    } else {
      query += " WHERE p.ptype = " + Person.ESTABLISHMENT;
    }
    if (where != null && !where.isEmpty()) {
      query += where;
    }
    if (adminAccess) {
      return jdbcTemplate.query(query, new RowMapper<Person>() {
      @Override
      public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getEstabFromResult(rs);
      }
    }, login);
    } else {
      return jdbcTemplate.query(query, new RowMapper<Person>() {
      @Override
      public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getEstabFromResult(rs);
      }
    });
    }
  }

  private Person getEstabFromResult(ResultSet rs) throws SQLException {
    Person p = new Person();
    p.setId(rs.getInt(1));
    p.setName(rs.getString(2));
    return p;
  }
}

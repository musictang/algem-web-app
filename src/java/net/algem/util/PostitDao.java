/*
 * @(#) PostitDao.java Algem Web App 1.5.2 11/01/2017
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import net.algem.planning.DateFr;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.5.2
 * @since 1.5.2 11/01/2017
 */
@Repository
public class PostitDao
  extends AbstractGemDao {

  public final static String TABLE = "postit";
  private final static Logger LOGGER = Logger.getLogger(PostitDao.class.getName());

  public List<Postit> find(int dest) {

    String query = "SELECT pt.id,pt.ptype,pt.emet,pt.dest,pt.jour,pt.echeance,pt.texte FROM " + TABLE + " pt  WHERE pt.ptype = " + Postit.EXTERNAL + " AND dest = ?";
    return jdbcTemplate.query(query, new RowMapper<Postit>() {
      @Override
      public Postit mapRow(ResultSet rs, int rowNum) throws SQLException {
        Postit p = new Postit();
        p.setId(rs.getInt(1));
        p.setType(rs.getInt(2));
        p.setIssuer(rs.getInt(3));
        p.setReceiver(rs.getInt(4));
				p.setDay(new DateFr(rs.getString(5)).getDate());
				p.setTerm(new DateFr(rs.getString(6)).getDate());
        p.setText(rs.getString(7));
        return p;
      }
    }, dest);
  }
}

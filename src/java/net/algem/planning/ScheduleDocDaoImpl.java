/*
 * @(#) ScheduleDocDaoImpl.java Algem Web App 1.6.0 03/02/2017
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
 */

package net.algem.planning;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.algem.util.AbstractGemDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.0
 * @since 1.6.0 03/02/2017
 */
@Repository
public class ScheduleDocDaoImpl 
        extends AbstractGemDao 
{

  private final static Logger LOGGER = Logger.getLogger(ScheduleDocDaoImpl.class.getName());
  
  public List<ScheduleDoc> findActionDocuments(final int actionId) {
    String query = "SELECT id,datedebut,idplanning,idper,doctype,nom,uri FROM document_action WHERE idaction = ?";
    return jdbcTemplate.query(query, new RowMapper<ScheduleDoc>() {
      @Override
      public ScheduleDoc mapRow(ResultSet rs, int rowNum) throws SQLException {

        ScheduleDoc doc = new ScheduleDoc(rs.getInt(1), actionId);
        doc.setFirstDate(rs.getDate(2));
        doc.setScheduleId(rs.getInt(3));
        doc.setMemberId(rs.getInt(4));
        doc.setDocType(rs.getShort(5));
        doc.setName(rs.getString(6));
        doc.setUri(rs.getString(7));
        LOGGER.log(Level.WARNING, doc.getName());
        return doc;
      }
    }, actionId);
  }
}

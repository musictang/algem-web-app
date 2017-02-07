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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.algem.util.AbstractGemDao;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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

  public final static String TABLE = "document_action";
  private final static Logger LOGGER = Logger.getLogger(ScheduleDocDaoImpl.class.getName());
  
  public int create(final ScheduleDoc doc) {
//    id,datedebut,idaction,idplanning,idper,doctype,nom,uri 
    final String query = "INSERT INTO " + TABLE + " VALUES(DEFAULT,?,?,?,?,?,?,?)";
    LOGGER.log(Level.INFO, doc.toString());
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
        ps.setDate(1, new java.sql.Date(doc.getFirstDate().getTime()));
        ps.setInt(2, doc.getActionId());
        ps.setInt(3, doc.getScheduleId());
        ps.setInt(4, doc.getMemberId());
        ps.setShort(5, doc.getDocType());
        ps.setString(6, doc.getName());
        ps.setString(7,doc.getUri());
        return ps;
      }
    }, keyHolder);
    return keyHolder.getKey().intValue();
  }
  
  public void update(final ScheduleDoc doc) {
    final String query = "UPDATE " + TABLE + " SET datedebut = ?, doctype = ?, nom = ?, uri = ? WHERE id = ?";
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setDate(1, new java.sql.Date(doc.getFirstDate().getTime()));
        ps.setShort(2, doc.getDocType());
        ps.setString(3, doc.getName());
        ps.setString(4,doc.getUri());
        ps.setInt(5, doc.getId());
        return ps;
      }
    });
  }
  
  public ScheduleDoc findActionDocument(final int docId) {
    String query = "SELECT id,datedebut,idaction,idplanning,idper,doctype,nom,uri FROM " + TABLE + " WHERE id = ?";
    return jdbcTemplate.queryForObject(query, new RowMapper<ScheduleDoc>() {
      @Override
      public ScheduleDoc mapRow(ResultSet rs, int row) throws SQLException {
        ScheduleDoc doc = new ScheduleDoc();
        doc.setId(rs.getInt(1));
        doc.setFirstDate(rs.getDate(2));
        doc.setActionId(rs.getInt(3));
        doc.setScheduleId(rs.getInt(4));
        doc.setMemberId(rs.getInt(5));
        doc.setDocType(rs.getShort(6));
        doc.setName(rs.getString(7));
        doc.setUri(rs.getString(8));
        LOGGER.log(Level.WARNING, doc.toString());
        return doc;
      }
      
    }, docId);
  }
  
  public List<ScheduleDoc> findActionDocuments(final int actionId) {
    String query = "SELECT id,datedebut,idplanning,idper,doctype,nom,uri FROM " + TABLE + " WHERE idaction = ?";
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

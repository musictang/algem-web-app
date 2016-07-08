/*
 * @(#) TeacherDaoImpl.java Algem Web App 1.4.0 27/06/2016
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
package net.algem.contact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.algem.planning.DateFr;
import net.algem.planning.FollowUp;
import net.algem.planning.Hour;
import net.algem.planning.ScheduleDao;
import net.algem.planning.ScheduleElement;
import net.algem.planning.ScheduleRangeElement;
import net.algem.planning.ScheduleRangeIO;
import net.algem.util.AbstractGemDao;
import net.algem.util.NamedModel;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.4.0
 * @since 1.0.6 06/01/2016
 */
@Repository
public class TeacherDaoImpl
        extends AbstractGemDao
        implements TeacherDao
{

  public static final String TABLE = "prof";
  public static final String FOLLOW_UP_T = "suivi";
  public static final String FOLLOW_UP_SEQ = "idsuivi";

  private final static String FOLLOWUP_STATEMENT = "SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.jour,pl.debut,pl.fin,p.lieux,p.note,c.id,c.titre,c.collectif,s.nom,v.texte"
          + " FROM " + ScheduleDao.TABLE + " p"
          + " JOIN " + ScheduleRangeIO.TABLE + " pl ON (p.id = pl.idplanning)"
          + " JOIN " + ScheduleDao.T_ACTION + " a ON (p.action = a.id)"
          + " JOIN cours c ON (a.cours = c.id)"
          + " JOIN salle s ON (p.lieux = s.id)"
          + " JOIN suivi v ON (p.note = v.id)"
          + " WHERE p.idper = ?"
          + " AND jour BETWEEN ? AND ?"
          + " ORDER BY p.jour,pl.debut";

  @Override
  public List<ScheduleElement> findFollowUp(final int teacher, Date from, Date to) {
    System.out.println(FOLLOWUP_STATEMENT);
    return jdbcTemplate.query(FOLLOWUP_STATEMENT, new RowMapper<ScheduleElement>()
    {
      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        d.setEnd(new Hour(rs.getString(4)));
        d.setIdPerson(teacher);
        d.setPlace(rs.getInt(5));
        d.setNote(rs.getInt(6));
        d.setDetail("course", new NamedModel(rs.getInt(7), rs.getString(8)));
        d.setCollective(rs.getBoolean(9));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(10)));
        d.setDetail("note", new NamedModel(d.getNote(), rs.getString(11)));
        d.setDetail("estab", null);

        Collection<ScheduleRangeElement> ranges = getRanges(d.getId(), d.getStart().toString());
        d.setRanges(ranges);

        return d;
      }
    }, teacher, from, to);
  }

  private Collection<ScheduleRangeElement> getRanges(final int id, String start) {
    String query = " SELECT pl.id,pl.debut,pl.fin,pl.adherent,pl.note,p.nom,p.prenom,p.pseudo,s.id,s.texte"
            + " FROM " + ScheduleRangeIO.TABLE + " pl JOIN " + PersonIO.TABLE + " p ON (pl.adherent = p.id)"
            + " LEFT JOIN suivi s ON (pl.note = s.id)"
            + " WHERE pl.idplanning = ? AND pl.debut = ? ORDER BY pl.debut";
    System.out.println(query);
    return jdbcTemplate.query(query, new RowMapper<ScheduleRangeElement>()
    {

      @Override
      public ScheduleRangeElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleRangeElement r = new ScheduleRangeElement();
        r.setId(rs.getInt(1));
        r.setScheduleId(id);
        r.setStart(new Hour(rs.getString(2)));
        r.setEnd(new Hour(rs.getString(3)));
        r.setMemberId(rs.getInt(4));
        r.setNote(rs.getInt(5));
        Person p = new Person(r.getMemberId());
        p.setName(rs.getString(6));
        p.setFirstName(rs.getString(7));
        p.setNickName(rs.getString(8));
        r.setPerson(p);
        FollowUp up = new FollowUp();
        up.setId(rs.getInt(9));
        up.setContent(rs.getString(10));
        r.setFollowUp(up);

        return r;
      }
    }, id, java.sql.Time.valueOf(start + ":00"));
  }
  
    @Override
  public void updateFollowUp(final FollowUp follow) {
    String text = follow.getContent();
    if (text == null || text.isEmpty()) {
      //delete
      return;
    }
    if (follow.getId() > 0) {
      updateFollowUpContent(follow);
    } else {
      createFollowUp(follow);
      updateSchedule(follow.getScheduleId(), follow.getId(), follow.isCollective());
    }
    

  }

  @Override
  public void createFollowUp(final FollowUp follow) {
    final int id = nextId(FOLLOW_UP_SEQ);
    follow.setId(id);
    final String sql = "INSERT INTO suivi(id,texte) VALUES(?,?)";
    jdbcTemplate.update(new PreparedStatementCreator()
    {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.setString(2, follow.getContent());
        return ps;
      }
    });

  }

  private void updateFollowUpContent(final FollowUp follow) {
    final String sql = "UPDATE suivi SET texte = ? WHERE id = ?";
    jdbcTemplate.update(new PreparedStatementCreator()
    {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, follow.getContent());
        ps.setInt(2, follow.getId());

        return ps;
      }
    });
  }

  private void updateSchedule(final int id, final int noteId, boolean collective) {
    
    final String sql = "UPDATE " + (collective ? ScheduleDao.TABLE : ScheduleRangeIO.TABLE) + " SET note = ? WHERE id = ?";
    jdbcTemplate.update(new PreparedStatementCreator()
    {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, noteId);
        ps.setInt(2, id);

        return ps;
      }
    });
  }

}

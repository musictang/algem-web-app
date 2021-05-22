/*
 * @(#) TeacherDaoImpl.java Algem Web App 1.7.0 04/10/17
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
package net.algem.contact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.algem.planning.DateFr;
import net.algem.planning.FollowUp;
import net.algem.planning.Hour;
import net.algem.planning.ScheduleDao;
import net.algem.planning.ActionDocumentDaoImpl;
import net.algem.planning.ScheduleElement;
import net.algem.planning.ScheduleRangeElement;
import net.algem.planning.ScheduleRangeIO;
import net.algem.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.0
 * @since 1.0.6 06/01/2016
 */
@Repository
public class TeacherDaoImpl
  extends AbstractGemDao
  implements TeacherDao {

  public static final String TABLE = "prof";
  public static final String FOLLOW_UP_T = "suivi";
  public static final String FOLLOW_UP_SEQ = "idsuivi";

  private final static String FOLLOWUP_STATEMENT = "SELECT DISTINCT ON (p.jour,pl.debut) p.id,p.action,p.jour,pl.debut,pl.fin,p.lieux,p.note,c.id,c.titre,c.collectif,s.nom,v.texte,v.statut"
    + " FROM " + ScheduleDao.TABLE + " p"
    + " JOIN " + ScheduleRangeIO.TABLE + " pl ON (p.id = pl.idplanning)"
    + " JOIN " + ScheduleDao.T_ACTION + " a ON (p.action = a.id)"
    + " JOIN cours c ON (a.cours = c.id)"
    + " JOIN salle s ON (p.lieux = s.id)"
    + " LEFT JOIN suivi v ON (p.note = v.id)"
    + " WHERE p.idper = ?"
    + " AND jour BETWEEN ? AND ?"
    + " ORDER BY p.jour,pl.debut";

  private final static Logger LOGGER = Logger.getLogger(TeacherDaoImpl.class.getName());

  @Autowired
  private CommonDao commonDao;

  @Autowired
  private ActionDocumentDaoImpl docDao;

  @Autowired
  private GemOptions options;

  private Map<Integer,String> photoCache = new HashMap<>();

  @Override
  public List<ScheduleElement> findFollowUpSchedules(final int teacher, Date from, Date to) {
//    System.out.println(FOLLOWUP_STATEMENT);
    return jdbcTemplate.query(FOLLOWUP_STATEMENT, new RowMapper<ScheduleElement>() {
      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setIdAction(rs.getInt(2));
        d.setDateFr(new DateFr(rs.getString(3)));
        d.setStart(new Hour(rs.getString(4)));
        d.setEnd(new Hour(rs.getString(5)));
        d.setIdPerson(teacher);
        d.setPlace(rs.getInt(6));
        d.setNote(rs.getInt(7));
        d.setDetail("course", new NamedModel(rs.getInt(8), rs.getString(9)));
        d.setCollective(rs.getBoolean(10));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(11)));
        d.setDetail("estab", null);

        FollowUp up = new FollowUp();
        up.setId(d.getNote());
        up.setContent(rs.getString(12));
        up.setStatus(rs.getShort(13));
        d.setFollowUp(up);

        Collection<ScheduleRangeElement> ranges = getRanges(d.getId(), d.getStart().toString());
        d.setRanges(ranges);
        d.setDocuments(docDao.findActionDocuments(d.getIdAction()));
        return d;
      }
    }, teacher, from, to);
  }

  public FollowUp findFollowUp(final int id) {
    String query = "SELECT texte,note,statut FROM " + FOLLOW_UP_T + " WHERE id = ?";
    return jdbcTemplate.queryForObject(query, new RowMapper<FollowUp>() {
      @Override
      public FollowUp mapRow(ResultSet rs, int arg1) throws SQLException {
        FollowUp up = new FollowUp();
        up.setId(id);
        up.setContent(rs.getString(1));
        up.setNote(rs.getString(2));
        up.setStatus(rs.getShort(3));

        return up;
      }
    }, id);
  }

  private Collection<ScheduleRangeElement> getRanges(final int id, String start) {
    String query = "SELECT pl.id,pl.debut,pl.fin,pl.adherent,pl.note,p.nom,p.prenom,p.pseudo,s.id,s.texte,s.note,s.statut,COALESCE(em1.email, em2.email),COALESCE(t1.numero, t2.numero)"
      + " FROM " + ScheduleRangeIO.TABLE
      + " pl JOIN " + PersonIO.TABLE + " p ON (pl.adherent = p.id)"
      + " JOIN eleve e ON (p.id = e.idper)"
      + " LEFT JOIN email em1 ON (e.idper = em1.idper AND em1.idx = 0)"
      + " LEFT JOIN telephone t1 ON (e.idper = t1.idper AND t1.idx = 0)";
    if (options.withFamilyManagement()) {
      query += " LEFT JOIN email em2 ON (e.famille = em2.idper AND em2.idx = 0)"
      + " LEFT JOIN telephone t2 ON (e.famille = t2.idper AND t2.idx = 0)";
        
    } else {
      query += " LEFT JOIN email em2 ON (e.payeur = em2.idper AND em2.idx = 0)"
      + " LEFT JOIN telephone t2 ON (e.payeur = t2.idper AND t2.idx = 0)";
    }
    query += " LEFT JOIN suivi s ON (pl.note = s.id)"
      + " WHERE pl.idplanning = ? AND pl.debut = ? ORDER BY pl.debut";

    return jdbcTemplate.query(query, new RowMapper<ScheduleRangeElement>() {

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
        p.setPhoto(getPhotoAsBase64(r.getMemberId()));

        List<Email> emails = new ArrayList<Email>();
        if (rs.getString(13) != null) {
          emails.add(new Email(rs.getString(13)));
        }
        p.setEmails(emails);
        List<Tel> tels = new ArrayList<Tel>();
        if (rs.getString(14) != null) {
          tels.add(new Tel(rs.getString(14)));
        }
        p.setEmails(emails);
        p.setTels(tels);
        r.setPerson(p);
        FollowUp up = new FollowUp();
        up.setId(rs.getInt(9));
        up.setContent(rs.getString(10));
        up.setNote(rs.getString(11));
        up.setStatus(rs.getShort(12));
        r.setFollowUp(up);


        return r;
      }
    }, id, java.sql.Time.valueOf(start + ":00"));
  }

  private String getPhotoAsBase64(int idper) {
    String data = photoCache.get(idper);
    try {
      if (data == null) {
        data = commonDao.findPhoto(idper);
        photoCache.put(idper, data);
      }
    } catch (DataAccessException ex) {
      LOGGER.log(Level.WARNING, ex.getMessage());
      return null;
    }
    return data;
  }

  @Override
  public int updateFollowUp(final FollowUp up) {
    String text = up.getContent();
    if ((text == null || text.isEmpty())
      && (up.getNote() == null || up.getNote().isEmpty())
      && !up.isAbsent() && !up.isExcused()) {
      deleteFollowUp(up);
      return 0;
    } else if (up.getId() > 0) {
      updateFollowUpContent(up);
      return 1;
    } else {
      createFollowUp(up);
      updateSchedule(up.getScheduleId(), up.getId(), up.isCollective());
      return 2;
    }

  }

  @Override
  public int updateAbsenceStatus(final FollowUp up) {
    if (up.getId() == 0) {
      if (up.getStatus() > 0) {
        createFollowUp(up);
        updateSchedule(up.getScheduleId(), up.getId(), false);
        return 2;
      }
      return 0;
    } else {
      final String sql = "UPDATE suivi SET statut=? WHERE id = ?";
      jdbcTemplate.update(new PreparedStatementCreator() {
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
          PreparedStatement ps = con.prepareStatement(sql);
          ps.setShort(1, up.getStatus());
          ps.setInt(2, up.getId());

          LOGGER.log(Level.INFO, ps.toString());
          return ps;
        }
      });
      return 1;
    }
  }

  @Override
  public void createFollowUp(final FollowUp up) {
    final int id = nextId(FOLLOW_UP_SEQ);
    up.setId(id);

    final String sql = "INSERT INTO suivi(id,texte,note,statut) VALUES(?,?,?,?)";
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.setString(2, up.getContent());
        ps.setString(3, up.getNote());
        ps.setShort(4, up.getStatus());

        LOGGER.log(Level.INFO, ps.toString());
        return ps;
      }
    });

  }

  private void deleteFollowUp(final FollowUp up) {
    final String sql = "DELETE FROM suivi WHERE id = ?";
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, up.getId());
        return ps;
      }
    });
    updateSchedule(up.getScheduleId(), 0, up.isCollective());// reset
//    follow.setId(0);
  }

  private void updateFollowUpContent(final FollowUp up) {
    final String sql = "UPDATE suivi SET texte = ?, note=?, statut=? WHERE id = ?";
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, up.getContent());
        ps.setString(2, up.getNote());
        ps.setShort(3, up.getStatus());
        ps.setInt(4, up.getId());
        return ps;
      }
    });
  }

  private void updateSchedule(final int id, final int noteId, boolean collective) {

    final String sql = "UPDATE " + (collective ? ScheduleDao.TABLE : ScheduleRangeIO.TABLE) + " SET note = ? WHERE id = ?";
    jdbcTemplate.update(new PreparedStatementCreator() {
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

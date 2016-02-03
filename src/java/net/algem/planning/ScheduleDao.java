/*
 * @(#)ScheduleDao.java	1.0.4 25/05/15
 *
 * Copyright (c) 2015 Musiques Tangentes. All Rights Reserved.
 *
 * This file is part of Algem Agenda.
 * Algem Agenda is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Algem Agenda is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Algem Agenda. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.algem.planning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.algem.room.Room;
import net.algem.util.AbstractGemDao;
import net.algem.util.Constants;
import net.algem.util.NamedModel;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * IO methods for class {@link net.algem.planning.Schedule}.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.4
 * @since 1.0.0 11/02/13
 */
@Repository
public class ScheduleDao
  extends AbstractGemDao {

  public final static String TABLE = "planning";
  /** Action table. */
  public final static String ACTION = "action";
  public final static String ROOM_TIMES_TABLE = "horaires";
  public final static String SEQUENCE = "planning_id_seq";
  public final static String COLUMNS = "p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.action,p.lieux,p.note";

  public void insert(Schedule p) throws SQLException {

    int id = nextId(SEQUENCE);

    String query = "INSERT INTO " + TABLE + " VALUES("
      + id
      + ",'" + p.getDate().toString() + "'"
      + ",'" + p.getStart() + "'"
      + ",'" + p.getEnd() + "'"
      + "," + p.getType()
      + "," + p.getIdPerson()
      + "," + p.getIdAction()
      + "," + p.getPlace()
      + "," + p.getNote()
      + ")";
  }

  public void update(Schedule p) throws SQLException {
    String query = "UPDATE " + TABLE + " SET "
      + "jour = '" + p.getDate()
      + "',debut = '" + p.getStart()
      + "',fin = '" + p.getEnd()
      + "',ptype = " + p.getType()
      + ",idper = " + p.getIdPerson()
      + ",action = " + p.getIdAction()
      + ",lieux = " + p.getPlace()
      + ",note = " + p.getNote()
      + " WHERE id = " + p.getId();

  }

  public void delete(Schedule p) throws SQLException {
    String query = "DELETE FROM " + TABLE + " WHERE id = " + p.getId();
  }

  /**
   * Retrieves the schedules of the {@code date}.
   *
   * @param date selected date
   * @param estab establishment number
   * @return a list of schedule elements
   */
  public List<ScheduleElement> find(Date date, int estab) {
    String query = " SELECT p.*, c.id, c.titre, c.collectif, s.nom, t.prenom, t.nom"
      + " FROM " + TABLE + " p INNER JOIN action a LEFT OUTER JOIN cours c ON (a.cours = c.id)"
      + " ON (p.action = a.id) LEFT OUTER JOIN personne t ON (t.id = p.idper)"
      + ", salle s"
      + " where p.lieux = s.id"
      + " AND s.public = true"
      + " AND jour = ?"
      + " AND s.etablissement = ?"
      + " ORDER BY s.nom, p.debut";
    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {

      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDate(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        d.setEnd(new Hour(rs.getString(4)));
        d.setType(rs.getInt(5));
        d.setIdPerson(rs.getInt(6));
        d.setIdAction(rs.getInt(7));
        d.setPlace(rs.getInt(8));
        d.setNote(rs.getInt(9));
        d.setDetail("course", new NamedModel(rs.getInt(10), rs.getString(11)));
//        d.setCourseName(rs.getString(10));
        d.setCollective(rs.getBoolean(12));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(13)));
        d.setDetail("estab", null);
        String firstName = rs.getString(14);
        String lastName = rs.getString(15);
        String name = firstName == null ? (lastName == null ? "" : lastName) : firstName + " " + lastName;
        d.setDetail("person",new NamedModel(d.getIdPerson(), name));
        if (d.type == Schedule.COURSE && !d.isCollective()) {
          d.setRanges(getTimeSlots(d.getId()));
        }
        return d;
      }
    }, date, estab);
  }

  /**
   * Gets the list of free public rooms (whithout any schedule for this
   * {@code date} in {@code estab}.
   *
   * @param date selected date
   * @param estab establishment number
   * @return a list of rooms
   */
  public List<Room> getFreeRoom(Date date, int estab) {
    String query = "SELECT id,nom FROM salle s"
      + " WHERE s.public = true"
      + " AND s.etablissement = ?"
      + " AND s.id NOT IN ("
      + " SELECT DISTINCT lieux FROM " + TABLE + " WHERE jour = ?) ORDER BY s.nom";
    return jdbcTemplate.query(query, new RowMapper<Room>() {

      @Override
      public Room mapRow(ResultSet rs, int rowNum) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt(1));
        r.setName(rs.getString(2));
        return r;
      }
    }, estab, date);
  }

  /**
   * Retrieves the time slots included in schedule which id is {@code id}.
   *
   * @param id schedule's id
   * @return a collection of schedule ranges
   */
  private Collection<ScheduleRange> getTimeSlots(int id) {
    String query = " SELECT pl.* FROM " + TABLE + " p," + ScheduleRangeIO.TABLE + " pl"
      + " WHERE pl.idplanning = p.id AND p.id = ?";
    return jdbcTemplate.query(query, new RowMapper<ScheduleRange>() {

      @Override
      public ScheduleRange mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleRange r = new ScheduleRange();
        r.setId(rs.getInt(1));
        r.setScheduleId(rs.getInt(2));
        r.setStart(new Hour(rs.getString(3)));
        r.setEnd(new Hour(rs.getString(4)));
        r.setMemberId(rs.getInt(5));
        r.setNote(rs.getInt(6));

        return r;
      }
    }, id);
  }

  public DailyTimes[] find(int roomId) throws SQLException {
    String query = "SELECT jour, ouverture, fermeture FROM " + ROOM_TIMES_TABLE
      + " WHERE idsalle = ? ORDER BY jour";

    List<DailyTimes> times = jdbcTemplate.query(query, new RowMapper<DailyTimes>() {
      @Override
      public DailyTimes mapRow(ResultSet rs, int rowNum) throws SQLException {
        DailyTimes dt = new DailyTimes(rs.getInt(1));
        dt.setOpening(new Hour(rs.getString(2)));
        dt.setClosing(new Hour(rs.getString(3)));
        return dt;
      }
    }, roomId);

    DailyTimes[] timesArray = new DailyTimes[7];
    if (times.size() < 7) {
      for (int i = 0; i < 7; i++) {
        DailyTimes dt = new DailyTimes(i + 1);
        dt.setOpening(new Hour("00:00"));
        dt.setClosing(new Hour("24:00"));
        timesArray[i] = dt;
      }
    } else {
      timesArray = times.toArray(timesArray);
    }
    return timesArray;
  }

  List<Room> findRoomInfo(int estab) {
    String query = "SELECT s.id,s.nom,s.fonction,t.hc,t.hp FROM salle s JOIN tarifsalle t ON(s.idtarif = t.id)"
      + " WHERE s.etablissement = ? AND s.active = TRUE AND s.public = TRUE ORDER BY s.nom";

    List<Room> rooms = jdbcTemplate.query(query, new RowMapper<Room>() {
      @Override
      public Room mapRow(ResultSet rs, int i) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt(1));
        r.setName(rs.getString(2));
        r.setUsage(rs.getString(3));
        r.setOffPeakPrice(rs.getDouble(4));
        r.setFullPrice(rs.getDouble(5));
        return r;
      }

    }, estab);
    return rooms;
  }

  List<ScheduleElement> getRoomConflicts(final Booking booking) {
    List<ScheduleElement> conflicts = new ArrayList<>();
    String query = "SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.lieux,s.nom FROM " + TABLE + " p JOIN salle s ON(p.lieux = s.id)"
      + " WHERE p.jour = ?"
      + " AND p.lieux = ?"
      + " AND ((p.debut >= ? AND p.debut < ?)" // start //end
      + " OR (p.fin > ? AND p.fin <= ?)"
      + " OR (p.debut <= ? AND p.fin >= ?))";
    try {
      final Date d = Constants.DATE_FORMAT.parse(booking.getDate());
      final PreparedStatementSetter setter = new PreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
          ps.setDate(1, new java.sql.Date(d.getTime()));
          ps.setInt(2, booking.getRoom());
          ps.setTime(3, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(4, Time.valueOf(booking.getEndTime().toString() + ":00"));
          ps.setTime(5, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(6, Time.valueOf(booking.getEndTime().toString() + ":00"));
          ps.setTime(7, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(8, Time.valueOf(booking.getEndTime().toString() + ":00"));
        }
      };
      conflicts = jdbcTemplate.query(query, setter, new RowMapper<ScheduleElement>() {

        @Override
        public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
          return getConflictFromRS(rs);
        }
      });
    } catch (ParseException ex) {
      Logger.getLogger(ScheduleDao.class.getName()).log(Level.SEVERE, null, ex);
    }

    return conflicts;
  }

  List<ScheduleElement> getPersonConflicts(final Booking booking) {
    List<ScheduleElement> conflicts = new ArrayList<>();
    String query = "SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.lieux,s.nom FROM " + TABLE + " p JOIN salle s ON(p.lieux = s.id)"
      + " WHERE p.jour = ?"
      + " AND p.idper = ?"
      + " AND ((p.debut >= ? AND p.debut < ?)" // start //end
      + " OR (p.fin > ? AND p.fin <= ?)"
      + " OR (p.debut <= ? AND p.fin >= ?))";
    try {
      final Date d = Constants.DATE_FORMAT.parse(booking.getDate());
      final PreparedStatementSetter setter = new PreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
          ps.setDate(1, new java.sql.Date(d.getTime()));
          ps.setInt(2, booking.getType() == Schedule.BOOKING_GROUP ? booking.getGroup() : booking.getPerson());
          ps.setTime(3, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(4, Time.valueOf(booking.getEndTime().toString() + ":00"));
          ps.setTime(5, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(6, Time.valueOf(booking.getEndTime().toString() + ":00"));
          ps.setTime(7, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(8, Time.valueOf(booking.getEndTime().toString() + ":00"));
        }
      };
      conflicts = jdbcTemplate.query(query, setter, new RowMapper<ScheduleElement>() {

        @Override
        public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
          return getConflictFromRS(rs);
        }
      });
    } catch (ParseException ex) {
      Logger.getLogger(ScheduleDao.class.getName()).log(Level.SEVERE, null, ex);
    }

    return conflicts;
  }

  List<ScheduleElement> getBookings(int idper) {
//    List<ScheduleElement> bookings = new ArrayList<ScheduleElement>();
//    String query = "SELECT p.id,p.jour,p.debut,p.fin,p.ptype,s.nom,e.nom"
//      + " FROM " + TABLE + " p JOIN salle s ON(p.lieux = s.id)"
//      + " JOIN person e ON (e.id = s.etablissement)"
//      + " WHERE p.jour >= 'now' "
//      + " AND (p.ptype = " + Schedule.MEMBER + " AND p.idper = ?)"
//      + " OR (p.ptype = " + Schedule.GROUP + " AND p.idper IN ("
//      + "SELECT g.id FROM " + GroupIO.TABLE + " g JOIN " + GroupIO.TABLE_DET + " d ON (g.id = d.id)"
//      + " WHERE d.musicien = ?";

    String query = "SELECT p.id,p.idper,p.jour,p.debut,p.fin,p.ptype,p.lieux,s.nom,e.id,e.nom,"
      + "CASE WHEN p.ptype = " + Schedule.BOOKING_GROUP + " THEN g.nom ELSE '' END"
      + " FROM " + TABLE + " p JOIN reservation r ON (p.action = r.idaction) JOIN salle s ON(p.lieux = s.id)"
      + " JOIN personne e ON (e.id = s.etablissement)"
      + " LEFT JOIN groupe g ON (p.idper = g.id)"
      + " WHERE r.idper = ? ORDER BY p.jour,p.debut";
    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {
      @Override
      public ScheduleElement mapRow(ResultSet rs, int i) throws SQLException {
        ScheduleElement b = new ScheduleElement();
        b.setId(rs.getInt(1));
        b.setIdPerson(rs.getInt(2));
        b.setDate(rs.getDate(3));
        b.setStart(new Hour(rs.getString(4)));
        b.setEnd(new Hour(rs.getString(5)));
        b.setType(rs.getInt(6));
        b.setDetail("room" , new NamedModel(rs.getInt(7) , rs.getString(8)));
        b.setDetail("estab" , new NamedModel(rs.getInt(9) , rs.getString(10)));
        NamedModel group = Schedule.BOOKING_GROUP == b.getType() ? new NamedModel(b.getIdPerson(), rs.getString(11)) : null;
        b.setDetail("group" , group);
        return b;
      }

    }, idper);
  }

  private ScheduleElement getConflictFromRS(ResultSet rs) throws SQLException {
    ScheduleElement e = new ScheduleElement();
    e.setId(rs.getInt(1));
    e.setDate(rs.getDate(2));
    e.setStart(new Hour(rs.getString(3)));
    e.setEnd(new Hour(rs.getString(4)));
    e.setType(rs.getInt(5));
    e.setIdPerson(rs.getInt(6));
    e.setDetail("room" , new NamedModel(rs.getInt(7) , rs.getString(8)));

    return e;
  }

  @Transactional
  public void book(final Booking booking) throws ParseException {
    final Date date = Constants.DATE_FORMAT.parse(booking.getDate());
    final int action = createEmptyAction();
    booking.setAction(action);
    final int booked = createBooking(booking);
    final String sql = "INSERT INTO " + TABLE + " (jour,debut,fin,ptype,idper,action,lieux,note) VALUES(?,?,?,?,?,?,?,?)";
    jdbcTemplate.update(new PreparedStatementCreator() {

      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setDate(1, new java.sql.Date(date.getTime()));
        ps.setTime(2, Time.valueOf(booking.getStartTime().toString() + ":00"));
        ps.setTime(3, Time.valueOf(booking.getEndTime().toString() + ":00"));
        ps.setInt(4, booking.getType());
        ps.setInt(5, Schedule.BOOKING_GROUP == booking.getType() ? booking.getGroup() : booking.getPerson());
        ps.setInt(6, action);
        ps.setInt(7, booking.getRoom());
        ps.setInt(8, 0);
        return ps;
      }

    });
  }

  private int createBooking(final Booking booking) throws ParseException {
    final Date date = Constants.DATE_FORMAT.parse(booking.getDate());
    final String sql = "INSERT INTO reservation(idper,idaction,dateres,pass) VALUES(?,?,?,?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
        ps.setInt(1, booking.getPerson());
        ps.setInt(2, booking.getAction());
        ps.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
        ps.setBoolean(4, booking.isPass());
        return ps;
      }
    }, keyHolder);
    return keyHolder.getKey().intValue();

  }

  private int createEmptyAction() {
    final String sql = "INSERT INTO " + ACTION + " (cours) VALUES(?);";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
        ps.setInt(1, 0);
        return ps;
      }
    }, keyHolder);
    return keyHolder.getKey().intValue();
  }

}

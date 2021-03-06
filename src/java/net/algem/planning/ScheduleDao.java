/*
 * @(#)ScheduleDao.java	1.6.3 21/06/17
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
import net.algem.config.Instrument;
import net.algem.contact.Person;
import net.algem.room.Equipment;
import net.algem.room.Room;
import net.algem.util.AbstractGemDao;
import net.algem.util.GemConstants;
import net.algem.util.NamedModel;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * IO methods for class {@link net.algem.planning.Schedule}.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.3
 * @since 1.0.0 11/02/13
 */
@Repository
public class ScheduleDao
  extends AbstractGemDao {

  public final static String TABLE = "planning";
  /** Action table. */
  public final static String T_ACTION = "action";
  public final static String T_BOOKING = "reservation";
  public final static String T_ROOM_TIMES = "horaires";
  public final static String SEQUENCE = "planning_id_seq";
  public final static String COLUMNS = "p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.action,p.lieux,p.note";

  private final static Logger LOGGER = Logger.getLogger(ScheduleDao.class.getName());

  public void insert(Schedule p) throws SQLException {

    int id = nextId(SEQUENCE);

    String query = "INSERT INTO " + TABLE + " VALUES("
      + id
      + ",'" + p.getDateFr().toString() + "'"
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
      + "jour = '" + p.getDateFr()
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
  public List<ScheduleElement> find(Date date, int estab, boolean adminAccess) {
    String query = " SELECT p.*, c.id, c.titre, c.collectif, c.code, s.nom, per.prenom, per.nom, g.nom"
      + " FROM " + TABLE + " p INNER JOIN action a ON (p.action = a.id) LEFT OUTER JOIN cours c ON (a.cours = c.id)"
      + " LEFT OUTER JOIN personne per ON (p.idper = per.id)"
      + " LEFT OUTER JOIN groupe g ON (p.idper = g.id)"
      + " JOIN salle s ON (p.lieux = s.id)"
      + " WHERE jour = ?"
      + " AND s.etablissement = ?";
    if (!adminAccess) {
      query += " AND s.public = TRUE";
    }
    query += " ORDER BY s.nom, p.debut";
    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {

      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        String end = rs.getString(4);
        d.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        //d.setEnd(new Hour(rs.getString(4)));
        d.setType(rs.getInt(5));
        d.setIdPerson(rs.getInt(6));
        d.setIdAction(rs.getInt(7));
        d.setPlace(rs.getInt(8));
        d.setNote(rs.getInt(9));
        d.setDetail("course", new NamedModel(rs.getInt(10), rs.getString(11)));
        d.setCollective(rs.getBoolean(12));
        d.setCode(rs.getInt(13));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(14)));
        d.setDetail("estab", null);
        String name = "";
        if (Schedule.GROUP == d.getType() || Schedule.BOOKING_GROUP == d.getType() || Schedule.STUDIO == d.getType()) {
          name = rs.getString(17);
        } else {
          String firstName = rs.getString(15);
          String lastName = rs.getString(16);
          name = firstName == null ? (lastName == null ? "" : lastName) : firstName + " " + lastName;
        }
        d.setDetail("person", new NamedModel(d.getIdPerson(), name));
        if ((Schedule.COURSE == d.type && !d.isCollective()) || Schedule.ADMINISTRATIVE == d.type) {
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
    String query = " SELECT pl.*,s.texte FROM " + ScheduleRangeIO.TABLE + " pl LEFT JOIN suivi s ON pl.note = s.id WHERE pl.idplanning = ?";
    return jdbcTemplate.query(query, new RowMapper<ScheduleRange>() {

      @Override
      public ScheduleRange mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleRange r = new ScheduleRange();
        r.setId(rs.getInt(1));
        r.setScheduleId(rs.getInt(2));
        r.setStart(new Hour(rs.getString(3)));
        String end = rs.getString(4);
        r.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        //r.setEnd(new Hour(rs.getString(4)));
        r.setMemberId(rs.getInt(5));
        r.setNote(rs.getInt(6));
        String lb = rs.getString(7);
        r.setLabel(lb != null ? lb : null);

        return r;
      }
    }, id);
  }

  public DailyTimes[] find(int roomId) throws SQLException {
    String query = "SELECT jour, ouverture, fermeture FROM " + T_ROOM_TIMES
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

  Room findRoom(final int roomId) {
    String query = "SELECT nom,fonction,etablissement,idtarif FROM salle WHERE id = ?";
    return jdbcTemplate.queryForObject(query, new RowMapper<Room>() {
      @Override
      public Room mapRow(ResultSet rs, int row) throws SQLException {
        Room r = new Room();
        r.setId(roomId);
        r.setName(rs.getString(1));
        r.setUsage(rs.getString(2));
        r.setEstab(rs.getInt(3));
        r.setPriceIndex(rs.getInt(3));
        return r;
      }
    }, roomId);

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

  Room findRoomDetail(final int roomId) {
    String query = "SELECT s.nom,s.fonction,s.surf,s.npers,t.hc,t.hp FROM salle s LEFT JOIN tarifsalle t ON(s.idtarif = t.id) WHERE s.id = ?";

    Room room = jdbcTemplate.queryForObject(query, new RowMapper<Room>() {
      @Override
      public Room mapRow(ResultSet rs, int i) throws SQLException {
        Room r = new Room();
        r.setId(roomId);
        r.setName(rs.getString(1));
        r.setUsage(rs.getString(2));
        r.setSurface(rs.getShort(3));
        r.setPlaces(rs.getShort(4));
        r.setOffPeakPrice(rs.getDouble(5));
        r.setFullPrice(rs.getDouble(6));
        return r;
      }
    }, roomId);
//    Room r = findRoom(roomId);
    if (room != null) {
      room.setEquipment(findRoomEquipment(room.getId()));
    }
    return room;
  }

  private List<Equipment> findRoomEquipment(final int roomId) {
    String query = "SELECT libelle,qte,idx from sallequip WHERE idsalle = ? AND visible = TRUE ORDER BY idx";
    return jdbcTemplate.query(query, new RowMapper<Equipment>() {
     @Override
      public Equipment mapRow(ResultSet rs, int row) throws SQLException {
        Equipment e = new Equipment();
        e.setRoomId(roomId);
        e.setName(rs.getString(1));
        e.setQuantity(rs.getInt(2));
        e.setIndex(rs.getInt(3));
        return e;
      }
    }, roomId);
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
      final Date d = GemConstants.DATE_FORMAT.parse(booking.getDate());
      final PreparedStatementSetter setter = new PreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
          String end = "24:00".equals(booking.getEndTime().toString()) ? "23:59:59" : booking.getEndTime().toString() + ":00";
          ps.setDate(1, new java.sql.Date(d.getTime()));
          ps.setInt(2, booking.getRoom());
          ps.setTime(3, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(4, Time.valueOf(end));
          ps.setTime(5, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(6, Time.valueOf(end));
          ps.setTime(7, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(8, Time.valueOf(end));
        }
      };
      conflicts = jdbcTemplate.query(query, setter, new RowMapper<ScheduleElement>() {

        @Override
        public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
          return getConflictFromRS(rs);
        }
      });
    } catch (ParseException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
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
      final Date d = GemConstants.DATE_FORMAT.parse(booking.getDate());
      final PreparedStatementSetter setter = new PreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
          String end = "24:00".equals(booking.getEndTime().toString()) ? "23:59:59" : booking.getEndTime().toString() + ":00";
          ps.setDate(1, new java.sql.Date(d.getTime()));
          ps.setInt(2, booking.getType() == Schedule.BOOKING_GROUP ? booking.getGroup() : booking.getPerson());
          ps.setTime(3, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(4, Time.valueOf(end));
          ps.setTime(5, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(6, Time.valueOf(end));
          ps.setTime(7, Time.valueOf(booking.getStartTime().toString() + ":00"));
          ps.setTime(8, Time.valueOf(end));
        }
      };
      conflicts = jdbcTemplate.query(query, setter, new RowMapper<ScheduleElement>() {

        @Override
        public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
          return getConflictFromRS(rs);
        }
      });
    } catch (ParseException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }

    return conflicts;
  }

  /**
   * Gets the booking with id {@code id}.
   *
   * @param id booking's id
   * @return a booking instance
   */
  Booking getBooking(int id) {
    String query = "SELECT DISTINCT b.id,b.idper,b.idaction,b.dateres,b.pass,b.statut,p.debut FROM " + T_BOOKING + " b JOIN action a ON b.idaction = a.id JOIN " + ScheduleDao.TABLE + " p ON a.id = p.action WHERE b.id = ? LIMIT 1";
    return jdbcTemplate.queryForObject(query, new RowMapper<Booking>() {
      @Override
      public Booking mapRow(ResultSet rs, int arg1) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt(1));
        b.setPerson(rs.getInt(2));
        b.setAction(rs.getInt(3));
        java.sql.Timestamp t = rs.getTimestamp(4);
        b.setDate(GemConstants.DATE_FORMAT.format(new java.util.Date(t.getTime())));
        b.setPass(rs.getBoolean(5));
        b.setStatus(rs.getByte(6));
        b.setStartTime(new Hour(rs.getString(7)));
        return b;
      }

    }, id);
  }

  /**
   *
   * @param idper member's id
   * @param key primary key id
   * @return a list of bookings
   */
  List<BookingScheduleElement> getBookings(int idper, int key) {
    String query = "SELECT r.id,p.action,p.idper,p.jour,p.debut,p.fin,p.ptype,p.lieux,s.nom,e.id,e.nom,"
      + " CASE WHEN (p.ptype = " + Schedule.BOOKING_GROUP + " OR p.ptype = " + Schedule.GROUP + ") THEN g.nom ELSE '' END"
      + ",r.statut"
      + " FROM " + TABLE + " p JOIN " + T_BOOKING + " r ON (p.action = r.idaction) JOIN salle s ON(p.lieux = s.id)"
      + " JOIN personne e ON (e.id = s.etablissement)"
      + " LEFT JOIN groupe g ON (p.idper = g.id)"
      + " WHERE r.id < ? AND r.idper = ? ORDER BY r.id DESC, p.jour DESC,p.debut  LIMIT 10";
    return jdbcTemplate.query(query, new RowMapper<BookingScheduleElement>() {
      @Override
      public BookingScheduleElement mapRow(ResultSet rs, int i) throws SQLException {
        BookingScheduleElement b = new BookingScheduleElement();
        b.setId(rs.getInt(1));
        b.setIdAction(rs.getInt(2));
        b.setIdPerson(rs.getInt(3));
        b.setDateFr(rs.getDate(4));
        b.setStart(new Hour(rs.getString(5)));
        String end = rs.getString(6);
        b.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        b.setType(rs.getInt(7));
        b.setDetail("room", new NamedModel(rs.getInt(8), rs.getString(9)));
        b.setDetail("estab", new NamedModel(rs.getInt(10), rs.getString(11)));
        NamedModel group = (Schedule.BOOKING_GROUP == b.getType() || Schedule.GROUP == b.getType())
          ? new NamedModel(b.getIdPerson(), rs.getString(12)) : null;
        b.setDetail("group", group);
        b.setStatus(rs.getByte(13));
        return b;
      }

    }, key, idper);
  }

  private ScheduleElement getConflictFromRS(ResultSet rs) throws SQLException {
    ScheduleElement e = new ScheduleElement();
    e.setId(rs.getInt(1));
    e.setDateFr(rs.getDate(2));
    e.setStart(new Hour(rs.getString(3)));
    e.setEnd(new Hour(rs.getString(4)));
    e.setType(rs.getInt(5));
    e.setIdPerson(rs.getInt(6));
    e.setDetail("room", new NamedModel(rs.getInt(7), rs.getString(8)));

    return e;
  }

  public void book(final Booking booking) throws ParseException {
    final Date date = GemConstants.DATE_FORMAT.parse(booking.getDate());
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

  public void cancelBooking(final int action) {
    // n'annuler que si la reservation n'a pas été confirmée
    String sql = "DELETE FROM " + TABLE + " WHERE action = ?";
    jdbcTemplate.update(sql, action);
    //String sql2 = "DELETE FROM " + T_BOOKING + " WHERE idaction = ?";
    //jdbcTemplate.update(sql2, action);
    String sql2 = "UPDATE " + T_BOOKING + " SET idaction = ?, statut = ? WHERE idaction = ?";
    jdbcTemplate.update(sql2, 0, -1, action);
  }

  private int createBooking(final Booking booking) throws ParseException {
    final Date date = GemConstants.DATE_FORMAT.parse(booking.getDate());
    final String sql = "INSERT INTO reservation(idper,idaction,dateres,pass,statut) VALUES(?,?,?,?,?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
        ps.setInt(1, booking.getPerson());
        ps.setInt(2, booking.getAction());
        ps.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
        ps.setBoolean(4, booking.isPass());
        ps.setByte(5, (byte) 0);
        return ps;
      }
    }, keyHolder);
    return keyHolder.getKey().intValue();

  }

  private int createEmptyAction() {
    final String sql = "INSERT INTO " + T_ACTION + " (cours) VALUES(?);";
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

  public List<ScheduleElement> findWeekIdper(Date start, Date end, int idper) {
    List<ScheduleElement> schedules = findWeekCourse(start, end, idper);
    schedules.addAll(findWeekRehearsal(start, end, idper));
    schedules.addAll(findWeekAdministrative(start, end, idper));
    schedules.addAll(findWeekTech(start, end, idper));
    schedules.addAll(findWeekMeetings(start, end, idper));
    return schedules;
  }

  private List<ScheduleElement> findWeekRehearsal(Date start, Date end, int idper) {
    String query = "SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.action,p.lieux,p.note,s.nom"
      + " FROM " + TABLE + " p INNER JOIN salle s ON (p.lieux = s.id)"
      + " WHERE p.jour BETWEEN ? AND ?"
      + " AND (((p.ptype = " + Schedule.MEMBER + " OR p.ptype = " + Schedule.BOOKING_MEMBER + ") AND p.idper = ?)"
      + " OR ((p.ptype = " + Schedule.GROUP + " OR p.ptype = " + Schedule.BOOKING_GROUP + " OR p.ptype = " + Schedule.STUDIO + ") AND p.idper IN (SELECT id FROM groupe_det WHERE musicien = ?)))"
      + " ORDER BY p.jour, p.debut";

    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {

      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        String end = rs.getString(4);
        d.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        //d.setEnd(new Hour(rs.getString(4)));
        d.setType(rs.getInt(5));
        d.setIdPerson(rs.getInt(6));
        d.setIdAction(rs.getInt(7));
        d.setPlace(rs.getInt(8));
        d.setNote(rs.getInt(9));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(10)));
        return d;
      }
    }, start, end, idper, idper);
  }

  /**
   * Find all sessions where {@code idper} is a participant.
   * @param start starting date
   * @param end ending date
   * @param idper participant's id
   * @return a list of schedules
   */
  private List<ScheduleElement> findWeekCourse(Date start, Date end, int idper) {
    String query = "SELECT p.id,p.jour,pl.debut,pl.fin,p.ptype,p.idper,p.action,p.lieux,p.note, c.id, c.titre, c.collectif, c.code, s.nom, n.prenom, n.nom"
      + " FROM " + TABLE + " p INNER JOIN action a LEFT OUTER JOIN cours c ON (a.cours = c.id)"
      + " ON (p.action = a.id) LEFT OUTER JOIN personne n ON (p.idper = n.id), salle s, plage pl"
      + " WHERE p.lieux = s.id"
      + " AND p.ptype IN (" + Schedule.COURSE + "," + Schedule.TRAINING + "," + Schedule.WORKSHOP + ")"
      + " AND p.id = pl.idplanning"
      + " AND pl.adherent = ?"
      + " AND jour BETWEEN ? AND ?"
      + " ORDER BY p.jour, p.debut";
    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {

      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        String end = rs.getString(4);
        d.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        //d.setEnd(new Hour(rs.getString(4)));
        d.setType(rs.getInt(5));
        d.setIdPerson(rs.getInt(6));
        d.setIdAction(rs.getInt(7));
        d.setPlace(rs.getInt(8));
        d.setNote(rs.getInt(9));
        d.setDetail("course", new NamedModel(rs.getInt(10), rs.getString(11)));
        d.setCollective(rs.getBoolean(12));
        d.setCode(rs.getInt(13));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(14)));
        d.setDetail("estab", null);
        String firstName = rs.getString(15);
        String lastName = rs.getString(16);
        String name = firstName == null ? (lastName == null ? "" : lastName) : firstName + " " + lastName;
        d.setDetail("person", new NamedModel(d.getIdPerson(), name));

        return d;
      }
    }, idper, start, end);
  }

  /**
   * Find all sessions where {@code idper} is involved as teacher or administrative.
   * @param start starting date
   * @param end ending date
   * @param idper person's id
   * @return a list of schedules
   */
  private List<ScheduleElement> findWeekAdministrative(Date start, Date end, int idper) {
    String query = "SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.action,p.lieux,p.note, c.id, c.titre, c.collectif, c.code, s.nom"
      + " FROM " + TABLE + " p INNER JOIN action a LEFT OUTER JOIN cours c ON (a.cours = c.id)"
      + " ON (p.action = a.id) JOIN salle s ON (p.lieux = s.id)"
      + " WHERE p.idper = ?"
      + " AND p.ptype IN (" + Schedule.COURSE + "," + Schedule.TRAINING + "," + Schedule.WORKSHOP + "," + Schedule.ADMINISTRATIVE + ")"
      + " AND jour BETWEEN ? AND ?"
      + " ORDER BY p.jour, p.debut";
    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {

      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        String end = rs.getString(4);
        d.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        //d.setEnd(new Hour(rs.getString(4)));
        d.setType(rs.getInt(5));
        d.setIdPerson(rs.getInt(6));
        d.setIdAction(rs.getInt(7));
        d.setPlace(rs.getInt(8));
        d.setNote(rs.getInt(9));
        d.setDetail("course", new NamedModel(rs.getInt(10), rs.getString(11)));
        d.setCollective(rs.getBoolean(12));
        d.setCode(rs.getInt(13));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(14)));
        d.setDetail("estab", null);

        if (d.type == Schedule.COURSE && !d.isCollective()) {
          d.setRanges(getTimeSlots(d.getId()));
        }
        return d;
      }
    }, idper, start, end);
  }

  private List<ScheduleElement> findWeekMeetings(Date start, Date end, int idper) {
    String query = "SELECT pl.id,p.jour,pl.debut,pl.fin,p.ptype,p.idper,p.action,p.lieux,pl.note,s.nom, v.texte"
      + " FROM " + TABLE + " p JOIN plage pl ON p.id = pl.idplanning JOIN suivi v ON pl.note = v.id JOIN salle s ON p.lieux = s.id "
      + " WHERE p.ptype = (" + Schedule.ADMINISTRATIVE + ")"
      + " AND pl.adherent = ?"
      + " AND jour BETWEEN ? AND ?"
      + " ORDER BY p.jour, p.debut";
    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {

      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        String end = rs.getString(4);
        d.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        //d.setEnd(new Hour(rs.getString(4)));
        d.setType(Schedule.MEETING);
        d.setIdPerson(rs.getInt(6));
        d.setIdAction(rs.getInt(7));
        d.setPlace(rs.getInt(8));
        d.setNote(rs.getInt(9));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(10)));
        d.setDetail("course", null);
        d.setLabel(rs.getString(11));
        d.setCollective(false);
        //d.setCode(0);

        d.setDetail("estab", null);

        return d;
      }
    }, idper, start, end);
  }

  private List<ScheduleElement> findWeekTech(Date start, Date end, int idper) {
    String query = "SELECT p.id,p.jour,p.debut,p.fin,p.ptype,p.idper,p.action,p.lieux,p.note, s.nom, g.nom"
      + " FROM " + TABLE + " p INNER JOIN plage pl ON (p.id = pl.idplanning) LEFT OUTER JOIN groupe g ON (p.idper = g.id)"
      + " INNER JOIN salle s ON (p.lieux = s.id)"
      + " WHERE p.ptype = 8"
      + " AND pl.adherent = ?"
      + " AND p.jour BETWEEN ? AND ?"
      + " ORDER BY p.jour, p.debut";
    return jdbcTemplate.query(query, new RowMapper<ScheduleElement>() {

      @Override
      public ScheduleElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduleElement d = new ScheduleElement();
        d.setId(rs.getInt(1));
        d.setDateFr(new DateFr(rs.getString(2)));
        d.setStart(new Hour(rs.getString(3)));
        String end = rs.getString(4);
        d.setEnd("00:00:00".equals(end) ? new Hour("24:00") : new Hour(end));
        //d.setEnd(new Hour(rs.getString(4)));
        d.setType(rs.getInt(5));
        d.setIdPerson(rs.getInt(6));
        d.setIdAction(rs.getInt(7));
        d.setPlace(rs.getInt(8));
        d.setNote(rs.getInt(9));
        d.setDetail("room", new NamedModel(d.getPlace(), rs.getString(10)));
        d.setDetail("estab", null);
        d.setDetail("person", new NamedModel(d.getIdPerson(), rs.getString(11)));
        return d;
      }
    }, idper, start, end);
  }

  public List<ScheduleRangeElement> findScheduleDetail(int id, int ptype) {
    switch (ptype) {
      case Schedule.COURSE:
        return findCourseScheduleDetail(id, ptype);
      case Schedule.GROUP:
        return findGroupScheduleDetail(id, ptype);
      case Schedule.MEMBER:
        return findMemberScheduleDetail(id, ptype);
      case Schedule.MEETING:
        return findMeetingScheduleDetail(id, ptype);
      default:
        return findCourseScheduleDetail(id, ptype);
    }
  }

  private List<ScheduleRangeElement> findMeetingScheduleDetail(int id, int ptype) {
    String query = "SELECT DISTINCT pl.id,pl.adherent,pl.debut,pl.fin,pl.note,per.nom,per.prenom,per.pseudo,i.id,i.nom"
      + " FROM plage pl JOIN personne per ON (pl.adherent = per.id)"
      + " LEFT JOIN person_instrument pi ON (per.id = pi.idper AND pi.idx = 0 AND pi.ptype = " + getInstrumentFromScheduleType(ptype) + ")"
      + " LEFT JOIN instrument i ON (pi.instrument = i.id)"
      + " WHERE idplanning = (SELECT idplanning FROM " + ScheduleRangeIO.TABLE + " WHERE id = ?)"
      + " ORDER BY pl.debut,per.nom,per.prenom";
    return jdbcTemplate.query(query, new RowMapper<ScheduleRangeElement>() {

      @Override
      public ScheduleRangeElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getRangeFromRS(rs);
      }
    }, id);
  }

  private List<ScheduleRangeElement> findCourseScheduleDetail(int id, int ptype) {
    String query = "SELECT DISTINCT pl.id,pl.adherent,pl.debut,pl.fin,pl.note,p.nom,p.prenom,p.pseudo,i.id,i.nom"
      + " FROM plage pl JOIN personne p ON (pl.adherent = p.id)"
      + " LEFT JOIN person_instrument pi ON (p.id = pi.idper AND pi.idx = 0 AND pi.ptype = " + getInstrumentFromScheduleType(ptype) + ")"
      + " LEFT JOIN instrument i ON (pi.instrument = i.id)"
      + " WHERE idplanning = ?"
      + " ORDER BY pl.debut,p.nom,p.prenom";

    return jdbcTemplate.query(query, new RowMapper<ScheduleRangeElement>() {

      @Override
      public ScheduleRangeElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getRangeFromRS(rs);
      }
    }, id);
  }

  private List<ScheduleRangeElement> findMemberScheduleDetail(int id, int ptype) {

    String query = "SELECT DISTINCT p.id,per.id,p.debut,p.fin,p.note,per.nom,per.prenom,per.pseudo,i.id,i.nom"
      + " FROM planning p JOIN personne per ON (p.idper = per.id)"
      + " LEFT JOIN person_instrument pi ON (per.id = pi.idper AND pi.idx = 0"
      + " AND pi.ptype =  " + getInstrumentFromScheduleType(ptype) + ")"
      + " JOIN instrument i ON (pi.instrument = i.id)"
      + " WHERE p.id = ?"
      + " ORDER BY per.nom,per.prenom";
    return jdbcTemplate.query(query, new RowMapper<ScheduleRangeElement>() {

      @Override
      public ScheduleRangeElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getRangeFromRS(rs);
      }
    }, id);
  }

  private List<ScheduleRangeElement> findGroupScheduleDetail(int id, int ptype) {
    String query = "SELECT DISTINCT g.id,m.musicien,p.debut,p.fin,p.note,per.nom,per.prenom,per.pseudo,i.id,i.nom"
      + " FROM groupe g JOIN planning p ON (g.id = p.idper)"
      + " JOIN groupe_det m ON (g.id = m.id)"
      + " JOIN personne per ON (m.musicien = per.id)"
      + " LEFT JOIN person_instrument pi ON (m.musicien = pi.idper AND pi.idx = 0"
      + " AND pi.ptype =  " + getInstrumentFromScheduleType(ptype) + ")"
      + " JOIN instrument i ON (pi.instrument = i.id)"
      + " WHERE p.id = ?"
      + " ORDER BY per.nom,per.prenom";
    return jdbcTemplate.query(query, new RowMapper<ScheduleRangeElement>() {

      @Override
      public ScheduleRangeElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        return getRangeFromRS(rs);
      }
    }, id);
  }

  private ScheduleRangeElement getRangeFromRS(ResultSet rs) throws SQLException {
    ScheduleRangeElement r = new ScheduleRangeElement();
    r.setId(rs.getInt(1));
    r.setMemberId(rs.getInt(2));
    r.setStart(new Hour(rs.getString(3)));
    r.setEnd(new Hour(rs.getString(4)));
    r.setNote(rs.getInt(5));
    Person p = new Person(r.getMemberId());
    p.setName(rs.getString(6));
    p.setFirstName(rs.getString(7));
    p.setNickName(rs.getString(8));
    p.setInstrument(new Instrument(rs.getInt(9), 0, rs.getString(10)));
    r.setPerson(p);
    return r;
  }

  private int getInstrumentFromScheduleType(int type) {
    switch (type) {
      case Schedule.COURSE:
      case Schedule.ADMINISTRATIVE:
      case Schedule.MEETING:
        return Instrument.MEMBER;
      case Schedule.GROUP:
        return Instrument.MUSICIAN;
      default:
        return 1;
    }
  }
}

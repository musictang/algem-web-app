/*
 * @(#)PlanningServiceImpl.java	1.2.0 02/04/16
 *
 * Copyright (c) 2016 Musiques Tangentes. All Rights Reserved.
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

import net.algem.config.ColorPref;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.algem.config.Config;
import net.algem.config.ConfigIO;
import net.algem.config.ConfigKey;
import net.algem.contact.Person;
import net.algem.contact.PersonIO;
import net.algem.room.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for schedule operations.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.2.0
 * @since 1.0.0 11/02/13
 */
@Service
public class PlanningServiceImpl
        implements PlanningService
{

  private static final ScheduleColorizer COLORIZER = new ScheduleColorizer(new ColorPref());

  @Autowired
  private ScheduleDao scheduleDao;
  @Autowired
  private PersonIO personIO;
  @Autowired
  private ConfigIO configIO;
  @Autowired
  private MessageSource messageSource;

  @Override
  public Map<String, String> getConf() {
    Map<String, String> confs = new HashMap<>();
    Config c1 = configIO.findId(ConfigKey.OFFPEAK_HOUR.getKey());
    confs.put("offPeakTime", c1.getValue());
    Config c2 = configIO.findId(ConfigKey.START_OF_YEAR.getKey());
    confs.put("startDate", c2.getValue());
    Config c3 = configIO.findId(ConfigKey.END_OF_YEAR.getKey());
    confs.put("endDate", c3.getValue());
    Config c4 = configIO.findId(ConfigKey.PRE_ENROLMENT_START_DATE.getKey());
    confs.put("preEnrolmentStart", c4.getValue());

    return confs;
  }

  @Override
  public BookingConf getBookingConf() {
    BookingConf conf = new BookingConf();
    conf.setMinDelay(getBookingMinDelay());
    conf.setCancelDelay(getCancelBookingDelay());
    conf.setMaxDelay(getBookingMaxDelay());
    conf.setMemberShipRequired(getMemberShipConf());

    return conf;
  }

  @Override
  public int getTimeOffset() {
    Config c = configIO.findId("Heure.ouverture");
    return new Hour(c.getValue()).toMinutes();
  }

  @Override
  public DailyTimes getDailyTimes(int room, int dow) {
    DailyTimes[] dailyTimes = findDailyTimes(room);
    return dailyTimes == null || dailyTimes.length == 0 ? null : dailyTimes[dow - 1];
  }

  @Override
  public Map<Integer, Config> getDefaultColorCodes() {
    return COLORIZER.getDefColorCodes();
  }

  @Override
  public Room getRoom(int roomId) {
    return scheduleDao.findRoom(roomId);
  }

  @Override
  public List<Room> getRoomInfo(int estab) {
    return scheduleDao.findRoomInfo(estab);
  }

  @Override
  public List<Person> getEstablishments(String where) {
    return personIO.findEstablishments(where);
  }

  /**
   * Returns a map associating room's id with the list of the date's schedules.
   *
   * @param date selected date
   * @param estab establishment number
   * @return a map
   */
  @Override
  public Map<Integer, Collection<ScheduleElement>> getDaySchedule(Date date, int estab) {
    Map<Integer, Collection<ScheduleElement>> map = new HashMap<Integer, Collection<ScheduleElement>>();
    int place = -1;
    for (ScheduleElement d : scheduleDao.find(date, estab)) {
      d.setLabel(getHtmlTitle(d));
      d.setColor(ScheduleColorizer.colorToHex(COLORIZER.getColor(d)));
      d.setLabelColor(ScheduleColorizer.colorToHex(COLORIZER.getTextColor(d)));
      if (d.getPlace() != place) {
        place = d.getPlace();
        List<ScheduleElement> elements = new ArrayList<ScheduleElement>();
        elements.add(d);

        List<ScheduleElement> closed = getClosed(place, date);
        if (closed.size() > 0) {
          elements.addAll(closed);
        }
        map.put(place, elements);
      } else {
        map.get(place).add(d);
      }
    }
    return map;
  }

  public Map<Integer, Collection<ScheduleElement>> getWeekSchedule(Date start, Date end, int idper) {
    Map<Integer, Collection<ScheduleElement>> map = new LinkedHashMap<Integer, Collection<ScheduleElement>>();
    int p = -1;
    Calendar cal = Calendar.getInstance();
    map.put(Calendar.MONDAY, new ArrayList<ScheduleElement>());
    map.put(Calendar.TUESDAY, new ArrayList<ScheduleElement>());
    map.put(Calendar.WEDNESDAY, new ArrayList<ScheduleElement>());
    map.put(Calendar.THURSDAY, new ArrayList<ScheduleElement>());
    map.put(Calendar.FRIDAY, new ArrayList<ScheduleElement>());
    map.put(Calendar.SATURDAY, new ArrayList<ScheduleElement>());
    map.put(Calendar.SUNDAY, new ArrayList<ScheduleElement>());

    for (ScheduleElement d : scheduleDao.findWeek(start, end, idper)) {
      d.setLabel(getHtmlTitle(d));
      d.setColor(ScheduleColorizer.colorToHex(COLORIZER.getColor(d)));
      d.setLabelColor(ScheduleColorizer.colorToHex(COLORIZER.getTextColor(d)));
      cal.setTime(d.getDate());
      int dow = cal.get(Calendar.DAY_OF_WEEK);
      map.get(dow).add(d);
    }

    return map;
  }

  @Override
  public Map<Room, Collection<ScheduleElement>> getFreePlace(Date date, int estab) {
    Map<Room, Collection<ScheduleElement>> map = new TreeMap<Room, Collection<ScheduleElement>>();
    List<Room> rooms = scheduleDao.getFreeRoom(date, estab);
    for (Room r : rooms) {
      List<ScheduleElement> closed = getClosed(r.getId(), date);
      map.put(r, closed);
    }
    return map;
  }

  /**
   * Gets the list of schedules conflicting with this {@code booking}.
   *
   * @param booking desired booking
   * @return a list of schedules or an empty list if no conflict was detected
   */
  @Override
  public List<ScheduleElement> getRoomConflicts(Booking booking) {
    return scheduleDao.getRoomConflicts(booking);
  }

  @Override
  public List<ScheduleElement> getPersonConflicts(Booking booking) {
    return scheduleDao.getPersonConflicts(booking);
  }

  @Override
  public Booking getBooking(int id) {
    return scheduleDao.getBooking(id);
  }

  @Override
  public List<BookingScheduleElement> getBookings(int idper) {
    return scheduleDao.getBookings(idper);
  }

  @Override
  @Transactional
  public void book(Booking booking) throws ParseException {
    scheduleDao.book(booking);
  }

  @Override
  @Transactional
  public boolean cancelBooking(int action) {
    try {
      scheduleDao.cancelBooking(action);
      return true;
    } catch (DataAccessException e) {
      Logger.getLogger(PlanningServiceImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
      return false;
    }
  }

  private int getBookingMinDelay() {
    try {
      return Integer.parseInt(configIO.findId("Reservation.delai.min").getValue());
    } catch (NumberFormatException nfe) {
      System.err.println(nfe.getMessage());
      return 24;
    }
  }

  private int getBookingMaxDelay() {
    try {
      return Integer.parseInt(configIO.findId("Reservation.delai.max").getValue());
    } catch (NumberFormatException nfe) {
      System.err.println(nfe.getMessage());
      return 30;
    }
  }

  private int getCancelBookingDelay() {
    try {
      return Integer.parseInt(configIO.findId("Reservation.annulation.delai").getValue());
    } catch (NumberFormatException nfe) {
      System.err.println(nfe.getMessage());
      return 24;
    }
  }

  private boolean getMemberShipConf() {
    Config c = configIO.findId(ConfigKey.BOOKING_REQUIRED_MEMBERSHIP.getKey());
    return c.getValue().toLowerCase().startsWith("t");
  }

  private DailyTimes[] findDailyTimes(int roomId) {
    try {
      return scheduleDao.find(roomId);
    } catch (SQLException ex) {
      return getDefaultDailyTimes();
    }
  }

  private List<ScheduleElement> getClosed(int room, int dow) {
    List<ScheduleElement> closed = new ArrayList<ScheduleElement>();
    Hour first = new Hour(getTimeOffset());
    Hour last = new Hour("24:00");

    DailyTimes dt = getDailyTimes(room, dow);
    Hour start = dt.getOpening();
    Hour end = dt.getClosing();

    if (start != null && start.toMinutes() > first.toMinutes()) {
      ScheduleElement s = new ScheduleElement();
      s.setType(Schedule.ROOM);
      s.setStart(first);
      s.setEnd(start);
      closed.add(s);
    }

    if (end != null && end.toMinutes() < 1440) {
      ScheduleElement e = new ScheduleElement();
      e.setType(Schedule.ROOM);
      e.setStart(end.toString().equals(Hour.NULL_HOUR) ? first : end);
      e.setEnd(last);
      closed.add(e);
    }

    return closed;
  }

  private List<ScheduleElement> getClosed(int room, Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int dow = cal.get(Calendar.DAY_OF_WEEK);
    return getClosed(room, dow);
  }

  /**
   * Default opening times.
   *
   * @return an array of daily times
   */
  private DailyTimes[] getDefaultDailyTimes() {
    DailyTimes[] timesArray = new DailyTimes[7];

    for (int i = 0; i < 7; i++) {
      DailyTimes dt = new DailyTimes(i + 1);
      dt.setOpening(new Hour("00:00"));
      dt.setClosing(new Hour("24:00"));
      timesArray[i] = dt;
    }
    return timesArray;
  }

  /**
   * Gets the label of the schedule {@code e} corresponding to its type.
   * The label is html formatted.
   *
   * @return a html string representing the schedule element
   */
  private String getHtmlTitle(ScheduleElement e) {
    Locale locale = LocaleContextHolder.getLocale();
    String t = "";
    switch (e.getType()) {
      case Schedule.COURSE:
      case Schedule.WORKSHOP:
      case Schedule.TRAINING:
        t = e.getDetail().get("course").getName();
        break;
      case Schedule.GROUP:
        t = messageSource.getMessage("group.rehearsal.title", null, locale);
        break;
      case Schedule.MEMBER:
        t = messageSource.getMessage("member.rehearsal.title", null, locale);
        break;
      case Schedule.BOOKING_GROUP:
        t = messageSource.getMessage("booking.group.title", null, locale);
        break;
      case Schedule.BOOKING_MEMBER:
        t = messageSource.getMessage("booking.member.title", null, locale);
        break;
      default:
        t = "";
    }
    return t.toUpperCase() + "<br />" + e.getStart() + "-" + e.getEnd();
  }

}

/*
 * @(#)PlanningServiceImpl.java	1.0.5 14/09/15
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

import net.algem.config.ColorPref;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import net.algem.config.Config;
import net.algem.config.ConfigIO;
import net.algem.config.ConfigKey;
import net.algem.contact.Person;
import net.algem.contact.PersonIO;
import net.algem.room.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Service class for schedule operations.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.5
 * @since 1.0.0 11/02/13
 */
@Component
public class PlanningServiceImpl implements PlanningService
{

  private static final ScheduleColorizer colorizer = new ScheduleColorizer(new ColorPref());

  @Autowired
  private ScheduleDao scheduleIO;
  @Autowired
  private PersonIO personIO;
  @Autowired
  private ConfigIO configIO;
  @Autowired
  private MessageSource messageSource;

  public void setScheduleIO(ScheduleDao scheduleIO) {
    this.scheduleIO = scheduleIO;
  }

  public void setPersonIO(PersonIO personIO) {
    this.personIO = personIO;
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
    for (ScheduleElement d : scheduleIO.find(date, estab)) {
      d.setLabel(getHtmlTitle(d));
      d.setColor(ScheduleColorizer.colorToHex(colorizer.getColor(d)));
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

  @Override
  public Map<Room, Collection<ScheduleElement>> getFreePlace(Date date, int estab) {
    Map<Room, Collection<ScheduleElement>> map = new TreeMap<Room, Collection<ScheduleElement>>();
    List<Room> rooms = scheduleIO.getFreeRoom(date, estab);
    for (Room r : rooms) {
      List<ScheduleElement> closed = getClosed(r.getId(), date);
      map.put(r, closed);
    }
    return map;
  }

  @Override
  public List<Room> getRoomInfo(int estab) {
    return scheduleIO.findRoomInfo(estab);
  }

  /**
   * Get the global opening time.
   * @return a time length in minutes
   */
  @Override
  public int getTimeOffset() {
    Config c = configIO.findId("Heure.ouverture");
    return new Hour(c.getValue()).toMinutes();
  }

  @Override
  public int getBookingDelay() {
    try {
      return Integer.parseInt(configIO.findId("Reservation.delai").getValue());
    } catch (NumberFormatException nfe) {
      System.err.println(nfe.getMessage());
      return 24;
    }
  }

  public Map<String,String> getConf() {
    Map<String,String> confs = new HashMap<>();
//    Config c1 = configIO.findId(ConfigKey.START_TIME.getKey());
//    confs.put("startTime", c1.getValue());
//
//    Config c2 = configIO.findId(ConfigKey.BOOKING_DELAY.getKey());
//    try {
//      Integer.parseInt(c2.getValue());
//    } catch (NumberFormatException nfe) {
//      System.err.println(nfe.getMessage());
//      c2.setValue("24");
//    }
//    confs.put("bookingDelay", c2.getValue());
    Config c3 = configIO.findId(ConfigKey.OFFPEAK_HOUR.getKey());
    confs.put("offPeakTime", c3.getValue());
    Config c4 = configIO.findId(ConfigKey.END_YEAR.getKey());
    confs.put("endDate", c4.getValue());

    return confs;
  }

  @Override
  public int getCancelBookingDelay() {
    try {
      return Integer.parseInt(configIO.findId("Annulation.delai").getValue());
    } catch (NumberFormatException nfe) {
      System.err.println(nfe.getMessage());
      return 24;
    }
  }

  @Override
  public DailyTimes getDailyTimes(int room, int dow) {
    DailyTimes[] dailyTimes = findDailyTimes(room);
    return dailyTimes == null || dailyTimes.length == 0 ? null : dailyTimes[dow-1];
  }

  /**
   * Gets the list of schedules conflicting with this {@code booking}.
   * @param booking desired booking
   * @return a list of schedules or an empty list if no conflict was detected
   */
  @Override
  public List<ScheduleElement> getRoomConflicts(Booking booking) {
    return scheduleIO.getRoomConflicts(booking);
  }

   @Override
  public List<ScheduleElement> getPersonConflicts(Booking booking) {
    return scheduleIO.getPersonConflicts(booking);
  }

  /**
   * Gets the list of establishments in the organization.
   *
   * @return a list of persons' instances of type {@value Person#ESTABLISHMENT}
   */
  @Override
  public List<Person> getEstablishments(String where) {
    return personIO.findEstablishments(where);
  }

  @Override
  public void book(Booking booking) throws ParseException {
    scheduleIO.book(booking);
  }

  private DailyTimes[] findDailyTimes(int roomId) {
    try {
      return scheduleIO.find(roomId);
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
   * @return an array of daily times
   */
  private DailyTimes[] getDefaultDailyTimes() {
    DailyTimes[] timesArray = new DailyTimes[7];

    for (int i = 0 ; i < 7 ; i++) {
      DailyTimes dt = new DailyTimes(i+1);
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
        t = e.getCourseName();
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

/*
 * @(#) PlanningService.java Algem Web App 1.1.0 15/02/16
 *
 * Copyright (c) 2015 Musiques Tangentes. All Rights Reserved.
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

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.algem.config.Config;
import net.algem.contact.Person;
import net.algem.room.Room;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.0.6 27/01/2016
 */
public interface PlanningService
{

  /**
   * Gets common config.
   *
   * @return a map
   */
  Map<String, String> getConf();

  /**
   * Gets booking configuration.
   *
   * @return an instance of BookingConf
   */
  BookingConf getBookingConf();

  /**
   * Get the global opening time.
   * Ex. 08:00 = 480
   *
   * @return a time length in minutes
   */
  int getTimeOffset();

  DailyTimes getDailyTimes(int room, int dow);
  
  /**
   * Gets the default color codes.
   *
   * @return a map
   */
  Map<Integer, Config> getDefaultColorCodes();
  
  /**
   * Get details about this room {@code roomId}.
   * @param roomId room id
   * @return an instance of Room
   */
  public Room getRoom(int roomId);

  /**
   * Gets the list of public rooms in {@code estab}.
   *
   * @param estab id of the establishment
   * @return a list of rooms
   */
  List<Room> getRoomInfo(int estab);

  /**
   * Gets the list of establishments in the organization.
   *
   * @param where
   * @return a list of persons' instances of type {@value Person#ESTABLISHMENT}
   */
  List<Person> getEstablishments(String where);
  
  /**
   * Returns a map associating room's id with the list of the date's schedules.
   *
   * @param date selected date
   * @param estab establishment number
   * @return a map
   */
  Map<Integer, Collection<ScheduleElement>> getDaySchedule(Date date, int estab);

  /**
   * Gets the list of free rooms at the date {@code date} in the establishment {@code estab}.
   *
   * @param date date of search
   * @param estab establishment number
   * @return a map
   */
  Map<Room, Collection<ScheduleElement>> getFreePlace(Date date, int estab);

  /**
   * Gets the list of schedules conflicting with this {@code booking}.
   *
   * @param booking desired booking
   * @return a list of schedules or an empty list if no conflict was detected
   */
  List<ScheduleElement> getRoomConflicts(Booking booking);

  /**
   * Gets the list of schedules conflicting with the person in {@code booking}.
   *
   * @param booking the booking to check
   * @return a list of schedules or an empty list if no conflict was detected
   */
  List<ScheduleElement> getPersonConflicts(Booking booking);
  
  Booking getBooking(int id);

  /**
   * Gets all the bookings of the person with id {@code idper}.
   *
   * @param idper person's id
   * @return a list of BookingScheduleElement
   */
  List<BookingScheduleElement> getBookings(int idper);


  /**
   * Register booking.
   *
   * @param booking the booking to save
   * @throws ParseException when error in date format
   */
  void book(Booking booking) throws ParseException;

  /**
   * Cancel a booking.
   *
   * @param action schedule action to cancel
   * @return true if no exception was thrown
   */
  public boolean cancelBooking(int action);

}

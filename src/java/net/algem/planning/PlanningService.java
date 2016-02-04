/*
 * @(#) PlanningService.java Algem Web App 1.1.0 28/01/16
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
import java.util.HashMap;
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
public interface PlanningService {

//  int getBookingDelay();

//  int getCancelBookingDelay();

  BookingConf getBookingConf();
  /**
   * Gets the list of schedules conflicting with this {@code booking}.
   * @param booking desired booking
   * @return a list of schedules or an empty list if no conflict was detected
   */
  List<ScheduleElement> getRoomConflicts(Booking booking);

  List<ScheduleElement> getPersonConflicts(Booking booking);

  List<ScheduleElement> getBookings(int idper);

  DailyTimes getDailyTimes(int room, int dow);

  /**
   * Returns a map associating room's id with the list of the date's schedules.
   *
   * @param date selected date
   * @param estab establishment number
   * @return a map
   */
  Map<Integer, Collection<ScheduleElement>> getDaySchedule(Date date, int estab);

  /**
   * Gets the list of establishments in the organization.
   *
   * @param where
   * @return a list of persons' instances of type {@value Person#ESTABLISHMENT}
   */
  List<Person> getEstablishments(String where);

  	/**
	 * Gets the list of free rooms at the date {@code date} in the establishment {@code estab}.
	 * @param date date of search
	 * @param estab establishment number
	 * @return a map
	 */
  Map<Room, Collection<ScheduleElement>> getFreePlace(Date date, int estab);

  List<Room> getRoomInfo(int estab);

  /**
   * Get the global opening time.
   * @return a time length in minutes
   */
  int getTimeOffset();

  void book(Booking booking) throws ParseException;
  
  public boolean cancelBooking(int action);

  Map<Integer,Config> getDefaultColorCodes();

}

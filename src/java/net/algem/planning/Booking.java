/*
 * @(#) Booking.java Algem Web App 1.0.6 24/01/2016
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

import java.util.Date;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.0.6
 * @since 1.0.6 24/01/2016
 */
public class Booking {

  private int type;
  private int person;
  private int group;
  private int room;
  private Date date;
  private Hour startTime;
  private Hour endTime;

  public int getPerson() {
    return person;
  }

  public void setPerson(int person) {
    this.person = person;
  }

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

  public int getRoom() {
    return room;
  }

  public void setRoom(int room) {
    this.room = room;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Hour getStartTime() {
    return startTime;
  }

  public void setStartTime(Hour startTime) {
    this.startTime = startTime;
  }

  public Hour getEndTime() {
    return endTime;
  }

  public void setEndTime(Hour endTime) {
    this.endTime = endTime;
  }

  public boolean isValid() {
    return endTime.after(startTime) && room > 0 && date.after(new Date());
  }

  @Override
  public String toString() {
    return "Booking{" + "type=" + type + ", person=" + person + ", group=" + group + ", room=" + room + ", date=" + date + ", startTime=" + startTime + ", endTime=" + endTime + '}';
  }

}

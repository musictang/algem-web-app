/*
 * @(#) Booking.java Algem Web App 1.1.0 15/02/16
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

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.0.6 24/01/2016
 */
public class Booking
{

  public static final byte PENDING = 0;
  public static final byte CONFIRMED = 1;
  public static final byte CANCELLED = 2;
  private int id;
  private int type;
  private int person;
  private int group;
  private int room;
  private int action;
  /** Time length in hours. Decimal format : 1.5 = 90 min. */
  private float timeLength;
  private String date;
  private Hour startTime;
  private Hour endTime;
  private boolean pass;
  private byte status;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getPerson() {
    return person;
  }

  public void setPerson(int person) {
    this.person = person;
  }

  public int getAction() {
    return action;
  }

  public void setAction(int action) {
    this.action = action;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public boolean isPass() {
    return pass;
  }

  public void setPass(boolean pass) {
    this.pass = pass;
  }

  public byte getStatus() {
    return status;
  }

  public void setStatus(byte status) {
    this.status = status;
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

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public float getTimeLength() {
    return timeLength;
  }

  public void setTimeLength(float timeLength) {
    this.timeLength = timeLength;
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
    return true;
//    return endTime.after(startTime) && room > 0 && date.after(new Date());
  }

  @Override
  public String toString() {
    return "Booking{" + "type=" + type + ", person=" + person + ", group=" + group + ", room=" + room + ", date=" + date + ", startTime=" + startTime + ", endTime=" + endTime + '}';
  }

}

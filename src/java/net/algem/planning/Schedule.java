/*
 * @(#)Schedule.java	1.2.0 02/04/16
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
 *
 */
package net.algem.planning;

import java.util.Calendar;
import java.util.Date;

/**
 * Schedule object model.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.2.0
 * @since 1.0.0 11/02/13
 */
public class Schedule
        implements java.io.Serializable
{

  public static final int ROOM = 0;
  public static final int COURSE = 1;
  public static final int ACTION = 2;
  public static final int GROUP = 3;
  public static final int MEMBER = 4;
  public static final int WORKSHOP = 5;
  public static final int TRAINING = 6;
  public static final int STUDIO = 7;
  public static final int TECH = 8;
  public static final int ADMINISTRATIVE = 9;
  public static final int BOOKING_GROUP = 13;
  public static final int BOOKING_MEMBER = 14;
  
  private static final Calendar CAL = Calendar.getInstance();
  private static final long serialVersionUID = 1L;

  protected int id;
  protected DateFr date;
  protected Hour start;
  protected Hour end;
  protected int type;
  protected int idper;
  protected int idAction;
  protected int place;
  protected int note;
//  protected List<ScheduleRange> ranges;

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Schedule other = (Schedule) obj;
    if (this.id != other.id) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + this.id;
    return hash;
  }

  @Override
  public String toString() {
    //return "Planning:" + date + " " + start + " " + end + " " + idper + " " + idAction + " " + place;
    return date + " : " + start + "-" + end;
  }

  public int getId() {
    return id;
  }

  public void setId(int i) {
    id = i;
  }

  public void setDateFr(DateFr d) {
    date = d;
  }

  public void setDateFr(java.util.Date d) {
    date = new DateFr(d);
  }

  public DateFr getDateFr() {
    return date;
  }

  public Date getDate() {
//    CAL.setTime(date.getDate());
//    CAL.set(Calendar.HOUR_OF_DAY, 12);
//    CAL.set(Calendar.MINUTE, 0);
//    return CAL.getTime();
    return date.getDate();
  }

  public void setStart(Hour h) {
    start = h;
  }

  public Hour getStart() {
    return start;
  }

  public void setEnd(Hour h) {
    end = h;
  }

  public Hour getEnd() {
    return end;
  }

  public void setType(int i) {
    type = i;
  }

  public int getType() {
    return type;
  }

  public void setIdPerson(int i) {
    idper = i;
  }

  public int getIdPerson() {
    return idper;
  }

  public void setIdAction(int i) {
    idAction = i;
  }

  public int getIdAction() {
    return idAction;
  }

  /**
   * Sets the schedule location.
   * @param i room id
   */
  public void setPlace(int i) {
    place = i;
  }

  /**
   * Gets the schedule location.
   * @return an integer
   */
  public int getPlace() {
    return place;
  }

  public void setNote(int i) {
    note = i;
  }

  public int getNote() {
    return note;
  }

//	public List<ScheduleRange> getRanges() {
//		return ranges;
//	}
//
//	public void setRanges(List<ScheduleRange> ranges) {
//		this.ranges = ranges;
//	}

//  public boolean isValid() {
//    return true;
//  }
//
//  public static String attribFromLabel(String label) {
//    String attrib = label;
//    if (label.equals("prof")) {
//      attrib = "idper";
//    } else if (label.equals("d.prof")) {
//      attrib = "d.idper";
//    } else if (label.equals("salle")) {
//      attrib = "lieux";
//    } else if (label.equals("d.salle")) {
//      attrib = "d.lieux";
//    } else if (label.equals("cours")) {
//      attrib = "action";
//    } else if (label.equals("d.cours")) {
//      attrib = "d.action";
//    } else if (label.equals("atelier")) {
//      attrib = "atelier";
//    } else if (label.equals("d.atelier")) {
//      attrib = "d.atelier";
//    }
//    return attrib;
//  }
}

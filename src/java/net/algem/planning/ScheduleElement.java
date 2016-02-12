/*
 * @(#)ScheduleElement.java	1.1.0 28/01/16
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.algem.util.NamedModel;

/**
 * Schedule element representation.
 * This class is used in calendar to display a time slot with label, position and time.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.0.0 11/02/13
 */
public class ScheduleElement
        extends Schedule
{

  private static final long serialVersionUID = 1L;

  private Map<String,NamedModel> detail = new HashMap<>();
  private boolean collective;
  private Collection<ScheduleRange> ranges;
  private String label;
  private String color;

  public Map<String, NamedModel> getDetail() {
    return detail;
  }

  public void setDetail(String key, NamedModel m) {
    detail.put(key, m);
  }

  /**
   * Specifies whether the schedule is collective.
   * @return true if collective
   */
  public boolean isCollective() {
    return collective;
  }

  /**
   * Sets the collective status of schedule.
   * @param collective
   */
  public void setCollective(boolean collective) {
    this.collective = collective;
  }

  /**
   * Gets the schedule's label.
   * @return a string
   */
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ScheduleElement other = (ScheduleElement) obj;
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    return hash;
  }

  /**
   * Gets the differents time slots included in schedule (if not collective).
   *
   * @return a collection of time slots
   */
  public Collection<ScheduleRange> getRanges() {
    return ranges;
  }

  /**
   * Sets the differents time slots included in schedule (if not collective).
   *
   * @param ranges
   */
  public void setRanges(Collection<ScheduleRange> ranges) {
    this.ranges = ranges;
  }

  /**
   * Gets the beginning time in minutes.
   * @return a number of minutes
   */
  public int getMinutes() {
    return start.toMinutes();
  }

  /**
   * Gets the element's length.
   *
   * @return a duration in minutes
   */
  public int getLength() {
    return start.getLength(end);
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  @Override
  public String toString() {
    return (detail.get("estab") == null ? "" : detail.get("estab") + ", ") + (detail.get("room") == null ? "" : detail.get("room") + ", ") + super.toString();
  }

  /**
   * Gets the color value corresponding to the schedule's type.
   *
   * @return a string in hex format
   */
  public String getHtmlColor() {
    String prefix = "#";
    if (type == Schedule.ROOM) return prefix + "CCC";
    return color;
  }

}

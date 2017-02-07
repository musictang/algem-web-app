/*
 * @(#) ScheduleDoc.java Algem Web App 1.6.0 03/02/17
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
 */

package net.algem.planning;

import java.util.Date;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.0
 * @since 1.6.0 01/02/17
 */
public class ScheduleDoc {

  private int id;
  private Date firstDate;
  private int actionId;
  private int scheduleId;
  private int memberId;
  private String name;
  private short docType;
  private String uri;

  public ScheduleDoc() {
  }
  
  public ScheduleDoc(int id, int actionId) {
    this.id = id;
    this.actionId = actionId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getFirstDate() {
    return firstDate;
  }

  public void setFirstDate(Date firstDate) {
    this.firstDate = firstDate;
  }

  public int getActionId() {
    return actionId;
  }

  public void setActionId(int actionId) {
    this.actionId = actionId;
  }

  public int getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(int scheduleId) {
    this.scheduleId = scheduleId;
  }

  public int getMemberId() {
    return memberId;
  }

  public void setMemberId(int memberId) {
    this.memberId = memberId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public short getDocType() {
    return docType;
  }

  public void setDocType(short docType) {
    this.docType = docType;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  @Override
  public String toString() {
    return "ScheduleDoc{" + "id=" + id + ", firstDate=" + firstDate + ", actionId=" + actionId + ", scheduleId=" + scheduleId + ", memberId=" + memberId + ", name=" + name + ", docType=" + docType + ", uri=" + uri + '}';
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + this.id;
    hash = 53 * hash + this.actionId;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ScheduleDoc other = (ScheduleDoc) obj;
    if (this.id != other.id) {
      return false;
    }
    if (this.actionId != other.actionId) {
      return false;
    }
    return true;
  }

}

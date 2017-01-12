/*
 * @(#) Postit.java Algem Web App 1.5.2 11/01/2017
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

package net.algem.util;

import java.util.Date;

/**
 * Postit.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.5.2
 * @since 1.5.2 11/01/2017
 */
public class Postit
        implements java.io.Serializable
{

  public static final int INTERNAL = 0;
  public static final int INTERNAL_URGENT = 1;
  public static final int EXTERNAL = 2;
  public static final int TEACHERS = -2;

  private static final long serialVersionUID = -4295824851261772842L;

  private int id;

  /** Type : notes/urgent. */
  private int type;

  private int issuer;

  /** Receiver : private / public. */
  private int receiver;

  private Date day;

  /** Term. */
  private Date term;

  private String text;

  @Override
  public String toString() {
    return id + " " + day + " " + receiver + " " + text;
  }

  public int getId() {
    return id;
  }

  public void setId(int i) {
    id = i;
  }

  public int getType() {
    return type;
  }

  public void setType(int i) {
    type = i;
  }

  public int getReceiver() {
    return receiver;
  }

  public void setReceiver(int i) {
    receiver = i;
  }

  public int getIssuer() {
    return issuer;
  }

  public void setIssuer(int i) {
    issuer = i;
  }

  public Date getDay() {
    return day;
  }

  public void setDay(Date d) {
    day = d;
  }

  public Date getTerm() {
    return term;
  }

  public void setTerm(Date d) {
    term = d;
  }

  public String getText() {
    return text;
  }

  public void setText(String t) {
    text = t;
  }

}

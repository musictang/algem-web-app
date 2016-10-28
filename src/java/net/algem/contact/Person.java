/*
 * @(#)Person.java	1.5.0 28/10/16
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
package net.algem.contact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.algem.config.Instrument;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.5.0
 * @since 1.0.0 11/02/13
 */
public class Person
{

  public static final short PERSON = 1;
  public static final short GROUP = 3;
  public static final short ROOM = 4;
  public static final short ESTABLISHMENT = 5;
  public static final short BANK = 6;

  private int id;
  private int type;
  private String name;
  private String firstName;
  private String nickName;
  private List<Email> emails;
  private List<Tel> tels;
  private String photo;
  private Instrument instrument;
  private Map<Integer,List<Instrument>> instruments = new HashMap<Integer, List<Instrument>>();

  public Person() {
  }

  public Person(int id) {
    this(id, "");
  }

  public Person(String name) {
    this(0, name);
  }

  public Person(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstname) {
    this.firstName = firstname;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  public List<Email> getEmails() {
    return emails;
  }

  public void setEmails(List<Email> email) {
    this.emails = email;
  }

  public List<Tel> getTels() {
    return tels;
  }

  public void setTels(List<Tel> tels) {
    this.tels = tels;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public Map<Integer, List<Instrument>> getInstruments() {
    return instruments;
  }

  public void setInstruments(Map<Integer, List<Instrument>> instruments) {
    this.instruments = instruments;
  }

  public Instrument getInstrument(int type, int id) {
    List<Instrument> li = instruments.get(type);
    if (li != null && li.size() > 0) {
      for (Instrument i : li) {
        if (i.getId() == id) {
          return i;
        }
      }
    }
    return null;
  }

  public void setInstrument(int type, List<Instrument> li) {
    this.instruments.put(type, li);
  }

  /**
   * Get photo id as base64 string.
   * @return date base64-encoded or null if no data
   */
  public String getPhoto() {
    return photo;
  }

  public void setPhoto(String photo) {
    this.photo = photo;
  }

  @Override
  public String toString() {
    if (Person.PERSON == type || Person.ROOM == type) {
      return firstName == null ? "" : firstName + " " + name;
    }
    return name;
  }

}

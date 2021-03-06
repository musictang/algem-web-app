/*
 * @(#)Room.java	1.6.0 13/02/17
 *
 * Copyright (c) 2016-2017 Musiques Tangentes. All Rights Reserved.
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
package net.algem.room;

import java.util.List;

/**
 * Room object model.
 * A room is a person of type {@link net.algem.contact.Person#ROOM}.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.0
 * @since 1.0.1 06/03/13
 *
 */
public class Room
  implements Comparable<Room>
{

  private int id;
  private String name;
  private int estab;
  private short surface;
  private short places;
  private boolean active;
  /** Main usage description. */
  private String usage;
  /** Price index. */
  private int priceIndex;
  private double offPeakPrice;
  private double fullPrice;
  private List<Equipment> equipment;

  /** Public access and available for rehearsals. */
  private boolean available;

  public Room() {
    this(0, "aucune");
  }

  public Room(int n) {
    this(n, "");
  }

  public Room(String n) {
    this(0, n);
  }

  public Room(int i, String n) {
    id = i;
    name = n;
  }

  public List<Equipment> getEquipment() {
    return equipment;
  }

  public void setEquipment(List<Equipment> equipment) {
    this.equipment = equipment;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Room other = (Room) obj;
    if (this.id != other.id) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 61 * hash + this.id;
    return hash;
  }

  public void setId(int i) {
    id = i;
  }

  public int getId() {
    return id;
  }

  public void setName(String s) {
    name = s;
  }

  public String getName() {
    return name;
  }

  public void setEstab(int i) {
    estab = i;
  }

  public int getEstab() {
    return estab;
  }

  public void setActive(boolean n) {
    active = n;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public String getUsage() {
    return usage;
  }

  public void setUsage(String usage) {
    this.usage = usage;
  }

  public short getSurface() {
    return surface;
  }

  public void setSurface(short surface) {
    this.surface = surface;
  }

  public short getPlaces() {
    return places;
  }

  public void setPlaces(short places) {
    this.places = places;
  }

  /**
   * @return the priceIndex
   */
  public int getPriceIndex() {
    return priceIndex;
  }

  /**
   * @param idx the priceIndex to set
   */
  public void setPriceIndex(int idx) {
    this.priceIndex = idx;
  }

  public double getOffPeakPrice() {
    return offPeakPrice;
  }

  public void setOffPeakPrice(double offPeakPrice) {
    this.offPeakPrice = offPeakPrice;
  }

  public double getFullPrice() {
    return fullPrice;
  }

  public void setFullPrice(double fullPrice) {
    this.fullPrice = fullPrice;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Checks if this room is used to catching up with lessons when a teacher is absent.
   *
   * @return true if catching up
   */
  public boolean isCatchingUp() {
    // TODO test should not apply to room's name
    String regex = "(?iu).*(RATTRAP|CATCHING).*";
    return name.matches(regex);
  }

  @Override
  public int compareTo(Room o) {
    return this.getName().compareTo(o.getName());
  }

}

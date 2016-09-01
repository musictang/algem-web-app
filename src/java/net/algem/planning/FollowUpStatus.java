/*
 * @(#) FollowUpStatus.java Algem Web App 1.4.2 31/08/2016
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
 */

package net.algem.planning;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.4.2
 * @since 1.4.2 31/08/2016
 */
public enum FollowUpStatus {

  PRE(0, "present.label"),
  ABS(1, "absent.label"),
  EXC(2, "excused.label");

  private final int id;
  private final String label;

  private FollowUpStatus(int id, String label) {
    this.id = id;
    this.label = label;
  }

  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return label;
  }
}

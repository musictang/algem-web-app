/*
 * @(#) ColorPref.java Algem Web App 1.1.0 30/01/2016
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

package net.algem.config;

import java.awt.Color;
import java.util.prefs.Preferences;
import net.algem.planning.ScheduleColor;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.1.0 30/01/2016
 */
public class ColorPref {

  private final Preferences prefs = Preferences.userRoot().node("/algem/colors");;

  public ColorPref() {
  }

  /**
   * Gets the color corresponding to this enumeration {@code c}.
   * @param c schedule color enumeration
   * @return an object of type awt.Color
   */
  public Color getColor(ScheduleColor c) {
    return getColor(c.getKey(), c.getDefaultColor());
  }

  /**
   * Gets the preferred color stored under the key {@code key}.
   * @param key key name
   * @param defaultColor default color if none is found
   * @return an object of type awt.Color
   */
  public Color getColor(String key, int defaultColor) {
    int rgb = prefs.getInt(key, defaultColor);
    return new Color(rgb);
  }


}

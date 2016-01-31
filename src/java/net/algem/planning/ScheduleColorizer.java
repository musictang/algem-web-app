/*
 * @(#) ScheduleColorizer.java Algem Web App 1.1.0 30/01/2016
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

import net.algem.config.ColorPref;
import java.awt.Color;
import net.algem.util.Colorizer;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.1.0 30/01/2016
 */
public class ScheduleColorizer implements Colorizer<ScheduleElement> {

  private ColorPref colorPref;

  public ScheduleColorizer(ColorPref colorPref) {
    this.colorPref = colorPref;
  }

  @Override
  public Color getDefaultColor(ScheduleElement o) {
    switch (o.getType()) {
      case Schedule.COURSE:
        if (o.isCollective()) {
          return colorPref.getColor(ScheduleColor.COURSE_CO);
        } else {
          return colorPref.getColor(ScheduleColor.COURSE_INDIVIDUAL);
        }
      case Schedule.ROOM:
        return Color.LIGHT_GRAY;
      case Schedule.TRAINING:
        return colorPref.getColor(ScheduleColor.TRAINING);
      case Schedule.WORKSHOP:
        return colorPref.getColor(ScheduleColor.WORKSHOP);
      case Schedule.STUDIO:
      case Schedule.TECH:
        return colorPref.getColor(ScheduleColor.STUDIO);
      case Schedule.GROUP:
        return colorPref.getColor(ScheduleColor.GROUP_REHEARSAL);
      case Schedule.MEMBER:
        return colorPref.getColor(ScheduleColor.MEMBER_REHEARSAL);
      case Schedule.BOOKING_GROUP:
        return colorPref.getColor(ScheduleColor.BOOKING_GROUP);
      case Schedule.BOOKING_MEMBER:
        return colorPref.getColor(ScheduleColor.BOOKING_MEMBER);
    }
    return Color.WHITE;
  }

  @Override
  public Color getColor(ScheduleElement o) {
    return getDefaultColor(o);
  }

  @Override
  public Color getTextColor(ScheduleElement o) {
    return getForeground(getDefaultColor(o));
  }

  public static String colorToHex(Color c) {
    return  String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
  }

  /**
   * Automatically selecting a foreground color based on a background color.
   * @param bg background color
   * @return a color
   */
  public static Color getForeground(Color bg) {
    int r = bg.getRed();
    int g = bg.getGreen();
    int b = bg.getBlue();

    double luminance = r * 0.299 + g * 0.587 + b * 0.114;

    if (luminance <= 128) {
      return luminance <= 64 ? Color.WHITE.darker() : Color.WHITE;
    }
    return Color.BLACK;
  }

  /**
   * Lightens a color to enhance the foreground display.
   * @param c initial color
   * @return a color
   */
  public static Color brighten(Color c) {
    float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
    float h = hsb[0];
    float s = hsb[1];
    float b = hsb[2];
    if (b <= 0.85) {
      b += 0.15f;
    } else if (s >= 0.25) {
      s -= 0.25f;
    } else {
      return c.brighter();
    }
    return new Color(Color.HSBtoRGB(h, s, b));
  }

}

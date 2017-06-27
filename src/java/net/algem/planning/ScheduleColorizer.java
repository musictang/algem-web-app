/*
 * @(#) ScheduleColorizer.java Algem Web App 1.6.3 26/06/17
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

import net.algem.config.ColorPref;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;
import net.algem.config.Config;
import net.algem.util.Colorizer;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.3
 * @since 1.1.0 30/01/2016
 */
public class ScheduleColorizer implements Colorizer<ScheduleElement> {

  private ColorPref colorPref;
  private static final int INST_CODE = 1;

  public ScheduleColorizer(ColorPref colorPref) {
    this.colorPref = colorPref;
  }

  @Override
  public Color getDefaultColor(ScheduleElement o) {
    switch (o.getType()) {
      case Schedule.COURSE:
        if (o.isCollective()) {
          return INST_CODE == o.getCode()
            ? colorPref.getColor(ScheduleColor.INSTRUMENT_CO)
            : colorPref.getColor(ScheduleColor.COURSE_CO);
        }
        return colorPref.getColor(ScheduleColor.COURSE_INDIVIDUAL);
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
      case Schedule.ADMINISTRATIVE:
        return colorPref.getColor(ScheduleColor.ADMINISTRATIVE);
      case Schedule.MEETING:
        return colorPref.getColor(ScheduleColor.RANGE);
    }
    return Color.WHITE;
  }

  public Map<Integer,Config> getDefColorCodes() {
    Map<Integer,Config> codes = new TreeMap<>();
    codes.put(1, new Config("help.color.course.collective", colorToHex(colorPref.getColor(ScheduleColor.COURSE_CO))));
    codes.put(2, new Config("help.color.course.variable", colorToHex(colorPref.getColor(ScheduleColor.INSTRUMENT_CO))));
    codes.put(3,new Config("help.color.course.individual", colorToHex(colorPref.getColor(ScheduleColor.COURSE_INDIVIDUAL))));
    codes.put(4,new Config("help.color.range", colorToHex(colorPref.getColor(ScheduleColor.RANGE))));
    codes.put(5,new Config("help.color.workshop", colorToHex(colorPref.getColor(ScheduleColor.WORKSHOP))));
    codes.put(6,new Config("help.color.training", colorToHex(colorPref.getColor(ScheduleColor.TRAINING))));
    codes.put(7,new Config("help.color.studio", colorToHex(colorPref.getColor(ScheduleColor.STUDIO))));
    codes.put(8,new Config("help.color.administrative", colorToHex(colorPref.getColor(ScheduleColor.ADMINISTRATIVE))));
    codes.put(9,new Config("help.color.member.rehearsal", colorToHex(colorPref.getColor(ScheduleColor.MEMBER_REHEARSAL))));
    codes.put(10,new Config("help.color.booking.member", colorToHex(colorPref.getColor(ScheduleColor.BOOKING_MEMBER))));
    codes.put(11,new Config("help.color.group.rehearsal", colorToHex(colorPref.getColor(ScheduleColor.GROUP_REHEARSAL))));
    codes.put(12,new Config("help.color.booking.group", colorToHex(colorPref.getColor(ScheduleColor.BOOKING_GROUP))));
    codes.put(13,new Config("help.color.closed", "#CCC"));


    return codes;
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

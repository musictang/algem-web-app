/*
 * @(#) ScheduleColor.java Algem Web App 1.1.0 30/01/2016
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

import java.awt.Color;

/**
 * Default colors of schedule elements.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.1.0 30/01/2016
 */
public enum ScheduleColor {

  // Background
  ACTION("plan.action.color", Color.WHITE.getRGB()),
  WORKSHOP("plan.atelier.color", -526420),
  TRAINING("plan.stage.color", 16250748), //247,247,124
  COURSE_CO("plan.cours.collectif.color", -44462),
  INSTRUMENT_CO("plan.cours.instr.collectif.color", -32743),
  COURSE_INDIVIDUAL("plan.cours.individuel.color", -16723879),
  CATCHING_UP("plan.cours.rattrapage.color", -10405575),
  RANGE("plan.plage.color", -208128),
  MEMBER_REHEARSAL("plan.repet.adherent.color", -13395457),
  GROUP_REHEARSAL("plan.repet.groupe.color", -14591745),
  STUDIO("plan.studio.color", -5160193),//153,0,255 (alternative : 6711039 //102,102,255 | 10027263)
  ADMINISTRATIVE("plan.administratif.color", -6710785),//light purple 153,153,255 #9999FF
  BOOKING_GROUP("plan.reservation.groupe.color", Color.MAGENTA.getRGB()), // old -7692058
  BOOKING_MEMBER("plan.reservation.individuelle.color", Color.MAGENTA.brighter().getRGB()), // old -6170394
  // Foreground
  ACTION_LABEL("plan.action.label.color", Color.GREEN.getRGB()),
  MEMBER_LABEL("plan.adherent.label.color", -256),
  WORKSHOP_LABEL("plan.atelier.label.color", -16777216),
  TRAINING_LABEL("plan.stage.label.color", -16777216),
  COURSE_INDIVIDUAL_LABEL("plan.cours.individuel.label.color", -16777216),
  COURSE_CO_LABEL("plan.cours.collectif.label.color", -16777216),
  INSTRUMENT_CO_LABEL("plan.cours.instr.collectif.label.color", -16777216),
  FLAG("plan.flag.color",Color.YELLOW.getRGB()),
  GROUP_LABEL("plan.groupe.label.color", -1),
  LABEL("plan.label.color", Color.BLACK.getRGB()),
  CATCHING_UP_LABEL("plan.rattrapage.label.color", Color.GREEN.getRGB()),
  STUDIO_LABEL("plan.studio.label.color", Color.WHITE.getRGB()),
  ADMINISTRATIVE_LABEL("plan.administratif.label.color", -16777216),
  BOOKING_GROUP_LABEL("plan.reservation.groupe.label.color", Color.WHITE.getRGB()),
  BOOKING_MEMBER_LABEL("plan.reservation.individuelle.label.color", Color.BLACK.getRGB());

  private final String key;
  private final int color;

  /**
   *
   * @param key key name in preferences file
   * @param color an integer representing the default color
   */
  ScheduleColor(String key, int color) {
    this.key = key;
    this.color = color;
  }

  public String getKey() {
    return key;
  }

  public int getDefaultColor(){
    return color;
  }

}

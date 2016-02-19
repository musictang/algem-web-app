/*
 * @(#) BookingConf.java Algem Web App 1.1.0 02/02/2016
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

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.1.0 02/02/2016
 */
public class BookingConf {

  /** Delay before booking in hours. */
  private int minDelay;
  
  /** Cancel minDelay in hours. */
  private int cancelDelay;
  
  /** Number max of days after which booking is not allowed. */
  private int maxDelay;
  
  /** Max length of one session. */
  private float maxLength;
  
  private boolean memberShipRequired;

  public int getMinDelay() {
    return minDelay;
  }

  public void setMinDelay(int minDelay) {
    this.minDelay = minDelay;
  }

  public int getMaxDelay() {
    return maxDelay;
  }

  public void setMaxDelay(int maxDelay) {
    this.maxDelay = maxDelay;
  }

  public int getCancelDelay() {
    return cancelDelay;
  }

  public void setCancelDelay(int cancelDelay) {
    this.cancelDelay = cancelDelay;
  }

  public float getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(float maxLength) {
    this.maxLength = maxLength;
  }

  public boolean isMemberShipRequired() {
    return memberShipRequired;
  }

  public void setMemberShipRequired(boolean memberShipRequired) {
    this.memberShipRequired = memberShipRequired;
  }
  
}

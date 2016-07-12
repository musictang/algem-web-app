/*
 * @(#) FollowUpException.java Algem Web App 1.4.0 12/07/2016
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
 * @version 1.4.0
 * @since 1.4.0 12/07/2016
 */
public class FollowUpException
        extends IllegalArgumentException
{

  private Object[] args;

  /**
   * Creates a new instance of <code>FollowUpException</code> without detail message.
   */
  public FollowUpException() {
  }

  /**
   * Constructs an instance of <code>FollowUpException</code> with the specified detail message.
   *
   * @param msg the detail message.
   */
  public FollowUpException(String msg) {
    super(msg);
  }

  public FollowUpException(String msg, Object[] args) {
    super(msg);
    this.args = args;
  }
  
  public Object[] getArgs() {
    return args;
  }
}

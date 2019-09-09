/*
 * @(#) PlanningCtrlTest.java Algem Web App 1.7.4 19/10/2018
 *
 * Copyright (c) 2015-2018 Musiques Tangentes. All Rights Reserved.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.4
 */
public class PlanningCtrlTest {

  public PlanningCtrlTest() {
  }

  @Test
  public void testWeek() throws ParseException {
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    Date d = df.parse("04-01-2016");
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    assertTrue(cal.get(Calendar.WEEK_OF_YEAR) == 1);

    d = df.parse("28-12-2015");
    cal.setTime(d);
    assertTrue(cal.get(Calendar.WEEK_OF_YEAR) == 53);

  }
}

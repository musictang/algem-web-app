/*
 * @(#) UserCtrlTest.java Algem Web App 1.7.4 19/10/2018
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
package net.algem.security;

import java.text.ParseException;
import java.util.Date;
import static net.algem.util.GemConstants.DATE_FORMAT;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.4
 * @since 1.6.0 15/02/2018
 */
public class UserCtrlTest {

  @Test
  public void testPeriod() {

    long max = 1000l * 60l * 60l * 24l * 365l;// 1year
    //System.out.println(max);
    assertTrue(String.valueOf(max), max == 31536000000l);

    try {
      Date dateFrom = DATE_FORMAT.parse("15-02-2018");
      Date dateTo = DATE_FORMAT.parse("12-02-2018");
      if (dateFrom.after(dateTo)) {
        dateFrom.setTime(dateTo.getTime());
      }

      assertEquals(dateFrom, dateTo);
      dateFrom = DATE_FORMAT.parse("15-02-2016");
      long interval = dateTo.getTime() - dateFrom.getTime();
      if (interval > max) {
        dateTo.setTime(dateFrom.getTime() + max);
      }
      System.out.println(DATE_FORMAT.format(dateTo));
      assertEquals(dateTo, DATE_FORMAT.parse("14-02-2017"));
    } catch (ParseException ex) {
      System.err.println(ex.getMessage());
    }
  }
}

/*
 * @(#) UserCtrlTest.java Algem Web App 1.6.0 15/02/2018
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
package net.algem.security;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.algem.util.GemConstants.DATE_FORMAT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.0
 * @since 1.6.0 15/02/2018
 */
public class UserCtrlTest {

  @Test
  public void stackOver() {
    String BOMResult = "1|00022954|41.418\n 2|00022951|1.0\n 3|00022945|41.575\n 3|00022944|41.684\n 3|00022944|41.778\n 3|00022944|41.871\n 3|00022946|42.918\n 3|00022944|41.918\n 3|00022944|41.825\n 3|00022944|41.731\n 3|00022945|41.621\n 3|00022953|41.512\n 4|00022957|0.0\n 5|00022947|42.809\n 5|00022942|42.918\n 5|00022948|43.918\n 5|00022947|42.871\n 5|00022950|42.746\n 4|00022952|1.0\n 5|00022941|41.246\n 5|00020472|41.184\n 2|00022958|0.0\n 3|00022945|39.621\n 3|00022944|39.731\n 3|00022944|39.84 \n 3|00022944|39.949\n 3|00022944|39.887\n 3|00022944|39.793\n 3|00022945|39.684\n 3|00022956|39.512\n 4|00022959|1.0\n 5|00022941|40.762\n 5|00022943|40.699\n 4|00022957|0.0\n 5|00022947|42.809\n 5|00022942|42.918\n 5|00022948|43.918\n 5|00022947|42.871\n 5|00022950|42.746\n 3|00022949|40.996\n 3|00022944|39.996\n";
    List<String> rowList = Arrays.asList(BOMResult.split("\n"));
//    Collections.sort(rowList, new Comparator<String>() {
//       @Override
//    public int compare(String s1, String s2) {
//
//    }
//    }

    Collections.sort(rowList, new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        String array1[] = s1.split("\\|");
        String array2[] = s2.split("\\|");
        Integer i1 = Integer.valueOf(array1[0].trim());
        Integer i2 = Integer.valueOf(array2[0].trim());
        int res = i1.compareTo(i2);
        if (res == 0) {
          Double d1 = Double.valueOf(array1[array1.length - 1]);
          Double d2 = Double.valueOf(array2[array2.length - 1]);
          return d1.compareTo(d2);
        }
        return res;

      }
    });
    BOMResult = String.join("\n", rowList);
    System.out.println(BOMResult);
  }

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

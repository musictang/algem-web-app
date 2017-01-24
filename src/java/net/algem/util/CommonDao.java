/*
 * @(#) CommonDao.java Algem Web App 1.5.2 23/01/17
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

package net.algem.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.5.2
 * @since 1.5.0 19/10/2016
 */
@Repository
public class CommonDao
  extends AbstractGemDao
{
  private final static String PHOTO_TABLE = "personne_photo";
  private final static Logger LOGGER = Logger.getLogger(CommonDao.class.getName());

  public String findPhoto(int idper) throws DataAccessException {
    String query = "SELECT photo FROM " + PHOTO_TABLE + " WHERE idper = ?";
    byte[] data =  jdbcTemplate.queryForObject(query, new RowMapper<byte[]>() {
      @Override
      public byte[] mapRow(ResultSet rs, int i) throws SQLException {
        return rs.getBytes(1);
      }
    }, idper);

    return Base64.encodeBase64String(data);

  }
  
  /**
   * Gets a string representing the absence or presence of a student.
   * @param code absence status as saved in database
   * @return the string designing the absence status or an empty string if no absence
   */
  public static String getAbsenceFromNumberStatus(short code) {
    switch(code) {
      case 0: return "";
      case 1: return "ABS";
      case 2: return "EXC";
      default: return "";
    }
  }
}

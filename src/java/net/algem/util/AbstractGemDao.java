/*
 * @(#)AbstractGemDao.java	1.1.0 17/02/16
 *
 * Copyright (c) 2016 Musiques Tangentes. All Rights Reserved.
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
 *
 */
package net.algem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Abstract Data Access Object class.
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.1.0
 * @since 1.0.0 11/02/13
 */
public abstract class AbstractGemDao
        implements GemDao
{

  @Autowired
  protected JdbcTemplate jdbcTemplate;

  @Autowired
  protected NamedParameterJdbcTemplate namedJdbcTemplate;

  protected void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void setNamedJdbcTemplate(NamedParameterJdbcTemplate namedJdbcTemplate) {
    this.namedJdbcTemplate = namedJdbcTemplate;
  }

  /**
   * Gets the last sequence number in sequence {@code sequenceName}.
   *
   * @param sequenceName
   * @return an integer
   */
  @Override
  public int nextId(String sequenceName) {
    String query = "SELECT nextval(?)";
    return jdbcTemplate.queryForObject(query, Integer.class, sequenceName);
  }
}

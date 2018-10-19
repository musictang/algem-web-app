/*
 * @(#) MainConfig.java Algem Web App 1.6.0 14/03/2018
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
package net.algem.temp;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

/**
 *
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.6.0
 * @since 1.6.0 14/03/2018
 */
//@Configuration
//@ComponentScan(basePackages = "net.algem.temp")
//@PropertySource(value = {"classpath:application.properties"})
public class MainConfig {

  /*@Autowired
  private Environment env;

      @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getRequiredProperty("jdbc.url"));
        dataSource.setUsername(env.getRequiredProperty("jdbc.username"));
        dataSource.setPassword(env.getRequiredProperty("jdbc.password"));
        return dataSource;
    }*/
//  @Bean
//  public JdbcTemplate getJdbcTemplate(DataSource dataSource) {
//    return new JdbcTemplate(dataSource);
//  }

  // bean implicit name is the name of annotated method : dataSource
//  @Bean
//  public DataSource dataSource() {
//    return new JndiDataSourceLookup().getDataSource("jdbc/mustang");
//  }

//  @Bean(name = "transactionManager")
//  public DataSourceTransactionManager getTransactionManager(DataSource dataSource) {
//    return new DataSourceTransactionManager(dataSource);
//  }

}

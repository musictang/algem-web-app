/*
 * @(#) Famille.java Algem Web App 1.7.5b 18/03/2021
 *
 * Copyright (c) 2015-2021 Musiques Tangentes. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;
import net.algem.security.User;

/**
 *
 * @author <a href="mailto:eric@algem.net">Eric</a>
 * @version 1.7.5b
 * @since 1.7.5b 18/03/2021
 */
public class Famille {

    private int currentId;
    private User parent;
    private List<User> childrens = new ArrayList<User>();

    public Famille() {
    }

    public int getCurrentId() {
        return currentId;
    }

    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }

    public List<User> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<User> childrens) {
        this.childrens = childrens;
    }
  
    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

}

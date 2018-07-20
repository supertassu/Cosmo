/*
 * The MIT License
 *
 * Copyright © 2018 Tassu <hello@tassu.me>
 * Copyright © 2018 Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.tassu.cosmo.plugin.db;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.val;
import me.tassu.cosmo.cosmetic.AbstractCosmetic;
import me.tassu.cosmo.cosmetic.CosmeticManager;
import me.tassu.cosmo.plugin.cfg.CosmoConfig;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseLoader {

    @Inject private DatabaseConnector connector;
    @Inject private CosmeticManager cosmeticManager;
    @Inject private CosmoConfig cosmoConfig;

    @Getter(lazy = true) private final String table = getTableInternal();

    private String getTableInternal() {
        return Objects.requireNonNull(cosmoConfig).getConfig().getDatabase().getTables().getUserDataTable();
    }

    private static final Joiner JOINER = Joiner.on(' ');

    public Set<AbstractCosmetic> getCosmeticsForPlayer(UUID uuid) {
        try (val connection = connector.getConnection()) {
            try (val s = connection.prepareStatement("SELECT * FROM " + getTable() + " WHERE `uuid` = ? LIMIT 1;")) {
                s.setString(1, uuid.toString());
                val rs = s.executeQuery();

                if (!rs.next()) return Sets.newHashSet();

                return Arrays.stream(rs.getString("unlocked_cosmetics").split(" "))
                        .map(cosmeticManager::getCosmeticById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCosmetic(UUID uuid, AbstractCosmetic cosmetic) {
        try (val connection = connector.getConnection()) {
            try (val s = connection.prepareStatement("INSERT " + getTable() + "(`uuid`, `unlocked_cosmetics`)" +
                    " VALUES (?, ?) ON DUPLICATE KEY UPDATE unlocked_cosmetics = CONCAT(VALUES(unlocked_cosmetics), ' ', ?);")) {
                s.setString(1, uuid.toString());
                s.setString(2, cosmetic.getIdentifier());
                s.setString(3, cosmetic.getIdentifier());
                s.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}

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

package me.tassu.cosmo.cosmetic;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.SpongeCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.val;
import me.tassu.cosmo.plugin.cfg.CosmoConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CosmeticManager {

    @Inject
    private CosmoConfig cosmoConfig;

    public Optional<AbstractCosmetic> getCosmeticById(String id) {
        return cosmoConfig.getConfig().getCosmetics()
                .stream()
                .filter(it -> it.getIdentifier().equals(id))
                .findFirst();
    }

    @Getter(lazy = true) private final ContextResolver<AbstractCosmetic, SpongeCommandExecutionContext> context = createContext();

    private ContextResolver<AbstractCosmetic, SpongeCommandExecutionContext> createContext() {
        return c -> {
            try {
                return getCosmeticById(c.popFirstArg()).orElseThrow(NullPointerException::new);
            } catch (NullPointerException e) {
                throw new InvalidCommandArgument("No cosmetics found with identifier " + c.getFirstArg());
            }
        };
    }

    private Map<UUID, Map<CosmeticType, String>> map = Maps.newHashMap();

    public void equip(Player player, AbstractCosmetic cosmetic) {
        if (!map.containsKey(player.getUniqueId())) {
            map.put(player.getUniqueId(), Maps.newHashMap());
        }

        val userData = map.get(player.getUniqueId());

        if (userData.containsKey(cosmetic.getType())) {
            getCosmeticById(userData.get(cosmetic.getType())).ifPresent(it -> it.unequip(player));
        }

        cosmetic.apply(player);

        userData.put(cosmetic.getType(), cosmetic.getIdentifier());
        map.put(player.getUniqueId(), userData);
    }

    @SuppressWarnings("WeakerAccess")
    public void unequip(Player player, CosmeticType type) {
        if (!map.containsKey(player.getUniqueId())) {
            return;
        }

        val userData = map.get(player.getUniqueId());

        if (userData.containsKey(type)) {
            getCosmeticById(userData.get(type)).ifPresent(it -> it.unequip(player));
        }
    }

    public void unequip(Player player) {
        for (CosmeticType type : CosmeticType.values()) unequip(player, type);
    }

    @Listener
    public void onClientConnectionDisconnect(ClientConnectionEvent.Disconnect event, @First Player player) {
        for (CosmeticType type : CosmeticType.values()) unequip(player, type);
    }
}

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

package me.tassu.cosmo.plugin;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.SpongeCommandManager;
import co.aikar.taskchain.SpongeTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.flowpowered.noise.module.modifier.ScalePoint;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import lombok.val;
import me.tassu.cosmo.cosmetic.AbstractCosmetic;
import me.tassu.cosmo.cosmetic.CosmeticManager;
import me.tassu.cosmo.cosmetic.CosmeticSerializer;
import me.tassu.cosmo.plugin.cfg.CosmoConfig;
import me.tassu.cosmo.plugin.ui.CosmoCommand;
import me.tassu.cosmo.plugin.db.DatabaseConnector;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class Cosmo {

    @Inject private Logger logger;

    @Inject private PluginContainer container;

    @Inject private CosmoConfig config;
    @Inject private DatabaseConnector database;
    @Inject private CosmeticManager cosmeticManager;

    @Inject private CosmeticSerializer serializer;
    @Inject private CosmoCommand command;

    private static TaskChainFactory taskChainFactory;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Getting started...");

        // init taskchain
        taskChainFactory = SpongeTaskChainFactory.create(container);

        // load config
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(AbstractCosmetic.class), serializer);

        config.load();

        // connect to database
        database.connect();
        database.checkSchema();

        // setup command
        val manager = new SpongeCommandManager(container);
        manager.getCommandContexts().registerContext(AbstractCosmetic.class, cosmeticManager.getContext());
        manager.getCommandContexts().registerContext(UUID.class, ctx -> {
            val search = ctx.popFirstArg();
            val player = Sponge.getServer().getPlayer(search);

            if (player.isPresent()) return player.get().getUniqueId();
            if (ctx.getSource() instanceof Player) return ((Player) ctx.getSource()).getUniqueId();
            throw new InvalidCommandArgument("No players found with name " + search);
        });
        manager.registerCommand(command);
    }

    @Listener
    public void onGameStopping(GameStoppingEvent event) {
        logger.info("Ready. Set. Done.");
        config.save();

    }

}

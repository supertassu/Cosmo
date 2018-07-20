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

package me.tassu.cosmo.plugin.startup;

import com.google.inject.Inject;
import lombok.val;
import me.tassu.cosmo.cosmetic.CosmeticManager;
import me.tassu.cosmo.plugin.Cosmo;
import me.tassu.cosmo.plugin.ui.CosmoClickListener;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

@Plugin(id = "cosmo",
        name = "Cosmo",
        description = "A cosmetics plugin for sponge.",
        version = "1.0.0",
        dependencies = {
                @Dependency(id = "boxboy")
        })
public class CosmoPlugin {

    @Inject
    private PluginContainer container;
    @Inject
    private Logger logger;
    @Inject
    private Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Listener
    public void onStart(GameStartingServerEvent event) {
        val injector = new CosmoModule(container, logger, loader).getInjector();
        game.getEventManager().registerListeners(this, injector.getInstance(Cosmo.class));
        game.getEventManager().registerListeners(this, injector.getInstance(CosmoClickListener.class));
        game.getEventManager().registerListeners(this, injector.getInstance(CosmeticManager.class));
        logger.info("Cosmo was loaded.");
    }
}

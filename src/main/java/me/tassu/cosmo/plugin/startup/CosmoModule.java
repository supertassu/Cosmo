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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import lombok.Getter;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

public class CosmoModule extends AbstractModule {

    private final PluginContainer plugin;
    private final Logger logger;
    private final ConfigurationLoader<CommentedConfigurationNode> configLoader;

    public CosmoModule(PluginContainer plugin, Logger logger, ConfigurationLoader<CommentedConfigurationNode> configLoader) {
        if (instance != null) throw new IllegalStateException("already setup");
        instance = this;

        this.plugin = plugin;
        this.logger = logger;
        this.configLoader = configLoader;
    }

    @Getter private static CosmoModule instance;
    @Getter(lazy = true) private final Injector injector = createInjector();

    private Injector createInjector() {
        return Guice.createInjector(this);
    }

    @Override
    protected void configure() {
        this.bind(PluginContainer.class).toInstance(plugin);
        this.bind(Logger.class).toInstance(logger);
        this.bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {}).toInstance(configLoader);

        this.bind(Game.class).toInstance(Sponge.getGame());
        this.bind(Server.class).toInstance(Sponge.getServer());
    }

}

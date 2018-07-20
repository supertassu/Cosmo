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

package me.tassu.cosmo.plugin.cfg;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;

import java.io.IOException;

@Singleton
public class CosmoConfig {

    @Getter @Setter
    private static CosmoConfig instance;

    public CosmoConfig() {
        instance = this;
    }

    private static final String HEADER = "This is the main configuration file of Cosmo.";
    private static final ConfigurationOptions LOADER_OPTIONS = ConfigurationOptions.defaults();

    @Inject private ConfigurationLoader<CommentedConfigurationNode> loader;

    public void load() {
        try {
            val configMapper = ObjectMapper.forObject(this);
            configMapper.populate(this.loader.load(LOADER_OPTIONS));
        } catch (ObjectMappingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            ObjectMapper<CosmoConfig>.BoundInstance configMapper = ObjectMapper.forObject(this);
            SimpleCommentedConfigurationNode out = SimpleCommentedConfigurationNode.root();
            configMapper.serialize(out);
            loader.save(out);
        } catch (ObjectMappingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Setting(value = "cosmo", comment = HEADER)
    @Getter
    private MainConfig config = new MainConfig();

}

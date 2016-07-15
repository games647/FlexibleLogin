/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 games647 and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.games647.flexiblelogin.config;

import com.github.games647.flexiblelogin.FlexibleLogin;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class Settings {

    private final ConfigurationLoader<CommentedConfigurationNode> configManager;
    private final File defaultConfigFile;

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    private ObjectMapper<Config>.BoundInstance configMapper;
    private ObjectMapper<TextConfig>.BoundInstance textMapper;

    public Settings(ConfigurationLoader<CommentedConfigurationNode> configManager, File defaultConfigFile) {
        this.configManager = configManager;
        this.defaultConfigFile = defaultConfigFile;

        try {
            configMapper = ObjectMapper.forClass(Config.class).bindToNew();
            textMapper = ObjectMapper.forClass(TextConfig.class).bindToNew();
        } catch (ObjectMappingException objMappingExc) {
            plugin.getLogger().error("Invalid plugin structure", objMappingExc);
        }
    }

    public void load() {
        defaultConfigFile.getParentFile().mkdir();
        if (!defaultConfigFile.exists()) {
            try {
                defaultConfigFile.createNewFile();
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error creating a new config file", ioExc);
                return;
            }
        }

        loadMapper(configMapper, configManager);

        File textFile = new File(getConfigDir(), "messages");
        HoconConfigurationLoader textLoader = HoconConfigurationLoader.builder().setFile(textFile).build();
        loadMapper(textMapper, textLoader);
    }

    public void loadMapper(ObjectMapper<?>.BoundInstance mapper
            , ConfigurationLoader<CommentedConfigurationNode> loader) {
        CommentedConfigurationNode rootNode = loader.createEmptyNode();
        if (mapper != null) {
            try {
                rootNode = loader.load();

                //load the config into the object
                mapper.populate(rootNode);

                //add missing default values
                mapper.serialize(rootNode);
                loader.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                plugin.getLogger().error("Error loading the configuration", objMappingExc);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error saving the default configuration", ioExc);
            }
        }
    }

    public Config getConfig() {
        if (configMapper == null) {
            return null;
        }

        return configMapper.getInstance();
    }

    public TextConfig getTextConfig() {
        if (textMapper == null) {
            return null;
        }

        return textMapper.getInstance();
    }

    public File getConfigDir() {
        return defaultConfigFile.getParentFile();
    }
}

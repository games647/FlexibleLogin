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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class Settings {

    private final ConfigurationLoader<CommentedConfigurationNode> configManager;
    private final Path defaultConfigFile;

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    private ObjectMapper<Config>.BoundInstance configMapper;
    private ObjectMapper<TextConfig>.BoundInstance textMapper;

    public Settings(ConfigurationLoader<CommentedConfigurationNode> configManager, Path defaultConfigFile) {
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
        if (Files.notExists(defaultConfigFile)) {
            try {
                if (Files.notExists(defaultConfigFile.getParent())) {
                    Files.createDirectory(defaultConfigFile.getParent());
                }

                Files.createFile(defaultConfigFile);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error creating a new config file", ioExc);
                return;
            }
        }

        loadMapper(configMapper, configManager);

        Path textFile = getConfigDir().resolve("messages.conf");
        HoconConfigurationLoader textLoader = HoconConfigurationLoader.builder().setPath(textFile).build();
        loadMapper(textMapper, textLoader);
    }

    private void loadMapper(ObjectMapper<?>.BoundInstance mapper
            , ConfigurationLoader<CommentedConfigurationNode> loader) {
        CommentedConfigurationNode rootNode;
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

    public Config getGeneral() {
        return configMapper.getInstance();
    }

    public TextConfig getText() {
        return textMapper.getInstance();
    }

    public Path getConfigDir() {
        return defaultConfigFile.getParent();
    }
}

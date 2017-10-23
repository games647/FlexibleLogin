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

import com.google.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;

public class Settings {

    private final Logger logger;
    private final Path dataFolder;

    private ObjectMapper<Config>.BoundInstance configMapper;
    private ObjectMapper<TextConfig>.BoundInstance textMapper;

    @Inject
    //We will place more than one config there (i.e. H2/SQLite database)
    public Settings(Logger logger, @ConfigDir(sharedRoot = false) Path dataFolder) {
        this.logger = logger;
        this.dataFolder = dataFolder;

        try {
            configMapper = ObjectMapper.forClass(Config.class).bindToNew();
            textMapper = ObjectMapper.forClass(TextConfig.class).bindToNew();
        } catch (ObjectMappingException objMappingExc) {
            logger.error("Invalid plugin structure", objMappingExc);
        }
    }

    public void load() {
        Path configFile = dataFolder.resolve("config.conf");
        if (Files.notExists(configFile)) {
            try {
                Files.createDirectories(dataFolder);

                loadMapper(configMapper, HoconConfigurationLoader.builder().setPath(configFile).build());

                Path textFile = dataFolder.resolve("messages.conf");
                loadMapper(textMapper, HoconConfigurationLoader.builder().setPath(textFile).build());
            } catch (IOException ioExc) {
                logger.error("Error creating a new config file", ioExc);
            }
        }
    }

    private <T> void loadMapper(ObjectMapper<T>.BoundInstance mapper
            , ConfigurationLoader<CommentedConfigurationNode> loader) {
        CommentedConfigurationNode rootNode;
        if (mapper != null) {
            try {
                rootNode = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

                //load the config into the object
                mapper.populate(rootNode);

                //add missing default values
                loader.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                logger.error("Error loading the configuration", objMappingExc);
            } catch (IOException ioExc) {
                logger.error("Error saving the default configuration", ioExc);
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
        return dataFolder;
    }
}

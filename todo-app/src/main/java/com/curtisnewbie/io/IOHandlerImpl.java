package com.curtisnewbie.io;

import com.curtisnewbie.config.Config;
import com.curtisnewbie.entity.TodoJob;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhuangyongj
 */
public class IOHandlerImpl implements IOHandler {
    private static final String DIR_NAME = "todo-app";
    private static final String CONF_NAME = "settings.conf";
    private static final String DEF_SAVE_NAME = "save.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_PATH = System.getProperty("user.home") + File.separator + DIR_NAME;

    @Override
    public List<TodoJob> loadTodoJob(String savePath) {
        File saveFile = new File(savePath);
        try {
            if (!saveFile.exists()) {
                return new ArrayList<>();
            } else {
                return Arrays.asList(objectMapper.readValue(saveFile, TodoJob[].class));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    @Override
    public void generateConfIfNotExists() {
        File conf = new File(getConfPath());
        if (!conf.exists()) {
            try {
                if (!conf.getParentFile().exists())
                    conf.getParentFile().mkdir();
                conf.createNewFile();
                writeDefaultConfIntoFile(conf);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    @Override
    public Config readConfig() {
        generateConfIfNotExists();
        File conf = new File(getConfPath());
        Config config = null;
        try {
            config = objectMapper.readValue(conf, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return config;
    }

    @Override
    public void writeTodoJob(List<TodoJob> jobs, String savePath) {
        File saveFile = new File(savePath);
        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
                bw.write(new String(objectMapper.writeValueAsBytes(jobs), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Write the default configuration (text) into the file
     *
     * @param file
     * @throws IOException
     */
    private void writeDefaultConfIntoFile(File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException("Cannot write default configuration, file doesn't exist");
        Config defaultConfig = new Config();
        defaultConfig.setSavePath(getDefSavePath());
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(objectMapper.writeValueAsString(defaultConfig));
        }
    }

    private String getDefSavePath() {
        return getBasePath() + File.separator + DEF_SAVE_NAME;
    }

    @Override
    public String getConfPath() {
        return getBasePath() + File.separator + IOHandlerImpl.CONF_NAME;
    }

    private String getBasePath() {
        return BASE_PATH;
    }
}

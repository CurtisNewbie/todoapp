package com.curtisnewbie.io;

import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;
import com.curtisnewbie.util.DateUtil;
import com.curtisnewbie.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yongjie.zhuang
 */
public class IOHandlerImpl implements IOHandler {
    private static final String DIR_NAME = "todo-app";
    private static final String CONF_NAME = "settings.json";
    private static final String DEF_SAVE_NAME = "save.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_PATH = System.getProperty("user.home") + File.separator + DIR_NAME;
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    @Override
    public List<TodoJob> loadTodoJob(String savePath) throws FailureToLoadException {
        File saveFile = new File(savePath);
        try {
            if (!saveFile.exists()) {
                return new ArrayList<>();
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(saveFile, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    String json = sb.toString();
                    if (StrUtil.isEmpty(json) || json.matches("^\\[\\s+\\]$"))
                        return new ArrayList<>();
                    return Arrays.asList(objectMapper.readValue(json, TodoJob[].class));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FailureToLoadException(e);
        }
    }

    @Override
    public void generateConfIfNotExists() {
        File conf = new File(getConfPath());
        if (!conf.exists()) {
            try {
                if (!conf.getParentFile().exists())
                    conf.getParentFile().mkdirs();
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
        Config config;
        try {
            config = objectMapper.readValue(conf, Config.class);
        } catch (IOException e) {
            e.printStackTrace(); // recoverable exception
            try {
                // overwrite the file if exception is caught
                config = writeDefaultConfIntoFile(conf);
            } catch (IOException ioException) {
                throw new RuntimeException(ioException); // unrecoverable exception
            }
        }
        return config;
    }

    private void writeTodoJob(List<TodoJob> jobs, String savePath) {
        File saveFile = new File(savePath);
        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile, StandardCharsets.UTF_8))) {
                objectMapper.writeValue(bw, jobs);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void writeTodoJobSync(List<TodoJob> jobs, String savePath) {
        writeTodoJob(jobs, savePath);
    }

    @Override
    public void writeTodoJobAsync(List<TodoJob> jobs, String savePath) {
        singleThreadExecutor.execute(() -> {
            writeTodoJob(jobs, savePath);
        });
    }

    @Override
    public void exportTodoJobAsync(List<TodoJob> jobs, File file, Language lang) {
        if (file == null)
            return;
        singleThreadExecutor.execute(() -> {
            try {
                if (!file.exists())
                    file.createNewFile();
                String suffix;
                if (lang.equals(Language.CHN))
                    suffix = Language.CHN.key;
                else
                    suffix = Language.ENG.key;
                String done = PropertiesLoader.getInstance().get(PropertyConstants.TEXT_DONE_PREFIX + suffix);
                String inProgress = PropertiesLoader.getInstance().get(PropertyConstants.TEXT_IN_PROGRESS_PREFIX + suffix);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                    for (TodoJob j : jobs) {
                        bw.write(String.format("[%s] %s '%s'\n", j.isDone() ? done : inProgress, DateUtil.toDateStrSlash(j.getStartDate()),
                                               j.getName()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Write the default configuration (text) into the file
     *
     * @param file
     * @return defaultConfig
     * @throws IOException
     */
    private Config writeDefaultConfIntoFile(File file) throws IOException {
        Config defaultConfig = new Config();
        defaultConfig.setSavePath(getDefSavePath());
        defaultConfig.setLanguage(Language.DEFAULT.key);
        writeConfig(defaultConfig, file);
        return defaultConfig;
    }

    @Override
    public void writeConfigAsync(Config config) {
        singleThreadExecutor.execute(() -> {
            File file = new File(getConfPath());
            try {
                if (!file.exists())
                    file.createNewFile();
                writeConfig(config, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * Write configuration (text) into the file
     *
     * @param config
     * @param file
     * @throws IOException
     */
    private void writeConfig(Config config, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(objectMapper.writeValueAsString(config));
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

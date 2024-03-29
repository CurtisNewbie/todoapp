package com.curtisnewbie.io;

import com.curtisnewbie.config.Config;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;
import com.curtisnewbie.util.CountdownTimer;
import com.curtisnewbie.util.StrUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.curtisnewbie.util.ToastUtil.toast;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * @author yongjie.zhuang
 */
@Slf4j
public class IOHandlerImpl implements IOHandler {
    private static final String DIR_NAME = "todo-app";
    private static final String CONF_NAME = "settings.json";
    private static final String BASE_PATH = System.getProperty("user.home") + File.separator + DIR_NAME;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }


    @Override
    public List<TodoJob> loadTodoJob(File saveFile) throws FailureToLoadException {
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        try {
            if (!saveFile.exists()) {
                return Collections.EMPTY_LIST;
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    String json = sb.toString();
                    List<TodoJob> list;
                    if (StrUtil.isEmpty(json) || json.matches("^\\[\\s+\\]$"))
                        list = Collections.EMPTY_LIST;
                    else
                        list = Arrays.asList(objectMapper.readValue(json, TodoJob[].class));
                    timer.stop();
                    log.debug(String.format("Loaded %d records, took %.2f milliseconds\n", list.size(), timer.getMilliSec()));
                    return list;
                }
            }
        } catch (IOException e) {
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
                log.error("Unable to generate configuration file", e);
                throw new IllegalStateException(e);
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
                throw new IllegalStateException(ioException); // unrecoverable exception
            }
        }
        return config;
    }

    @Override
    public void writeObjectsAsync(String content, File file) {
        if (file == null)
            return;
        runAsync(() -> {
            try {
                if (!file.exists())
                    file.createNewFile();

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write(content);
                }
            } catch (IOException e) {
                log.error("Unable to write objects", e);
            }
        });
    }


    /**
     * Write the default configuration (text) into the file
     *
     * @return defaultConfig
     */
    private Config writeDefaultConfIntoFile(File file) throws IOException {
        Config defaultConfig = Config.getDefaultConfig();
        writeConfig(defaultConfig, file);
        return defaultConfig;
    }

    @Override
    public void writeConfigAsync(Config config) {
        CompletableFuture.runAsync(() -> {
            File file = new File(getConfPath());
            try {
                if (!file.exists()) file.createNewFile();

                writeConfig(config, file);
            } catch (IOException e) {
                toast("Failed to write config file\n\n" + e.getMessage(), 10_000);
            }
        });
    }


    /**
     * Write configuration (text) into the file
     */
    private void writeConfig(Config config, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(objectMapper.writeValueAsString(config));
        }
    }

    @Override
    public String getConfPath() {
        return BASE_PATH + File.separator + IOHandlerImpl.CONF_NAME;
    }

    @Override
    public String readResourceAsString(String relPath) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(relPath), StandardCharsets.UTF_8)
        )) {
            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null)
                sb.append(temp).append("\n"); // preserve line feed
            return sb.toString();
        }
    }

}

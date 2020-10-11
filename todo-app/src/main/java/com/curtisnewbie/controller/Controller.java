package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerImpl;
import com.curtisnewbie.util.DateUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Controller for UI (fxml)
 * </p>
 *
 * @author yongjie.zhuang
 */
public class Controller implements Initializable {
    private static final int PADDING = 30;

    @FXML
    private ListView<TodoJobView> listView;
    private Config config;
    private final IOHandler ioHandler = new IOHandlerImpl();

    private String BACKUP_TODO_TITLE;
    private String EXPORT_TODO_TITLE;
    private String TODO_LOADING_FAILURE_TITLE;
    private String CONFIG_PATH_TITLE;
    private String SAVE_PATH_TITLE;
    private String NEW_TODO_NAME_TITLE;
    private String ADD_NEW_TODO_TITLE;
    private String NEW_TODO_TITLE;
    private String ADD_TITLE;
    private String DELETE_TITLE;
    private String COPY_TITLE;
    private String BACKUP_TITLE;
    private String EXPORT_TITLE;
    private String ABOUT_TITLE;

    /**
     * Add {@code TodoJobView} into the {@code ListView}
     *
     * @param jobView
     */
    public void addTodoJobView(TodoJobView jobView) {
        jobView.prefWidthProperty().bind(listView.widthProperty().subtract(PADDING));
        listView.getItems().add(jobView);
    }

    /**
     * Add new {@code TodoJobView} into the {@code ListView}
     *
     * @param jobName
     */
    public void addTodoJobView(String jobName) {
        TodoJobView jobView = new TodoJobView(jobName, this);
        addTodoJobView(jobView);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = ioHandler.readConfig();
        handleLanguageConfig();
        // load previous job list if exists
        try {
            var list = ioHandler.loadTodoJob(config.getSavePath());
            for (TodoJob j : list) {
                addTodoJobView(new TodoJobView(j, this));
            }
        } catch (FailureToLoadException e) {
            toastError(TODO_LOADING_FAILURE_TITLE);
            e.printStackTrace();
        }

        // save the whole to-do list on app shutdown
        App.registerOnClose(() -> saveSync());
        // register a ContextMenu for the ListView
        listView.setContextMenu(createCtxMenu());
        // register ctrl+s key event handler for ListView
        registerCtrlSHandler(listView);
        sortListView();
    }

    /**
     * Sort the {@code ListView} based on 1. whether they are finished and 2. their create date
     */
    protected void sortListView() {
        listView.getItems().sort((a, b) -> {
            int res = Boolean.compare(a.getTodoJob().isDone(), b.getTodoJob().isDone());
            if (res != 0)
                return res;
            else
                return b.getTodoJob().getStartDate().compareTo(a.getTodoJob().getStartDate());
        });
    }

    private JobCtxMenu createCtxMenu() {
        JobCtxMenu ctxMenu = new JobCtxMenu();
        ctxMenu.addMenuItem(ADD_TITLE, e -> {
            Platform.runLater(() -> {
                TextInputDialog dialog = new TextInputDialog(NEW_TODO_TITLE);
                dialog.setTitle(ADD_NEW_TODO_TITLE);
                dialog.setContentText(NEW_TODO_NAME_TITLE);
                Optional<String> result = dialog.showAndWait();
                if (!result.isEmpty() && !result.get().isBlank()) {
                    addTodoJobView(result.get().trim());
                    sortListView();
                }
            });
        }).addMenuItem(DELETE_TITLE, e -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0)
                listView.getItems().remove(selected);
        }).addMenuItem(COPY_TITLE, e -> {
            Platform.runLater(() -> {
                int selected = listView.getSelectionModel().getSelectedIndex();
                if (selected >= 0)
                    copyToClipBoard(listView.getItems().get(selected).getTodoJob().getName());
            });
        }).addMenuItem(BACKUP_TITLE, e -> {
            Platform.runLater(() -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(BACKUP_TODO_TITLE);
                fileChooser.setInitialFileName("backup_" + DateUtil.toLongDateStrDash(new Date()).replace(":", ""));
                fileChooser.getExtensionFilters().add(getBackupExtFilter());
                File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                ioHandler.writeTodoJobAsync(listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList()),
                                            nFile.getAbsolutePath());
            });
        }).addMenuItem(EXPORT_TITLE, e -> {
            Platform.runLater(() -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(EXPORT_TODO_TITLE);
                fileChooser.setInitialFileName("export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", ""));
                fileChooser.getExtensionFilters().add(getExportExtFilter());
                File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                ioHandler.exportTodoJob(listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList()), nFile);
            });
        }).addMenuItem(ABOUT_TITLE, e -> {
            Platform.runLater(() -> {
                toastPaths();
            });
        });
        return ctxMenu;
    }

    /**
     * Save the to-do list based on config in a synchronous way
     */
    private void saveSync() {
        List<TodoJob> list = listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList());
        ioHandler.writeTodoJobSync(list, config.getSavePath());
    }

    /**
     * Save the to-do list based on config in a asynchronous way
     */
    private void saveAsync() {
        List<TodoJob> list = listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList());
        ioHandler.writeTodoJobAsync(list, config.getSavePath());
    }

    /**
     * Register ctrl+s key event handler for ListView, which triggers {@link #saveAsync()}
     *
     * @param lv
     */
    private void registerCtrlSHandler(ListView<TodoJobView> lv) {
        lv.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.S) && e.isControlDown()) {
                saveAsync();
                toastInfo("Saved -- " + new Date().toString());
            }
        });
    }

    private void toastInfo(String msg) {
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(true);
            Label label = new Label(msg);
            alert.getDialogPane().setContent(label);
            alert.show();
        });
    }

    private void toastError(String msg) {
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);
            Label label = new Label(msg);
            alert.getDialogPane().setContent(label);
            alert.show();
        });
    }

    /**
     * Copy content to clipboard
     *
     * @param content text
     */
    private void copyToClipBoard(String content) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent cc = new ClipboardContent();
        cc.putString(content);
        clipboard.setContent(cc);
    }

    private void toastPaths() {
        toastInfo(String.format("%s '%s'\n", CONFIG_PATH_TITLE, ioHandler.getConfPath()) +
                  String.format("%s '%s'\n", SAVE_PATH_TITLE, config.getSavePath()) +
                  "Github: 'https://github.com/CurtisNewbie/todoapp'\n");
    }

    /**
     * Handle language-related configurations
     */
    private void handleLanguageConfig() {
        PropertiesLoader props = PropertiesLoader.getInstance();
        String lang = config.getLanguage();
        if (lang == null)
            lang = Language.DEFAULT.key;
        boolean isChn = lang.equals(Language.CHN.key);
        String suffix;
        if (isChn)
            suffix = Language.CHN.key;
        else
            suffix = Language.ENG.key;

        EXPORT_TODO_TITLE = props.get(PropertyConstants.TITLE_EXPORT_TODO_PREFIX + suffix);
        BACKUP_TODO_TITLE = props.get(PropertyConstants.TITLE_BACKUP_TODO_PREFIX + suffix);
        TODO_LOADING_FAILURE_TITLE = props.get(PropertyConstants.TITLE_TODO_LOADING_FAILURE_PREFIX + suffix);
        SAVE_PATH_TITLE = props.get(PropertyConstants.TITLE_SAVE_PATH_PREFIX + suffix);
        CONFIG_PATH_TITLE = props.get(PropertyConstants.TITLE_CONFIG_PATH_PREFIX + suffix);
        NEW_TODO_NAME_TITLE = props.get(PropertyConstants.TITLE_NEW_TODO_NAME_PREFIX + suffix);
        ADD_NEW_TODO_TITLE = props.get(PropertyConstants.TITLE_ADD_NEW_TODO_PREFIX + suffix);
        NEW_TODO_TITLE = props.get(PropertyConstants.TITLE_NEW_TODO_PREFIX + suffix);
        ADD_TITLE = props.get(PropertyConstants.TITLE_ADD_PREFIX + suffix);
        DELETE_TITLE = props.get(PropertyConstants.TITLE_DELETE_PREFIX + suffix);
        COPY_TITLE = props.get(PropertyConstants.TITLE_COPY_PREFIX + suffix);
        BACKUP_TITLE = props.get(PropertyConstants.TITLE_BACKUP_PREFIX + suffix);
        EXPORT_TITLE = props.get(PropertyConstants.TITLE_EXPORT_PREFIX + suffix);
        ABOUT_TITLE = props.get(PropertyConstants.TITLE_ABOUT_PREFIX + suffix);
    }

    private FileChooser.ExtensionFilter getExportExtFilter() {
        return new FileChooser.ExtensionFilter("txt", Arrays.asList("*.txt"));
    }

    private FileChooser.ExtensionFilter getBackupExtFilter() {
        return new FileChooser.ExtensionFilter("json", Arrays.asList("*.json"));
    }
}


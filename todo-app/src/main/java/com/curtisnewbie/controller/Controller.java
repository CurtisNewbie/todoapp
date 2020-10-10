package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerImpl;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * <p>
 * Controller for UI (fxml)
 * </p>
 *
 * @author yongjie.zhuang
 */
public class Controller implements Initializable {
    private static final String ENG = "eng";
    private static final String CHN = "chn";
    private static final int PADDING = 30;

    @FXML
    private ListView<TodoJobView> listView;

    private Config config;

    private final IOHandler ioHandler = new IOHandlerImpl();

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
            toastInfo("Successfully loaded todo list");
        } catch (FailureToLoadException e) {
            toastError("Failed to load todo list");
            e.printStackTrace();
        }

        // save the whole to-do list on app shutdown
        App.registerOnClose(() -> saveSync());
        // register a ContextMenu for the ListView
        listView.setContextMenu(createCtxMenu());
        // register ctrl+s key event handler for ListView
        registerCtrlSHandler(listView);
        sortListView();
        printPaths();
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
                TextInputDialog dialog = new TextInputDialog("NewJob");
                dialog.setHeaderText("Add New TODO");
                dialog.setTitle("Add a new TODO");
                dialog.setContentText("Enter the name of the TODO:");
                Optional<String> result = dialog.showAndWait();
                if (!result.isEmpty() && !result.get().isBlank()) {
                    addTodoJobView(result.get().trim());
                    sortListView();
                }
            });
        }).addMenuItem(DELETE_TITLE, e -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
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
                fileChooser.setTitle("Backup TO-DO List");
                File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                ioHandler.writeTodoJobAsync(listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList()),
                                            nFile.getAbsolutePath());
            });
        }).addMenuItem(EXPORT_TITLE, e -> {
            Platform.runLater(() -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export TO-DO List");
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

    private void printPaths() {
        System.out.printf("Configuration path: '%s'\n", ioHandler.getConfPath());
        System.out.printf("Saved at path: '%s'\n", config.getSavePath());
    }

    private void toastPaths() {
        toastInfo(String.format("Configuration path: '%s'\n", ioHandler.getConfPath()) +
                  String.format("Saved at path: '%s'\n", config.getSavePath()) + "Github: 'https://github.com/CurtisNewbie/todoapp'\n");
    }

    /**
     * Handle language-related configurations
     */
    private void handleLanguageConfig() {
        PropertiesLoader props = PropertiesLoader.getInstance();
        String lang = config.getLanguage();
        if(lang == null)
            lang = Config.DEF_LANGUAGE;
        boolean isChn = lang.equals("chn");
        String suffix;
        if (isChn)
            suffix = CHN;
        else
            suffix = ENG;

        ADD_TITLE = props.get(PropertyConstants.TITLE_ADD_PREFIX + suffix);
        DELETE_TITLE = props.get(PropertyConstants.TITLE_DELETE_PREFIX + suffix);
        COPY_TITLE = props.get(PropertyConstants.TITLE_COPY_PREFIX + suffix);
        BACKUP_TITLE = props.get(PropertyConstants.TITLE_BACKUP_PREFIX + suffix);
        EXPORT_TITLE = props.get(PropertyConstants.TITLE_EXPORT_PREFIX + suffix);
        ABOUT_TITLE = props.get(PropertyConstants.TITLE_ABOUT_PREFIX + suffix);
    }
}

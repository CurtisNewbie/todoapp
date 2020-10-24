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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <p>
 * Controller for UI (fxml)
 * </p>
 *
 * @author yongjie.zhuang
 */
public class Controller implements Initializable {

    private static final int PADDING = 35;

    private final String SAVED_TEXT;
    private final String SAVE_ON_CLOSE_TEXT;
    private final String CHOOSE_LANGUAGE_TITLE;
    private final String BACKUP_TODO_TITLE;
    private final String EXPORT_TODO_TITLE;
    private final String TODO_LOADING_FAILURE_TITLE;
    private final String CONFIG_PATH_TITLE;
    private final String SAVE_PATH_TITLE;
    private final String NEW_TODO_NAME_TITLE;
    private final String ADD_NEW_TODO_TITLE;
    private final String NEW_TODO_TITLE;
    private final String ADD_TITLE;
    private final String DELETE_TITLE;
    private final String COPY_TITLE;
    private final String BACKUP_TITLE;
    private final String EXPORT_TITLE;
    private final String ABOUT_TITLE;
    private final Language language;

    @FXML
    private ListView<TodoJobView> listView;
    private final Config config;
    private final IOHandler ioHandler = new IOHandlerImpl();

    /**
     * record whether user has content that is not saved
     */
    private final AtomicBoolean saved = new AtomicBoolean(true);

    public Controller() {
        config = ioHandler.readConfig();
        PropertiesLoader props = PropertiesLoader.getInstance();
        String lang = config.getLanguage();
        if (lang == null)
            lang = Language.DEFAULT.key;
        boolean isChn = lang.equals(Language.CHN.key);
        String suffix;
        if (isChn) {
            language = Language.CHN;
            suffix = Language.CHN.key;
        } else {
            language = Language.ENG;
            suffix = Language.ENG.key;
        }
        SAVED_TEXT = props.get(PropertyConstants.TEXT_SAVED_PREFIX + suffix);
        SAVE_ON_CLOSE_TEXT = props.get(PropertyConstants.TEXT_SAVE_ON_CLOSE_PREFIX + suffix);
        CHOOSE_LANGUAGE_TITLE = props.get(PropertyConstants.TITLE_CHOOSE_LANGUAGE_PREFIX + suffix);
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // load previous job list if exists
        try {
            var list = ioHandler.loadTodoJob(config.getSavePath());
            for (TodoJob j : list) {
                addTodoJobView(new TodoJobView(j));
            }
        } catch (FailureToLoadException e) {
            toastError(TODO_LOADING_FAILURE_TITLE);
            e.printStackTrace();
        }

        // save the whole to-do list on app shutdown only when it needs to
        App.registerOnClose(() -> {
            if (!saved.get()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, SAVE_ON_CLOSE_TEXT, ButtonType.OK, ButtonType.CANCEL);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    saveSync();
                }
            }
        });
        // register a ContextMenu for the ListView
        listView.setContextMenu(createCtxMenu());
        // register ctrl+s key event handler for ListView
        registerCtrlSHandler(listView);
    }

    /**
     * <p>
     * Add {@code TodoJobView} into the {@code ListView}.
     * </p>
     * <p>
     * The operation of adding the jobView to the ListView is always executed in Javafx's thread
     * </p>
     *
     * @param jobView
     */
    public void addTodoJobView(TodoJobView jobView) {
        jobView.regDoneCbEventHandler(() -> {
            saved.set(false);
            sortListView();
        });
        Platform.runLater(() -> {
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(PADDING));
            listView.getItems().add(jobView);
        });
    }

    /**
     * <p>
     * Remove {@code TodoJobView} from the {@code ListView}.
     * </p>
     * <p>
     * This method is always executed in Javafx's thread
     * </p>
     *
     * @param i index
     */
    public void removeTodoJobView(int i) {
        Platform.runLater(() -> {
            listView.getItems().remove(i);
        });
    }

    /**
     * Add new {@code TodoJobView} into the {@code ListView}.
     *
     * <p>
     * The operation of adding the jobView to the ListView is always executed in Javafx's thread
     * </p>
     *
     * @param jobName
     */
    public void addTodoJobView(String jobName) {
        TodoJobView jobView = new TodoJobView(jobName);
        addTodoJobView(jobView);
    }

    /**
     * <p>
     * Sort the {@code ListView} based on 1) whether they are finished and 2) the date when they are created
     * </p>
     * <p>
     * This method is always executed within Javafx Thread
     * </p>
     */
    protected void sortListView() {
        Platform.runLater(() -> {
            listView.getItems().sort((a, b) -> {
                int res = Boolean.compare(a.getTodoJob().isDone(), b.getTodoJob().isDone());
                if (res != 0)
                    return res;
                else
                    return b.getTodoJob().getStartDate().compareTo(a.getTodoJob().getStartDate());
            });
        });
    }

    private JobCtxMenu createCtxMenu() {
        JobCtxMenu ctxMenu = new JobCtxMenu();
        ctxMenu.addMenuItem(ADD_TITLE, this::onAddHandler).addMenuItem(DELETE_TITLE, this::onDeleteHandler).addMenuItem(
                COPY_TITLE, this::onCopyHandler).addMenuItem(BACKUP_TITLE, this::onBackupHandler).addMenuItem(
                EXPORT_TITLE, this::onExportHandler).addMenuItem(ABOUT_TITLE, this::onAboutHandler).addMenuItem(
                CHOOSE_LANGUAGE_TITLE, this::onLanguageHandler);
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
                saved.set(true);
                toastInfo(SAVED_TEXT + " - " + new Date().toString());
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
        toastInfo(String.format("%s '%s'\n", CONFIG_PATH_TITLE, ioHandler.getConfPath()) + String.format("%s '%s'\n",
                SAVE_PATH_TITLE, config.getSavePath()) + "Github: 'https://github.com/CurtisNewbie/todoapp'\n");
    }

    private FileChooser.ExtensionFilter getExportExtFilter() {
        return new FileChooser.ExtensionFilter("txt", Arrays.asList("*.txt"));
    }

    private FileChooser.ExtensionFilter getBackupExtFilter() {
        return new FileChooser.ExtensionFilter("json", Arrays.asList("*.json"));
    }

    private void onAddHandler(ActionEvent e) {
        Platform.runLater(() -> {
            TxtAreaDialog dialog = new TxtAreaDialog(NEW_TODO_TITLE);
            dialog.setTitle(ADD_NEW_TODO_TITLE);
            dialog.setContentText(NEW_TODO_NAME_TITLE);
            Optional<String> result = dialog.showAndWait();
            if (!result.isEmpty() && !result.get().isBlank()) {
                addTodoJobView(result.get().trim());
                sortListView();
            }
            saved.set(false);
        });
    }

    private void onDeleteHandler(ActionEvent e) {
        int selected = listView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText(DELETE_TITLE);
                alert.showAndWait().filter(resp -> resp == ButtonType.OK).ifPresent(resp -> {
                    removeTodoJobView(selected);
                    saved.set(false);
                });
            });
        }
    }

    private void onCopyHandler(ActionEvent e) {
        Platform.runLater(() -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0)
                copyToClipBoard(listView.getItems().get(selected).getTodoJob().getName());
        });
    }

    private void onBackupHandler(ActionEvent e) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(BACKUP_TODO_TITLE);
            fileChooser.setInitialFileName("backup_" + DateUtil.toLongDateStrDash(new Date()).replace(":", ""));
            fileChooser.getExtensionFilters().add(getBackupExtFilter());
            File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
            ioHandler.writeTodoJobAsync(
                    listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList()),
                    nFile.getAbsolutePath());
        });
    }

    private void onExportHandler(ActionEvent e) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(EXPORT_TODO_TITLE);
            fileChooser.setInitialFileName("export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", ""));
            fileChooser.getExtensionFilters().add(getExportExtFilter());
            File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
            ioHandler.exportTodoJob(
                    listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList()), nFile,
                    language);
        });
    }

    private void onAboutHandler(ActionEvent e) {
        Platform.runLater(() -> {
            toastPaths();
        });
    }

    private void onLanguageHandler(ActionEvent e) {
        String engChoice = "English";
        String chnChoice = "中文";
        Platform.runLater(() -> {
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(CHOOSE_LANGUAGE_TITLE);
            choiceDialog.setSelectedItem(engChoice);
            choiceDialog.getItems().add(engChoice);
            choiceDialog.getItems().add(chnChoice);
            Optional<String> opt = choiceDialog.showAndWait();
            if (opt.isPresent()) {
                if (opt.get().equals(engChoice) && !language.equals(Language.ENG)) {
                    config.setLanguage(Language.ENG.key);
                } else {
                    config.setLanguage(Language.CHN.key);
                }
            }
            ioHandler.writeConfigAsync(config);
        });
    }
}



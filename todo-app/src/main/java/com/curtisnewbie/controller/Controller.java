package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerImpl;
import com.curtisnewbie.util.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.TextFactory.*;

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
    private final String ADD_NEW_TODO_TITLE;
    private final String UPDATE_TODO_NAME_TITLE;
    private final String ADD_TITLE;
    private final String DELETE_TITLE;
    private final String UPDATE_TITLE;
    private final String COPY_TITLE;
    private final String BACKUP_TITLE;
    private final String EXPORT_TITLE;
    private final String ABOUT_TITLE;
    private final Language language;

    @FXML
    private ListView<TodoJobView> listView;
    private final Config config;
    private final IOHandler ioHandler = new IOHandlerImpl();
    private final RedoQueue redoQueue = new RedoQueue();

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
        SAVED_TEXT = props.get(TEXT_SAVED_PREFIX + suffix);
        SAVE_ON_CLOSE_TEXT = props.get(TEXT_SAVE_ON_CLOSE_PREFIX + suffix);
        CHOOSE_LANGUAGE_TITLE = props.get(TITLE_CHOOSE_LANGUAGE_PREFIX + suffix);
        EXPORT_TODO_TITLE = props.get(TITLE_EXPORT_TODO_PREFIX + suffix);
        BACKUP_TODO_TITLE = props.get(TITLE_BACKUP_TODO_PREFIX + suffix);
        TODO_LOADING_FAILURE_TITLE = props.get(TITLE_TODO_LOADING_FAILURE_PREFIX + suffix);
        SAVE_PATH_TITLE = props.get(TITLE_SAVE_PATH_PREFIX + suffix);
        CONFIG_PATH_TITLE = props.get(TITLE_CONFIG_PATH_PREFIX + suffix);
        ADD_NEW_TODO_TITLE = props.get(TITLE_ADD_NEW_TODO_PREFIX + suffix);
        UPDATE_TODO_NAME_TITLE = props.get(TITLE_UPDATE_TODO_NAME_PREFIX + suffix);
        ADD_TITLE = props.get(TITLE_ADD_PREFIX + suffix);
        DELETE_TITLE = props.get(TITLE_DELETE_PREFIX + suffix);
        UPDATE_TITLE = props.get(TITLE_UPDATE_PREFIX + suffix);
        COPY_TITLE = props.get(TITLE_COPY_PREFIX + suffix);
        BACKUP_TITLE = props.get(TITLE_BACKUP_PREFIX + suffix);
        EXPORT_TITLE = props.get(TITLE_EXPORT_PREFIX + suffix);
        ABOUT_TITLE = props.get(TITLE_ABOUT_PREFIX + suffix);
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
        registerCtrlKeyHandler(listView);
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
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(PADDING).subtract(TodoJobView.WIDTH_FOR_LABELS));
            listView.getItems().add(jobView);
            jobView.requestFocus();
            jobView.requestLayout();
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
     * @return TodoJob
     */
    public TodoJob removeTodoJobView(int i) {
        TodoJob job = listView.getItems().get(i).createTodoJobCopy();
        Platform.runLater(() -> {
            listView.getItems().remove(i);
        });
        return job;
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
        TodoJobView jobView = new TodoJobView(new TodoJob(jobName));
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
                int res = Boolean.compare(a.createTodoJobCopy().isDone(), b.createTodoJobCopy().isDone());
                if (res != 0)
                    return res;
                else
                    return b.createTodoJobCopy().getStartDate().compareTo(a.createTodoJobCopy().getStartDate());
            });
        });
    }

    /**
     * Update {@code TodoJobView}
     * <p>
     * This method is always ran in JavaFx's UI Thread
     *
     * @param jobView
     * @param todoJob
     */
    private void updateTodoJobView(TodoJobView jobView, TodoJob todoJob) {
        Platform.runLater(() -> {
            jobView.setName(todoJob.getName());
            if (todoJob.getStartDate() != null) {
                jobView.updateDate(todoJob.getStartDate());
            }
        });
    }

    private CnvCtxMenu createCtxMenu() {
        CnvCtxMenu ctxMenu = new CnvCtxMenu();
        ctxMenu.addMenuItem(ADD_TITLE, this::onAddHandler).addMenuItem(DELETE_TITLE, this::onDeleteHandler)
               .addMenuItem(UPDATE_TITLE, this::onUpdateHandler).addMenuItem(COPY_TITLE, this::onCopyHandler)
               .addMenuItem(BACKUP_TITLE, this::onBackupHandler).addMenuItem(EXPORT_TITLE, this::onExportHandler)
               .addMenuItem(ABOUT_TITLE, this::onAboutHandler).addMenuItem(CHOOSE_LANGUAGE_TITLE, this::onLanguageHandler);
        return ctxMenu;
    }

    /**
     * Save the to-do list based on config in a synchronous way
     */
    private void saveSync() {
        List<TodoJob> list = listView.getItems().stream().map(TodoJobView::createTodoJobCopy).collect(Collectors.toList());
        ioHandler.writeTodoJobSync(list, config.getSavePath());
    }

    /**
     * Save the to-do list based on config in a asynchronous way
     */
    private void saveAsync() {
        List<TodoJob> list = listView.getItems().stream().map(TodoJobView::createTodoJobCopy).collect(Collectors.toList());
        ioHandler.writeTodoJobAsync(list, config.getSavePath());
    }

    /**
     * <p>
     * Register ctrl+? key event handler for ListView
     * </p>
     * <p>
     * E.g., Ctrl+s, triggers {@link #saveAsync()} for saving To-do list
     * </p>
     * <p>
     * E.g., Ctrl+z, triggers {@link #redo()} for redoing previous action if possible
     * </p>
     *
     * @param lv
     */
    private void registerCtrlKeyHandler(ListView<TodoJobView> lv) {
        lv.setOnKeyPressed(e -> {
            if (!e.isControlDown())
                return;
            if (e.getCode().equals(KeyCode.S)) {
                saveAsync();
                saved.set(true);
                toastInfo(SAVED_TEXT + " - " + new Date().toString());
            } else if (e.getCode().equals(KeyCode.Z)) {
                saved.set(false);
                redo();
            }
        });
    }

    /**
     * Redo previous action
     */
    private void redo() {
        Redo redo = redoQueue.get();
        if (redo == null)
            return;
        if (redo.getType().equals(RedoType.DELETE)) {
            addTodoJobView(new TodoJobView(redo.getTodoJob()));
        }
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
     * Copy content to clipboard (This method is always ran within a Javafx' UI Thread)
     *
     * @param content text
     */
    private void copyToClipBoard(String content) {
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent cc = new ClipboardContent();
            cc.putString(content);
            clipboard.setContent(cc);
        });
    }

    private FileChooser.ExtensionFilter getExportExtFilter() {
        return new FileChooser.ExtensionFilter("txt", Arrays.asList("*.txt"));
    }

    private FileChooser.ExtensionFilter getBackupExtFilter() {
        return new FileChooser.ExtensionFilter("json", Arrays.asList("*.json"));
    }

    private void onAddHandler(ActionEvent e) {
        Platform.runLater(() -> {
            TodoJobDialog dialog = new TodoJobDialog();
            dialog.setTitle(UPDATE_TODO_NAME_TITLE);
            Optional<TodoJob> result = dialog.showAndWait();
            if (result.isPresent() && !StrUtil.isEmpty(result.get().getName())) {
                saved.set(false);
                addTodoJobView(new TodoJobView(result.get()));
                sortListView();
            }
        });
    }

    private void onUpdateHandler(ActionEvent e) {
        Platform.runLater(() -> {
            final int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                TodoJobView jobView = listView.getItems().get(selected);
                TodoJobDialog dialog = new TodoJobDialog(jobView.getName(), jobView.getStartDate());
                dialog.setTitle(UPDATE_TODO_NAME_TITLE);
                Optional<TodoJob> result = dialog.showAndWait();
                if (result.isPresent()) {
                    saved.set(false);
                    updateTodoJobView(jobView, result.get());
                    sortListView();
                }
            }
        });
    }

    private void onDeleteHandler(ActionEvent e) {
        int selected = listView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText(DELETE_TITLE);
                alert.showAndWait().filter(resp -> resp == ButtonType.OK).ifPresent(resp -> {
                    saved.set(false);
                    TodoJob job = removeTodoJobView(selected);
                    redoQueue.put(new Redo(RedoType.DELETE, job));
                });
            });
        }
    }

    private void onCopyHandler(ActionEvent e) {
        Platform.runLater(() -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0)
                copyToClipBoard(listView.getItems().get(selected).createTodoJobCopy().getName());
        });
    }

    private void onBackupHandler(ActionEvent e) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(BACKUP_TODO_TITLE);
            fileChooser.setInitialFileName("Backup_" + DateUtil.toLongDateStrDash(new Date()).replace(":", ""));
            fileChooser.getExtensionFilters().add(getBackupExtFilter());
            File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
            ioHandler.writeTodoJobAsync(listView.getItems().stream().map(TodoJobView::createTodoJobCopy).collect(Collectors.toList()),
                                        nFile.getAbsolutePath());
        });
    }

    private void onExportHandler(ActionEvent e) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(EXPORT_TODO_TITLE);
            fileChooser.setInitialFileName("Export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", ""));
            fileChooser.getExtensionFilters().add(getExportExtFilter());
            File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
            ioHandler.exportTodoJob(listView.getItems().stream().map(TodoJobView::createTodoJobCopy).collect(Collectors.toList()), nFile, language);
        });
    }

    private void onAboutHandler(ActionEvent e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            GridPane gPane = new GridPane();
            alert.setTitle(ABOUT_TITLE);
            gPane.add(getClassicTextWithPadding(String.format("%s: '%s'", CONFIG_PATH_TITLE, ioHandler.getConfPath())), 0, 0);
            gPane.add(getClassicTextWithPadding(String.format("%s: '%s'", SAVE_PATH_TITLE, config.getSavePath())), 0, 1);
            gPane.add(getClassicTextWithPadding("Github: 'https://github.com/CurtisNewbie/todoapp'"), 0, 2);
            alert.getDialogPane().setContent(gPane);
            alert.show();
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



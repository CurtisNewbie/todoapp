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
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.TextFactory.*;

/**
 * <p>
 * Controller for UI
 * </p>
 * <p>
 * Note that {@code ListView} isn't thread-safe, to make sure everything works fine, the read/write operations on {@code
 * ListView} must always be done in the same thread (JavaFx's UI Thread).
 * </p>
 *
 * @author yongjie.zhuang
 */
public class Controller implements Initializable {

    private static final int PADDING = 55;

    private final String SAVED_TEXT;
    private final String SAVE_ON_CLOSE_TEXT;
    private final String CHOOSE_LANGUAGE_TITLE;
    private final String BACKUP_TODO_TITLE;
    private final String APPEND_TODO_TITLE;
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
    private final String APPEND_TITLE;
    private final String ABOUT_TITLE;
    private final String GITHUB_ABOUT;
    private final String AUTHOR_ABOUT;

    private final Language lang;

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
        String langStr = config.getLanguage();
        if (langStr == null)
            langStr = Language.DEFAULT.key;
        boolean isChn = langStr.equals(Language.CHN.key);
        if (isChn) {
            lang = Language.CHN;
        } else {
            lang = Language.ENG;
        }
        GITHUB_ABOUT = props.get(APP_GITHUB);
        AUTHOR_ABOUT = props.get(APP_AUTHOR);
        SAVED_TEXT = props.get(TEXT_SAVED_PREFIX, lang);
        SAVE_ON_CLOSE_TEXT = props.get(TEXT_SAVE_ON_CLOSE_PREFIX, lang);
        CHOOSE_LANGUAGE_TITLE = props.get(TITLE_CHOOSE_LANGUAGE_PREFIX, lang);
        EXPORT_TODO_TITLE = props.get(TITLE_EXPORT_TODO_PREFIX, lang);
        BACKUP_TODO_TITLE = props.get(TITLE_BACKUP_TODO_PREFIX, lang);
        APPEND_TODO_TITLE = props.get(TITLE_APPEND_TODO_PREFIX, lang);
        TODO_LOADING_FAILURE_TITLE = props.get(TITLE_TODO_LOADING_FAILURE_PREFIX, lang);
        SAVE_PATH_TITLE = props.get(TITLE_SAVE_PATH_PREFIX, lang);
        CONFIG_PATH_TITLE = props.get(TITLE_CONFIG_PATH_PREFIX, lang);
        ADD_NEW_TODO_TITLE = props.get(TITLE_ADD_NEW_TODO_PREFIX, lang);
        UPDATE_TODO_NAME_TITLE = props.get(TITLE_UPDATE_TODO_NAME_PREFIX, lang);
        ADD_TITLE = props.get(TITLE_ADD_PREFIX, lang);
        DELETE_TITLE = props.get(TITLE_DELETE_PREFIX, lang);
        UPDATE_TITLE = props.get(TITLE_UPDATE_PREFIX, lang);
        COPY_TITLE = props.get(TITLE_COPY_PREFIX, lang);
        BACKUP_TITLE = props.get(TITLE_BACKUP_PREFIX, lang);
        EXPORT_TITLE = props.get(TITLE_EXPORT_PREFIX, lang);
        APPEND_TITLE = props.get(TITLE_APPEND_PREFIX, lang);
        ABOUT_TITLE = props.get(TITLE_ABOUT_PREFIX, lang);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // load previous job list if exists
        try {
            var list = ioHandler.loadTodoJob(config.getSavePath());
            for (TodoJob j : list) {
                addTodoJobView(new TodoJobView(j, lang));
            }
        } catch (FailureToLoadException e) {
            toastError(TODO_LOADING_FAILURE_TITLE);
            e.printStackTrace();
        }

        // save the whole to-do list on app shutdown only when it needs to
        App.registerOnClose(() -> {
            if (!saved.get()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, SAVE_ON_CLOSE_TEXT, ButtonType.YES, ButtonType.NO);
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
            saved.set(false);
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(PADDING));
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(PADDING).subtract(TodoJobView.WIDTH_FOR_LABELS));
            listView.getItems().add(jobView);
            jobView.requestFocus();
            jobView.requestLayout();
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
        TodoJobView jobView = new TodoJobView(new TodoJob(jobName), lang);
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
                int res = Boolean.compare(a.isSelected(), b.isSelected());
                if (res != 0)
                    return res;
                else
                    return b.getStartDate().compareTo(a.getStartDate());
            });
        });
    }

    private CnvCtxMenu createCtxMenu() {
        CnvCtxMenu ctxMenu = new CnvCtxMenu();
        ctxMenu.addMenuItem(ADD_TITLE, this::onAddHandler).addMenuItem(DELETE_TITLE, this::onDeleteHandler)
                .addMenuItem(UPDATE_TITLE, this::onUpdateHandler).addMenuItem(COPY_TITLE, this::onCopyHandler)
                .addMenuItem(APPEND_TITLE, this::onAppendHandler).addMenuItem(BACKUP_TITLE, this::onBackupHandler)
                .addMenuItem(EXPORT_TITLE, this::onExportHandler).addMenuItem(ABOUT_TITLE, this::onAboutHandler)
                .addMenuItem(CHOOSE_LANGUAGE_TITLE, this::onLanguageHandler);
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
            if (e.isControlDown()) {
                if (e.getCode().equals(KeyCode.S)) {
                    saveAsync();
                    saved.set(true);
                    toastInfo(SAVED_TEXT + " - " + new Date().toString());
                } else if (e.getCode().equals(KeyCode.Z)) {
                    saved.set(false);
                    redo();
                }
            } else {
                if (e.getCode().equals(KeyCode.DELETE)) {
                    deleteSelected();
                }
            }
        });
    }

    /**
     * Redo previous action
     */
    private void redo() {
        Redo redo = redoQueue.poll();
        if (redo == null)
            return;
        if (redo.getType().equals(RedoType.DELETE)) {
            addTodoJobView(new TodoJobView(redo.getTodoJob(), lang));
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

    private FileChooser.ExtensionFilter getTxtExtFilter() {
        return new FileChooser.ExtensionFilter("txt", Arrays.asList("*.txt"));
    }

    private FileChooser.ExtensionFilter getJsonExtFilter() {
        return new FileChooser.ExtensionFilter("json", Arrays.asList("*.json"));
    }

    private void onAddHandler(ActionEvent e) {
        Platform.runLater(() -> {
            TodoJobDialog dialog = new TodoJobDialog();
            dialog.setTitle(UPDATE_TODO_NAME_TITLE);
            Optional<TodoJob> result = dialog.showAndWait();
            if (result.isPresent() && !StrUtil.isEmpty(result.get().getName())) {
                saved.set(false);
                addTodoJobView(new TodoJobView(result.get(), lang));
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
                    jobView.setName(result.get().getName());
                    jobView.setStartDate(result.get().getStartDate());
                    sortListView();
                }
            }
        });
    }

    private void onDeleteHandler(ActionEvent e) {
        deleteSelected();
    }

    private void deleteSelected() {
        Platform.runLater(() -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText(DELETE_TITLE);
                alert.showAndWait()
                        .filter(resp -> resp == ButtonType.OK)
                        .ifPresent(resp -> {
                            saved.set(false);
                            TodoJobView jobView = listView.getItems().remove(selected);
                            redoQueue.offer(new Redo(RedoType.DELETE, jobView.createTodoJobCopy()));
                        });
            }
        });
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
            fileChooser.getExtensionFilters().add(getJsonExtFilter());
            File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
            if (nFile == null)
                return;
            ioHandler.writeTodoJobAsync(listView.getItems().stream().map(TodoJobView::createTodoJobCopy).collect(Collectors.toList()),
                    nFile.getAbsolutePath());
        });
    }

    private void onExportHandler(ActionEvent e) {
        Platform.runLater(() -> {
            if (listView.getItems().isEmpty())
                return;

            // 1. pick date range
            LocalDate earliestDate = findEarliestDate();
            LocalDate now = LocalDate.now();
            DateRangeDialog dateRangeDialog = new DateRangeDialog(earliestDate, now);
            var opt = dateRangeDialog.showAndWait();
            if (opt.isPresent()) {
                DateRange dr = opt.get();
                // 2. choose where to export
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(EXPORT_TODO_TITLE);
                fileChooser.setInitialFileName("Export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", ""));
                fileChooser.getExtensionFilters().add(getTxtExtFilter());
                File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                if (nFile == null)
                    return;

                // 3. filter based on date range, and create a todoJob copy of each "view"
                var todoJobs = new ArrayList<TodoJob>();
                for (var v : listView.getItems()) {
                    if (v.getStartDate().compareTo(dr.getStart()) >= 0 && v.getStartDate().compareTo(dr.getEnd()) <= 0) {
                        todoJobs.add(v.createTodoJobCopy());
                    }
                }
                ioHandler.exportTodoJobAsync(todoJobs, nFile, lang);
            }
        });
    }

    // this method should be ran in UI thread, wrap it inside Platform.runLater()
    private LocalDate findEarliestDate() {
        if (!listView.getItems().isEmpty()) {
            LocalDate earliest = listView.getItems().get(0).getStartDate();
            for (var jobView : listView.getItems()) {
                if (jobView.getStartDate().compareTo(earliest) < 0)
                    earliest = jobView.getStartDate();
            }
            return earliest;
        }
        return LocalDate.now();
    }

    private void onAboutHandler(ActionEvent e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            GridPane gPane = new GridPane();
            alert.setTitle(ABOUT_TITLE);
            gPane.add(getClassicTextWithPadding(String.format("%s: '%s'", CONFIG_PATH_TITLE, ioHandler.getConfPath())), 0, 0);
            gPane.add(getClassicTextWithPadding(String.format("%s: '%s'", SAVE_PATH_TITLE, config.getSavePath())), 0, 1);
            gPane.add(getClassicTextWithPadding(GITHUB_ABOUT), 0, 2);
            gPane.add(getClassicTextWithPadding(AUTHOR_ABOUT), 0, 3);
            alert.getDialogPane().setContent(gPane);
            alert.show();
        });
    }

    private void onAppendHandler(ActionEvent e) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(APPEND_TODO_TITLE);
            fileChooser.getExtensionFilters().add(getJsonExtFilter());
            File nFile = fileChooser.showOpenDialog(App.getPrimaryStage());
            if (nFile == null || !nFile.exists())
                return;
            try {
                var list = ioHandler.loadTodoJob(nFile);
                list.forEach(job -> {
                    addTodoJobView(new TodoJobView(job, lang));
                });
                toastInfo(String.format("Loaded %d TO-DOs", list.size()));
            } catch (FailureToLoadException ex) {
                ex.printStackTrace();
            }
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
                if (opt.get().equals(engChoice) && !lang.equals(Language.ENG)) {
                    config.setLanguage(Language.ENG.key);
                } else {
                    config.setLanguage(Language.CHN.key);
                }
            }
            ioHandler.writeConfigAsync(config);
        });
    }

    public Language getLang() {
        return lang;
    }
}



package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.dao.MapperFactory;
import com.curtisnewbie.dao.TodoJobMapper;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerImpl;
import com.curtisnewbie.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.TextFactory.getClassicTextWithPadding;

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

    private final String CHOOSE_LANGUAGE_TITLE;
    private final String BACKUP_TODO_TITLE;
    private final String APPEND_TODO_TITLE;
    private final String READ_TODO_TITLE;
    private final String EXPORT_TODO_TITLE;
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
    private final String LOAD_TITLE;

    private final Language lang;
    private final TodoJobMapper todoJobMapper = MapperFactory.getFactory().getTodoJobMapper();

    @FXML
    private ListView<TodoJobView> listView;
    @FXML
    private HBox pageControlHBox;

    private final Config config;
    private final IOHandler ioHandler = new IOHandlerImpl();
    private final RedoStack redoStack = new RedoStack();

    /** record whether the current file is readonly */
    private static final AtomicBoolean readOnly = new AtomicBoolean(false);

    /** current page, need to be synchronised using {@link #currPageLock} */
    private int currPage = 1;
    private Object currPageLock = new Object();
    private Label currPageLabel = LabelFactory.getClassicLabel("1");
    private static final Logger logger = Logger.getLogger(Controller.class.getName());

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
        CHOOSE_LANGUAGE_TITLE = props.get(TITLE_CHOOSE_LANGUAGE_PREFIX, lang);
        EXPORT_TODO_TITLE = props.get(TITLE_EXPORT_TODO_PREFIX, lang);
        BACKUP_TODO_TITLE = props.get(TITLE_BACKUP_TODO_PREFIX, lang);
        APPEND_TODO_TITLE = props.get(TITLE_APPEND_TODO_PREFIX, lang);
        READ_TODO_TITLE = props.get(TITLE_READ_TODO_PREFIX, lang);
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
        LOAD_TITLE = props.get(TITLE_LOAD_PREFIX, lang);
        ABOUT_TITLE = props.get(TITLE_ABOUT_PREFIX, lang);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // register a ContextMenu for the ListView
        listView.setContextMenu(createCtxMenu());
        // register ctrl+? key event handler for ListView
        registerCtrlKeyHandler(listView);
        // registers event handlers for paging
        Button prevPageBtn = ButtonFactory.getRectBtn("Previous Page");
        prevPageBtn.setOnAction(e -> {
            loadPrevPage();
        });
        Button nextPageBtn = ButtonFactory.getRectBtn("Next Page");
        nextPageBtn.setOnAction(e -> {
            loadNextPage();
        });
        pageControlHBox.setAlignment(Pos.BASELINE_RIGHT);
        pageControlHBox.getChildren().addAll(LabelFactory.getClassicLabel("Page:"), MarginFactory.fixedMargin(10),
                currPageLabel, MarginFactory.fixedMargin(10),
                prevPageBtn, MarginFactory.fixedMargin(10),
                nextPageBtn, MarginFactory.fixedMargin(10));
        CompletableFuture.runAsync(() -> {
            prepareStartupData();
        });
    }

    private void prepareStartupData() {
        // for migration
        if (!todoJobMapper.hasRecord() && ioHandler.fileExists(config.getSavePath())) {
            logger.info("Detected data migration needed, attempting to migrate todos");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Detected data for previous version, do you want to migrate them?",
                        ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.YES) {
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            var jsonList = ioHandler.loadTodoJob(config.getSavePath());
                            logger.info("Found " + jsonList.size() + " todos, migrating...");
                            return jsonList;
                        } catch (FailureToLoadException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }).thenAcceptAsync(jsonList -> {
                        if (jsonList != null)
                            for (TodoJob j : jsonList)
                                todoJobMapper.insert(j);
                    }).thenRun(() -> {
                        loadCurrPage();
                    });
                }
            });
        } else {
            // load the first page
            loadCurrPage();
        }
    }

    private void loadNextPage() {
        CompletableFuture.supplyAsync(() -> {
            synchronized (currPageLock) {
                var jobList = todoJobMapper.findByPage(currPage + 1);
                if (jobList.isEmpty())
                    return null;
                currPage += 1;
                Platform.runLater(() -> {
                    currPageLabel.setText(String.valueOf(currPage));
                });
                return jobList;
            }
        }).thenAccept((jobList) -> {
            if (jobList == null)
                return;
            var jobViewList = new ArrayList<TodoJobView>();
            for (TodoJob j : jobList) {
                jobViewList.add(new TodoJobView(j, lang));
            }
            Platform.runLater(() -> {
                listView.getItems().clear();
                jobViewList.forEach(jv -> {
                    addTodoJobView(jv);
                });
            });
        });
    }

    private void loadPrevPage() {
        CompletableFuture.supplyAsync(() -> {
            synchronized (currPageLock) {
                if (currPage <= 1)
                    return null;
                var jobList = todoJobMapper.findByPage(currPage - 1);
                if (jobList.isEmpty())
                    return null;
                currPage -= 1;
                Platform.runLater(() -> {
                    currPageLabel.setText(String.valueOf(currPage));
                });
                return jobList;
            }
        }).thenAccept((jobList) -> {
            if (jobList == null)
                return;
            var jobViewList = new ArrayList<TodoJobView>();
            for (TodoJob j : jobList) {
                jobViewList.add(new TodoJobView(j, lang));
            }
            Platform.runLater(() -> {
                listView.getItems().clear();
                jobViewList.forEach(jv -> {
                    addTodoJobView(jv);
                });
            });
        });
    }

    private void loadCurrPage() {
        CompletableFuture.supplyAsync(() -> {
            synchronized (currPageLock) {
                var jobList = todoJobMapper.findByPage(currPage);
                if (jobList.isEmpty())
                    return null;
                return jobList;
            }
        }).thenAccept((jobList) -> {
            if (jobList == null)
                return;
            var jobViewList = new ArrayList<TodoJobView>();
            for (TodoJob j : jobList) {
                jobViewList.add(new TodoJobView(j, lang));
            }
            Platform.runLater(() -> {
                listView.getItems().clear();
                jobViewList.forEach(jv -> {
                    addTodoJobView(jv);
                });
            });
        });
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
    private void addTodoJobView(TodoJobView jobView) {
        jobView.regCheckboxEvntHandler(() -> {
            int c = todoJobMapper.updateById(jobView.createTodoJobCopy());
            if (c > 0)
                sortListView();
            else
                toastError("Failed to update to-do, please try again");
        });
        Platform.runLater(() -> {
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(PADDING));
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(PADDING).subtract(TodoJobView.WIDTH_FOR_LABELS));
            listView.getItems().add(jobView);
            jobView.requestFocus();
            jobView.requestLayout();
        });
    }

    // only used on application startup, or loading read-only todos
    private void _batchAddTodoJobViews(List<TodoJobView> jobViews) {
        jobViews.forEach(jobView -> {
            jobView.regCheckboxEvntHandler(() -> {
                sortListView();
            });
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(PADDING));
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(PADDING).subtract(TodoJobView.WIDTH_FOR_LABELS));
            listView.getItems().add(jobView);
        });
    }

    /**
     * <p>
     * Sort the {@code ListView} based on 1) whether they are finished and 2) the date when they are created
     * </p>
     * <p>
     * This method is always executed within Javafx Thread
     * </p>
     */
    private void sortListView() {
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
                .addMenuItem(APPEND_TITLE, this::onAppendHandler).addMenuItem(LOAD_TITLE, this::onReadHandler)
                .addMenuItem(BACKUP_TITLE, this::onBackupHandler).addMenuItem(EXPORT_TITLE, this::onExportHandler)
                .addMenuItem(ABOUT_TITLE, this::onAboutHandler).addMenuItem(CHOOSE_LANGUAGE_TITLE, this::onLanguageHandler);
        return ctxMenu;
    }

    /**
     * <p>
     * Register ctrl+? key event handler for ListView
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
                if (e.getCode().equals(KeyCode.Z)) {
                    if (readOnly.get())
                        return;
                    redo();
                }
            } else {
                if (e.getCode().equals(KeyCode.DELETE)) {
                    if (readOnly.get())
                        return;
                    deleteSelected();
                } else if (e.getCode().equals(KeyCode.F5)) {
                    if (readOnly.get())
                        return;
                    loadCurrPage();
                }
            }
        });
    }

    /**
     * Redo previous action
     */
    private void redo() {
        synchronized (redoStack) {
            Redo redo = redoStack.pop();
            if (redo == null)
                return;
            if (redo.getType().equals(RedoType.DELETE)) {
                Integer newId = todoJobMapper.insert(redo.getTodoJob());
                if (newId != null) {
                    var job = redo.getTodoJob();
                    job.setId(newId);
                    addTodoJobView(new TodoJobView(job, lang));
                } else {
                    toastError("Unknown error happens when try to redo");
                }
            }
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

    private static FileChooser.ExtensionFilter getTxtExtFilter() {
        return new FileChooser.ExtensionFilter("txt", Arrays.asList("*.txt"));
    }

    private static FileChooser.ExtensionFilter getJsonExtFilter() {
        return new FileChooser.ExtensionFilter("json", Arrays.asList("*.json"));
    }

    private void onAddHandler(ActionEvent e) {
        Platform.runLater(() -> {
            if (readOnly.get())
                return;
            TodoJobDialog dialog = new TodoJobDialog();
            dialog.setTitle(ADD_NEW_TODO_TITLE);
            Optional<TodoJob> result = dialog.showAndWait();
            if (result.isPresent() && !StrUtil.isEmpty(result.get().getName())) {
                TodoJob newTodo = result.get();

                Integer id = todoJobMapper.insert(newTodo);
                if (id == null) {
                    toastError("Failed to add new to-do, please try again");
                    return;
                }
                newTodo.setId(id);
                addTodoJobView(new TodoJobView(newTodo, lang));
                sortListView();
            }
        });
    }

    private void onUpdateHandler(ActionEvent e) {
        Platform.runLater(() -> {
            if (readOnly.get())
                return;
            final int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                TodoJobView jobView = listView.getItems().get(selected);
                TodoJobDialog dialog = new TodoJobDialog(jobView.getName(), jobView.getStartDate());
                dialog.setTitle(UPDATE_TODO_NAME_TITLE);
                Optional<TodoJob> result = dialog.showAndWait();
                if (result.isPresent()) {
                    var job = result.get();
                    job.setDone(jobView.isSelected());
                    job.setId(jobView.getIdOfTodoJob());
                    if (todoJobMapper.updateById(job) > 0) {
                        jobView.setName(result.get().getName());
                        jobView.setStartDate(result.get().getStartDate());
                        sortListView();
                    } else {
                        toastError("Failed to update to-do, please try again");
                    }
                }
            }
        });
    }

    private void onDeleteHandler(ActionEvent e) {
        deleteSelected();
    }

    private void deleteSelected() {
        Platform.runLater(() -> {
            if (readOnly.get())
                return;
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText(DELETE_TITLE);
                alert.showAndWait()
                        .filter(resp -> resp == ButtonType.OK)
                        .ifPresent(resp -> {
                            int id = listView.getItems().get(selected).getIdOfTodoJob();
                            if (todoJobMapper.deleteById(id) <= 0) {
                                toastInfo("Failed to delete to-do, please try again");
                            } else {
                                var jobCopy = listView.getItems().remove(selected).createTodoJobCopy();
                                synchronized (redoStack) {
                                    redoStack.push(new Redo(RedoType.DELETE, jobCopy));
                                }
                            }
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
            fileChooser.setInitialFileName("Backup_" + DateUtil.toLongDateStrDash(new Date()).replace(":", "") + ".json");
            fileChooser.getExtensionFilters().add(getJsonExtFilter());
            File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
            if (nFile == null)
                return;
            ioHandler.writeTodoJobAsync(listView.getItems().stream().map(TodoJobView::createTodoJobCopy).collect(Collectors.toList()),
                    nFile.getAbsolutePath());
        });
    }

    private void onReadHandler(ActionEvent e) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(READ_TODO_TITLE);
            fileChooser.getExtensionFilters().add(getJsonExtFilter());
            File nFile = fileChooser.showOpenDialog(App.getPrimaryStage());
            if (nFile == null || !nFile.exists())
                return;
            CompletableFuture.supplyAsync(() -> {
                try {
                    return ioHandler.loadTodoJob(nFile);
                } catch (FailureToLoadException exception) {
                    exception.printStackTrace();
                    return null;
                }
            }).thenAccept((list) -> {
                if (list == null)
                    return;
                Platform.runLater(() -> {
                    // clean all todoJobView
                    listView.getItems().clear();
                    // readonly
                    readOnly.set(true);
                    // load the read-only ones
                    var readOnlyJobViewList = new ArrayList<TodoJobView>();
                    list.forEach(job -> {
                        var jobView = new TodoJobView(job, lang);
                        jobView.freeze(); // readonly
                        readOnlyJobViewList.add(jobView);
                        pageControlHBox.getChildren().forEach(btn -> {
                            btn.setDisable(true);
                        });
                        synchronized (currPageLock) {
                            currPage = 1;
                            currPageLabel.setText(String.valueOf(1));
                        }
                    });
                    _batchAddTodoJobViews(readOnlyJobViewList);
                    App.setTitle(App.STARTUP_TITLE + " " + "[Read-only Mode]");
                    toastInfo(String.format("Loaded %d TO-DOs (read-only)", list.size()));
                });
            });
        });
    }

    private void onExportHandler(ActionEvent e) {
        Platform.runLater(() -> {
            if (listView.getItems().isEmpty())
                return;

            // 1. pick date range
            LocalDate now = LocalDate.now();
            int daysAfterMonday = now.getDayOfWeek().getValue() - 1;
            LocalDate startDateToPick = daysAfterMonday == 0 ? now.minusWeeks(1) : now.minusDays(daysAfterMonday);
            DateRangeDialog dateRangeDialog = new DateRangeDialog(startDateToPick, now);

            var d = todoJobMapper.findEarliestDate();
            LocalDate earliestDate = d != null ? d : LocalDate.now();
            dateRangeDialog.showEarliestDate(earliestDate);
            var opt = dateRangeDialog.showAndWait();
            if (opt.isPresent()) {
                DateRange dr = opt.get();
                // 2. choose where to export
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(EXPORT_TODO_TITLE);
                fileChooser.setInitialFileName("Export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", "") + ".txt");
                fileChooser.getExtensionFilters().add(getTxtExtFilter());
                File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                if (nFile == null)
                    return;

                ioHandler.exportTodoJobAsync(todoJobMapper.findBetweenDates(dr.getStart(), dr.getEnd()), nFile, lang);
            }
        });
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
            if (readOnly.get())
                return;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(APPEND_TODO_TITLE);
            fileChooser.getExtensionFilters().add(getJsonExtFilter());
            File nFile = fileChooser.showOpenDialog(App.getPrimaryStage());
            if (nFile == null || !nFile.exists())
                return;

            CompletableFuture.supplyAsync(() -> {
                try {
                    var list = ioHandler.loadTodoJob(nFile);
                    list.forEach(job -> {
                        Integer id = todoJobMapper.insert(job);
                        if (id == null) {
                            toastError("Failed to add new to-do, please try again");
                            return;
                        }
                    });
                    return list.size();
                } catch (FailureToLoadException ex) {
                    ex.printStackTrace();
                    return 0;
                }
            }).thenAccept((todoCount) -> {
                toastInfo(String.format("Loaded %d TO-DOs", todoCount));
                if (todoCount > 0) {
                    synchronized (currPageLock) {
                        currPage = 0;
                    }
                    loadNextPage();
                }
            });
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
                toastInfo("Restart to apply the new configuration");
            }
            ioHandler.writeConfigAsync(config);
        });
    }
}



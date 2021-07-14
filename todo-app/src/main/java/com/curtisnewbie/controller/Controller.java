package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Environment;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.dao.MapperFactory;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.dao.TodoJobMapper;
import com.curtisnewbie.exception.FailureToLoadException;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerFactory;
import com.curtisnewbie.io.ObjectPrinter;
import com.curtisnewbie.io.TodoJobObjectPrinter;
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

    private final Environment environment;
    private final TodoJobMapper todoJobMapper = MapperFactory.getNewTodoJobMapper();
    private final ObjectPrinter<TodoJob> todojobExportObjectPrinter;

    @FXML
    private ListView<TodoJobView> listView;
    @FXML
    private HBox pageControlHBox;

    private final Object mutex = new Object();

    private volatile Dialog<?> aboutDialog;

    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final RedoStack redoStack = new RedoStack();

    /** record whether the current file is readonly */
    private static final AtomicBoolean readOnly = new AtomicBoolean(false);

    /** current page, need to be synchronised using {@link #currPageLock} */
    private int currPage = 1;
    private Object currPageLock = new Object();
    private Label currPageLabel = LabelFactory.classicLabel("1");
    private static final Logger logger = Logger.getLogger(Controller.class.getName());

    public Controller() {
        // read configuration from file
        Config config = ioHandler.readConfig();

        // get properties loader, who has already loaded all properties
        PropertiesLoader props = PropertiesLoader.getInstance();

        // setup environment
        this.environment = new Environment(config);

        // setup todojob's printer
        this.todojobExportObjectPrinter = new TodoJobObjectPrinter(props, environment);

        // load text and titles based on configured language
        GITHUB_ABOUT = props.get(APP_GITHUB);
        AUTHOR_ABOUT = props.get(APP_AUTHOR);
        CHOOSE_LANGUAGE_TITLE = props.get(TITLE_CHOOSE_LANGUAGE_PREFIX, environment.getLanguage());
        EXPORT_TODO_TITLE = props.get(TITLE_EXPORT_TODO_PREFIX, environment.getLanguage());
        BACKUP_TODO_TITLE = props.get(TITLE_BACKUP_TODO_PREFIX, environment.getLanguage());
        APPEND_TODO_TITLE = props.get(TITLE_APPEND_TODO_PREFIX, environment.getLanguage());
        READ_TODO_TITLE = props.get(TITLE_READ_TODO_PREFIX, environment.getLanguage());
        SAVE_PATH_TITLE = props.get(TITLE_SAVE_PATH_PREFIX, environment.getLanguage());
        CONFIG_PATH_TITLE = props.get(TITLE_CONFIG_PATH_PREFIX, environment.getLanguage());
        ADD_NEW_TODO_TITLE = props.get(TITLE_ADD_NEW_TODO_PREFIX, environment.getLanguage());
        UPDATE_TODO_NAME_TITLE = props.get(TITLE_UPDATE_TODO_NAME_PREFIX, environment.getLanguage());
        ADD_TITLE = props.get(TITLE_ADD_PREFIX, environment.getLanguage());
        DELETE_TITLE = props.get(TITLE_DELETE_PREFIX, environment.getLanguage());
        UPDATE_TITLE = props.get(TITLE_UPDATE_PREFIX, environment.getLanguage());
        COPY_TITLE = props.get(TITLE_COPY_PREFIX, environment.getLanguage());
        BACKUP_TITLE = props.get(TITLE_BACKUP_PREFIX, environment.getLanguage());
        EXPORT_TITLE = props.get(TITLE_EXPORT_PREFIX, environment.getLanguage());
        APPEND_TITLE = props.get(TITLE_APPEND_PREFIX, environment.getLanguage());
        LOAD_TITLE = props.get(TITLE_LOAD_PREFIX, environment.getLanguage());
        ABOUT_TITLE = props.get(TITLE_ABOUT_PREFIX, environment.getLanguage());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // register a ContextMenu for the ListView
        listView.setContextMenu(createCtxMenu());
        // register ctrl+? key event handler for ListView
        registerCtrlKeyHandler(listView);
        // registers event handlers for paging
        Button prevPageBtn = ButtonFactory.getArrowLeftBtn();
        prevPageBtn.setOnAction(e -> {
            loadPrevPage();
        });
        Button nextPageBtn = ButtonFactory.getArrowRightBtn();
        nextPageBtn.setOnAction(e -> {
            loadNextPage();
        });
        pageControlHBox.setAlignment(Pos.BASELINE_RIGHT);
        pageControlHBox.getChildren().addAll(LabelFactory.classicLabel("Page:"), MarginFactory.fixedMargin(10),
                currPageLabel, MarginFactory.fixedMargin(10),
                prevPageBtn, MarginFactory.fixedMargin(10),
                nextPageBtn, MarginFactory.fixedMargin(10));

        // load the first page
        CompletableFuture.runAsync(
                this::loadCurrPage
        );
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
                jobViewList.add(new TodoJobView(j, environment));
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
                jobViewList.add(new TodoJobView(j, environment));
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
                return jobList;
            }
        }).thenAccept((jobList) -> {
            if (jobList.isEmpty())
                return;
            var jobViewList = new ArrayList<TodoJobView>();
            for (TodoJob j : jobList) {
                jobViewList.add(new TodoJobView(j, environment));
            }
            Platform.runLater(() -> {
                listView.getItems().clear();
                jobViewList.forEach(jv -> {
                    addTodoJobView(jv);
                });
                listView.requestFocus();
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
        jobView.registerCheckboxEventHandler(() -> {
            int c = todoJobMapper.updateById(jobView.createTodoJobCopy());
            if (c <= 0)
                toastError("Failed to update to-do, please try again");
        });
        Platform.runLater(() -> {
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(PADDING));
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(PADDING).subtract(TodoJobView.WIDTH_OTHER_THAN_TEXT));
            listView.getItems().add(jobView);
            jobView.requestFocus();
            jobView.requestLayout();
        });
    }

    // only used on application startup, or loading read-only todos
    private void _batchAddTodoJobViews(List<TodoJobView> jobViews) {
        jobViews.forEach(jobView -> {
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(PADDING));
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(PADDING).subtract(TodoJobView.WIDTH_OTHER_THAN_TEXT));
            listView.getItems().add(jobView);
        });
    }

    private CnvCtxMenu createCtxMenu() {
        CnvCtxMenu ctxMenu = new CnvCtxMenu();
        ctxMenu.addMenuItem(ADD_TITLE, this::onAddHandler).addMenuItem(DELETE_TITLE, this::onDeleteHandler)
                .addMenuItem(UPDATE_TITLE, this::onUpdateHandler).addMenuItem(COPY_TITLE, this::onCopyHandler)
                .addMenuItem(APPEND_TITLE, this::onAppendHandler).addMenuItem(LOAD_TITLE, this::onReadHandler)
                .addMenuItem(EXPORT_TITLE, this::onExportHandler).addMenuItem(ABOUT_TITLE, this::onAboutHandler)
                .addMenuItem(CHOOSE_LANGUAGE_TITLE, this::onLanguageHandler);
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
                    addTodoJobView(new TodoJobView(job, environment));
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
            TodoJobDialog dialog = new TodoJobDialog(TodoJobDialog.DialogType.ADD_TODO_JOB, null);
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
                addTodoJobView(new TodoJobView(newTodo, environment));
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
                TodoJob old = jobView.createTodoJobCopy();
                TodoJobDialog dialog = new TodoJobDialog(TodoJobDialog.DialogType.UPDATE_TODO_JOB,
                        jobView.createTodoJobCopy());
                dialog.setTitle(UPDATE_TODO_NAME_TITLE);
                Optional<TodoJob> result = dialog.showAndWait();
                if (result.isPresent()) {
                    var updated = result.get();
                    updated.setDone(old.isDone());
                    updated.setId(old.getId());
                    if (todoJobMapper.updateById(updated) > 0) {
                        jobView.setName(updated.getName());
                        jobView.setExpectedEndDate(updated.getExpectedEndDate());
                        jobView.setActualEndDate(updated.getActualEndDate());
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
                            int id = listView.getItems().get(selected).getTodoJobId();
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
                copyToClipBoard(
                        todojobExportObjectPrinter.printObject(listView.getItems().get(selected).createTodoJobCopy())
                );
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
                        var jobView = new TodoJobView(job, environment);
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

            dateRangeDialog.showEarliestDate(todoJobMapper.findEarliestDate());
            dateRangeDialog.showLatestDate(todoJobMapper.findLatestDate());
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

                ioHandler.writeObjectsAsync(todoJobMapper.findBetweenDates(dr.getStart(), dr.getEnd()),
                        todojobExportObjectPrinter,
                        nFile);
            }
        });
    }

    private void onAboutHandler(ActionEvent e) {
        if (aboutDialog == null) {
            synchronized (mutex) {
                aboutDialog = new Alert(Alert.AlertType.INFORMATION);
                GridPane gPane = new GridPane();
                aboutDialog.setTitle(ABOUT_TITLE);
                gPane.add(getClassicTextWithPadding(String.format("%s: '%s'", CONFIG_PATH_TITLE, ioHandler.getConfPath())), 0, 0);
                gPane.add(getClassicTextWithPadding(String.format("%s: '%s'", SAVE_PATH_TITLE, MapperFactory.getDatabaseAbsolutePath())), 0, 1);
                gPane.add(getClassicTextWithPadding(GITHUB_ABOUT), 0, 2);
                gPane.add(getClassicTextWithPadding(AUTHOR_ABOUT), 0, 3);
                aboutDialog.getDialogPane().setContent(gPane);
                DialogUtil.disableHeader(aboutDialog);
            }
        }

        Platform.runLater(() -> {
            aboutDialog.show();
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
                        currPage = 1;
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
                if (opt.get().equals(engChoice) && !environment.getLanguage().equals(Language.ENG)) {
                    environment.setLanguage(Language.ENG);
                } else {
                    environment.setLanguage(Language.CHN);
                }
                toastInfo("Restart to apply the new configuration");
            }
            ioHandler.writeConfigAsync(new Config(environment));
        });
    }
}



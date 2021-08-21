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

    private static final Logger logger = Logger.getLogger(Controller.class.getName());

    private static final int LISTVIEW_PADDING = 55;
    private final String GITHUB_ABOUT;
    private final String AUTHOR_ABOUT;

    private final Environment environment;
    private final TodoJobMapper todoJobMapper = MapperFactory.getNewTodoJobMapper();
    private final ObjectPrinter<TodoJob> todoJobExportObjectPrinter;
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final RedoStack redoStack = new RedoStack();
    private final PropertiesLoader properties = PropertiesLoader.getInstance();

    @FxThreadConfinement
    @FXML
    private ListView<TodoJobView> listView;

    @FxThreadConfinement
    @FXML
    private HBox pageControlHBox;

    // todo create a separate component for 'searchBox' for maintainability
    @FxThreadConfinement
    @FXML
    private HBox searchHBox;

    @FxThreadConfinement
    private volatile String searchedText = "";

    @FxThreadConfinement
    private final Label currPageLabel = LabelFactory.classicLabel("1");

    /** current page */
    @FxThreadConfinement
    private static volatile int volatileCurrPage = 1;

    /** record whether it's the first time that the current page being loaded */
    private static final AtomicBoolean firstTimeLoadingCurrPage = new AtomicBoolean(true);

    /** record whether the current file is readonly */
    private static final AtomicBoolean readOnly = new AtomicBoolean(false);

    public Controller() {
        // read configuration from file
        Config config = ioHandler.readConfig();

        // setup environment
        this.environment = new Environment(config);

        // get properties loader singleton
        properties.changeToLocale(environment.getLanguage().locale);

        // setup todojob's printer
        this.todoJobExportObjectPrinter = new TodoJobObjectPrinter(properties, environment);

        // load text and titles based on configured language
        GITHUB_ABOUT = properties.getCommonProperty(APP_GITHUB);
        AUTHOR_ABOUT = properties.getCommonProperty(APP_AUTHOR);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // register a ContextMenu for the ListView
        registerContextMenu();
        // register key pressed event handler for ListView
        registerKeyPressedEventHandler();
        // setup control panel for pagination
        setupPageControlPanel();
        // setup search box
        setupSearchBox();
        // load the first page
        this.reloadCurrPageAsync();
    }

    /**
     * Load next page asynchronously
     */
    private void loadNextPageAsync() {
        CompletableFuture.supplyAsync(() -> {
            return todoJobMapper.findByPage(searchedText, volatileCurrPage + 1);
        }).thenAccept((jobList) -> {
            // it's the last page
            if (jobList.isEmpty())
                return;

            Platform.runLater(() -> {
                volatileCurrPage += 1;
                currPageLabel.setText(String.valueOf(volatileCurrPage));
                var jobViewList = new ArrayList<TodoJobView>();
                for (TodoJob j : jobList) {
                    jobViewList.add(new TodoJobView(j, environment));
                }
                listView.getItems().clear();
                jobViewList.forEach(jv -> {
                    addTodoJobView(jv);
                });
            });
        }).handle((v, e) -> {
            if (e != null) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Load previous page asynchronously
     */
    private void loadPrevPageAsync() {
        CompletableFuture.supplyAsync(() -> {
            if (volatileCurrPage <= 1)
                return null;
            return todoJobMapper.findByPage(searchedText, volatileCurrPage - 1);
        }).thenAccept((jobList) -> {
            if (jobList == null || jobList.isEmpty())
                return;

            Platform.runLater(() -> {
                volatileCurrPage -= 1;
                currPageLabel.setText(String.valueOf(volatileCurrPage));
                var jobViewList = new ArrayList<TodoJobView>();
                for (TodoJob j : jobList) {
                    jobViewList.add(new TodoJobView(j, environment));
                }
                listView.getItems().clear();
                jobViewList.forEach(jv -> {
                    addTodoJobView(jv);
                });
            });
        }).handle((v, e) -> {
            if (e != null) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Reload current page asynchronously
     */
    private void reloadCurrPageAsync() {
        CompletableFuture.supplyAsync(() -> {
            return todoJobMapper.findByPage(searchedText, volatileCurrPage);
        }).thenAccept((jobList) -> {
            boolean isFirstTimeLoading = firstTimeLoadingCurrPage.compareAndSet(true, false);
            // empty page
            if (jobList.isEmpty()) {
                // check if it's the first time loading current page
                if (isFirstTimeLoading) {
                    TodoJob welcomeTodo = new TodoJob();
                    welcomeTodo.setName("Welcome using this TODO app! :D");
                    welcomeTodo.setExpectedEndDate(LocalDate.now());
                    welcomeTodo.setDone(false);
                    Integer id = todoJobMapper.insert(welcomeTodo);
                    if (id != 0) {
                        welcomeTodo.setId(id);
                        addTodoJobView(new TodoJobView(welcomeTodo, environment));
                    }
                }
                Platform.runLater(() -> {
                    this.listView.getItems().clear();
                });
                return;
            }

            Platform.runLater(() -> {
                var jobViewList = new ArrayList<TodoJobView>();
                for (TodoJob j : jobList) {
                    jobViewList.add(new TodoJobView(j, environment));
                }
                listView.getItems().clear();
                jobViewList.forEach(jv -> {
                    addTodoJobView(jv);
                });
            });
        }).handle((v, e) -> {
            if (e != null) {
                e.printStackTrace();
            }
            return null;
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
        Platform.runLater(() -> {
            jobView.onModelChange((evt -> {
                int c = todoJobMapper.updateById((TodoJob) evt.getNewValue());
                if (c <= 0)
                    toastError("Failed to update to-do, please try again");
                reloadCurrPageAsync();
            }));
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(LISTVIEW_PADDING));
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(LISTVIEW_PADDING)
                    .subtract(Integer.parseInt(properties.getLocalizedProperty(TODO_VIEW_TEXT_WRAP_WIDTH_KEY))));
            listView.getItems().add(jobView);
        });
    }

    // only used on application startup, or loading read-only todos
    private void _batchAddTodoJobViews(List<TodoJobView> jobViews) {
        jobViews.forEach(jobView -> {
            Platform.runLater(() -> {
                jobView.prefWidthProperty().bind(listView.widthProperty().subtract(LISTVIEW_PADDING));
                jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(LISTVIEW_PADDING)
                        .subtract(Integer.parseInt(properties.getLocalizedProperty(TODO_VIEW_TEXT_WRAP_WIDTH_KEY))));
                listView.getItems().add(jobView);
            });
        });
    }

    private CnvCtxMenu createCtxMenu() {
        CnvCtxMenu ctxMenu = new CnvCtxMenu();
        ctxMenu.addMenuItem(properties.getLocalizedProperty(TITLE_ADD_KEY), this::onAddHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_DELETE_KEY), this::onDeleteHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_UPDATE_KEY), this::onUpdateHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_COPY_KEY), this::onCopyHandler)
//                .addMenuItem(properties.getLocalizedProperty(TITLE_APPEND_KEY), this::onAppendHandler)
//                .addMenuItem(properties.getLocalizedProperty(TITLE_LOAD_KEY), this::onReadHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_EXPORT_KEY), this::onExportHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_ABOUT_KEY), this::onAboutHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_CHOOSE_LANGUAGE_KEY), this::onLanguageHandler);
        return ctxMenu;
    }

    /**
     * <p>
     * Register ctrl+? key event handler for ListView
     * </p>
     * <p>
     * E.g., Ctrl+z, triggers {@link #redo()} for redoing previous action if possible
     * </p>
     */
    private void registerKeyPressedEventHandler() {
        listView.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                if (e.getCode().equals(KeyCode.Z)) {
                    if (readOnly.get())
                        return;
                    redo();
                } else if (e.getCode().equals(KeyCode.C)) {
                    copySelected();
                }
            } else {
                if (e.getCode().equals(KeyCode.DELETE)) {
                    if (readOnly.get())
                        return;
                    deleteSelected();
                } else if (e.getCode().equals(KeyCode.F5)) {
                    if (readOnly.get())
                        return;
                    reloadCurrPageAsync();
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
        Objects.requireNonNull(msg);
        _toast(msg, Alert.AlertType.INFORMATION);
    }

    private void toastError(String msg) {
        Objects.requireNonNull(msg);
        msg = "ERROR: " + msg;
        _toast(msg, Alert.AlertType.ERROR);
    }

    private void _toast(String msg, Alert.AlertType type) {
        Objects.requireNonNull(msg);
        Objects.requireNonNull(type);

        Platform.runLater(() -> {
            var alert = new Alert(type);
            alert.setResizable(true);
            Label label = new Label(msg);
            alert.getDialogPane().setContent(label);
            DialogUtil.disableHeader(alert);
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
            dialog.setTitle(properties.getLocalizedProperty(TITLE_ADD_NEW_TODO_KEY));
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
            reloadCurrPageAsync();
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
                dialog.setTitle(properties.getLocalizedProperty(TITLE_UPDATE_TODO_NAME_KEY));
                Optional<TodoJob> result = dialog.showAndWait();
                if (result.isPresent()) {
                    var updated = result.get();
                    updated.setDone(old.isDone());
                    updated.setId(old.getId());
                    if (todoJobMapper.updateById(updated) > 0) {
                        jobView.setName(updated.getName());
                        jobView.setExpectedEndDate(updated.getExpectedEndDate());
                        jobView.setActualEndDate(updated.getActualEndDate());
                        // reload current page only when it's actually updated
                        reloadCurrPageAsync();
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
                alert.setTitle(properties.getLocalizedProperty(TITLE_DELETE_KEY));
                alert.setContentText(properties.getLocalizedProperty(TEXT_DELETE_CONFIRM_KEY));
                DialogUtil.disableHeader(alert);
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
            reloadCurrPageAsync();
        });
    }

    private void onCopyHandler(ActionEvent e) {
        copySelected();
    }

    private void copySelected() {
        Platform.runLater(() -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0)
                copyToClipBoard(
                        todoJobExportObjectPrinter.printObject(listView.getItems().get(selected).createTodoJobCopy())
                );
        });
    }

    private void onReadHandler(ActionEvent e) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(properties.getLocalizedProperty(TITLE_READ_TODO_KEY));
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
                        volatileCurrPage = 1;
                        currPageLabel.setText(String.valueOf(volatileCurrPage));
                    });
                    _batchAddTodoJobViews(readOnlyJobViewList);
                    App.setTitle(App.STARTUP_TITLE + " " + "[Read-only Mode]");
                    toastInfo(String.format("Loaded %d TO-DOs (read-only)", list.size()));
                });
            });
        });
    }

    private void onExportHandler(ActionEvent e) {
        CompletableFuture.supplyAsync(() -> {
            LocalDate earliestDate = todoJobMapper.findEarliestDate();
            LocalDate latestDate = todoJobMapper.findLatestDate();
            return new Pair(earliestDate, latestDate);
        }).thenAccept(p -> {
            Pair<LocalDate, LocalDate> pair = (Pair<LocalDate, LocalDate>) p;
            Platform.runLater(() -> {
                if (listView.getItems().isEmpty())
                    return;

                // 1. pick date range
                LocalDate now = LocalDate.now();
                int daysAfterMonday = now.getDayOfWeek().getValue() - 1;
                LocalDate startDateToPick = daysAfterMonday == 0 ? now.minusWeeks(1) : now.minusDays(daysAfterMonday);
                DateRangeDialog dateRangeDialog = new DateRangeDialog(startDateToPick, now);

                dateRangeDialog.showEarliestDate(pair.getLeft());
                dateRangeDialog.showLatestDate(pair.getRight());
                var opt = dateRangeDialog.showAndWait();
                if (opt.isPresent()) {
                    DateRange dr = opt.get();
                    // 2. choose where to export
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(properties.getLocalizedProperty(TITLE_EXPORT_TODO_KEY));
                    fileChooser.setInitialFileName("Export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", "") + ".txt");
                    fileChooser.getExtensionFilters().add(getTxtExtFilter());
                    File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                    if (nFile == null)
                        return;

                    ioHandler.writeObjectsAsync(todoJobMapper.findBetweenDates(searchedText, dr.getStart(), dr.getEnd()),
                            todoJobExportObjectPrinter,
                            nFile);
                }
            });
        });
    }

    private void onAboutHandler(ActionEvent e) {
        Platform.runLater(() -> {
            Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
            GridPane gPane = new GridPane();
            aboutDialog.setTitle(properties.getLocalizedProperty(TITLE_ABOUT_KEY));
            gPane.add(getClassicTextWithPadding(
                    String.format("%s: '%s'", properties.getLocalizedProperty(TITLE_CONFIG_PATH_KEY), ioHandler.getConfPath())),
                    0, 0);
            gPane.add(getClassicTextWithPadding(
                    String.format("%s: '%s'", properties.getLocalizedProperty(TITLE_SAVE_PATH_KEY), MapperFactory.getDatabaseAbsolutePath())),
                    0, 1);
            gPane.add(getClassicTextWithPadding(GITHUB_ABOUT), 0, 2);
            gPane.add(getClassicTextWithPadding(AUTHOR_ABOUT), 0, 3);
            aboutDialog.getDialogPane().setContent(gPane);
            DialogUtil.disableHeader(aboutDialog);
            aboutDialog.show();
        });
    }

    private void onAppendHandler(ActionEvent e) {
        Platform.runLater(() -> {
            if (readOnly.get())
                return;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(properties.getLocalizedProperty(TITLE_APPEND_TODO_KEY));
            fileChooser.getExtensionFilters().add(getJsonExtFilter());
            File nFile = fileChooser.showOpenDialog(App.getPrimaryStage());
            if (nFile == null || !nFile.exists()) {
                logger.info("No file selected, abort operation");
                return;
            }

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
                    volatileCurrPage = 1;
                    loadNextPageAsync();
                }
            });
        });
    }

    private void onLanguageHandler(ActionEvent e) {
        String engChoice = "English";
        String chnChoice = "中文";
        Platform.runLater(() -> {
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(properties.getLocalizedProperty(TITLE_CHOOSE_LANGUAGE_KEY));
            choiceDialog.setSelectedItem(engChoice);
            choiceDialog.getItems().add(engChoice);
            choiceDialog.getItems().add(chnChoice);
            DialogUtil.disableHeader(choiceDialog);
            Optional<String> opt = choiceDialog.showAndWait();
            if (opt.isPresent()) {
                if (opt.get().equals(engChoice) && !environment.getLanguage().equals(Language.ENG)) {
                    environment.setLanguage(Language.ENG);
                } else {
                    environment.setLanguage(Language.CHN);
                }
                // reload resource bundle for the updated locale
                properties.changeToLocale(environment.getLanguage().locale);
                // reload current page
                reloadCurrPageAsync();
                // override the previous menu
                registerContextMenu();
            }
            ioHandler.writeConfigAsync(new Config(environment));
        });
    }

    private void registerContextMenu() {
        Platform.runLater(() -> {
            listView.setContextMenu(createCtxMenu());
        });
    }

    private void setupPageControlPanel() {
        // registers event handlers for paging
        Button prevPageBtn = ButtonFactory.getArrowLeftBtn();
        prevPageBtn.setOnAction(e -> {
            loadPrevPageAsync();
        });
        Button nextPageBtn = ButtonFactory.getArrowRightBtn();
        nextPageBtn.setOnAction(e -> {
            loadNextPageAsync();
        });
        pageControlHBox.setAlignment(Pos.BASELINE_RIGHT);
        pageControlHBox.getChildren().addAll(LabelFactory.classicLabel(properties.getLocalizedProperty(TEXT_SEARCH)),
                MarginFactory.fixedMargin(10),
                currPageLabel, MarginFactory.fixedMargin(10),
                prevPageBtn, MarginFactory.fixedMargin(10),
                nextPageBtn, MarginFactory.fixedMargin(10));
    }

    private void setupSearchBox() {
        TextField tf = new TextField();
        tf.prefWidthProperty().bind(listView.widthProperty().subtract(200));
        tf.setOnKeyReleased(e -> {
            String changed = tf.getText();
            if (!Objects.equals(searchedText, changed))
                searchedText = changed;

            if (e.getCode().equals(KeyCode.ENTER))
                reloadCurrPageAsync();
        });

        Button closeBtn = ButtonFactory.getCloseBtn();
        closeBtn.setOnAction(e -> {
            this.searchedText = "";
            tf.clear();
            reloadCurrPageAsync();
        });

        searchHBox.setAlignment(Pos.BASELINE_RIGHT);
        searchHBox.getChildren().addAll(
                MarginFactory.fixedMargin(20),
                LabelFactory.classicLabel(properties.getLocalizedProperty(TEXT_SEARCH)),
                MarginFactory.fixedMargin(10),
                tf,
                MarginFactory.expandingMargin(),
                MarginFactory.fixedMargin(10),
                closeBtn
        );
    }
}



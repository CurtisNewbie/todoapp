package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Environment;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.dao.MapperFactory;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.dao.TodoJobMapper;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerFactory;
import com.curtisnewbie.io.ObjectPrinter;
import com.curtisnewbie.io.TodoJobObjectPrinter;
import com.curtisnewbie.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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
@Slf4j
public class Controller {

    private static final int LISTVIEW_PADDING = 55;
    private String GITHUB_ABOUT;
    private String AUTHOR_ABOUT;

    private final Environment environment;
    private final TodoJobMapper todoJobMapper = MapperFactory.getNewTodoJobMapper();
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final RedoStack redoStack = new RedoStack();
    private final PropertiesLoader properties = PropertiesLoader.getInstance();
    private ObjectPrinter<TodoJob> todoJobExportObjectPrinter;

    @FxThreadConfinement
    private ListView<TodoJobView> listView = new ListView<>();

    @FxThreadConfinement
    private static volatile int volatileCurrPage = 1;

    @FxThreadConfinement
    private SearchBar searchBar;

    @FxThreadConfinement
    private PaginationBar paginationBar;

    @FxThreadConfinement
    private final BorderPane parent;

    /** record whether it's the first time that the current page being loaded */
    private final AtomicBoolean firstTimeLoadingCurrPage = new AtomicBoolean(true);

    /** record whether the current file is readonly */
    private final AtomicBoolean readOnly = new AtomicBoolean(false);

    /** subscription for a ticking interval */
    private volatile Disposable tickSubscription;

    // we only need a single thread, there isn't much concurrency going on here in this map,
    // mainly the main thread and the Fx's thread
    private final ExecutorService taskExec = Executors.newFixedThreadPool(1);
    private final Scheduler taskScheduler = Schedulers.fromExecutor(taskExec);

    public Controller(BorderPane parent) {
        this.parent = parent;

        // read configuration from file
        Config config = ioHandler.readConfig();

        // setup environment
        this.environment = new Environment(config);

        // load locale-specific resource bundle
        properties.changeToLocale(environment.getLanguage().locale, () -> {
            // setup todojob's printer
            this.todoJobExportObjectPrinter = new TodoJobObjectPrinter(properties, environment);

            // load text and titles based on configured language
            GITHUB_ABOUT = properties.getCommonProperty(APP_GITHUB);
            AUTHOR_ABOUT = properties.getCommonProperty(APP_AUTHOR);
        });

        // setup control panel for pagination
        setupPaginationBar();
        // setup search bar
        setupSearchBar();
        // layout the components on borderpane
        layoutComponents();
        // register a ContextMenu for the ListView
        registerContextMenu();
        // register key pressed event handler for ListView
        registerKeyPressedEventHandler();
        // load the first page
        this.reloadCurrPageAsync();
        subscribeTickingFluxForReloading();
        // register shutdown hook
    }

    private void layoutComponents() {
        parent.setTop(searchBar);
        parent.setBottom(paginationBar);
        parent.setCenter(listView);
    }

    public static Controller initialize(BorderPane parent) {
        return new Controller(parent);
    }

    /**
     * Load next page asynchronously
     */
    private void loadNextPageAsync() {
        todoJobMapper.findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage + 1)
                .subscribeOn(taskScheduler)
                .subscribe(list -> {
                    if (!list.isEmpty()) {
                        Platform.runLater(() -> {
                            volatileCurrPage += 1;
                            paginationBar.setCurrPage(volatileCurrPage);
                            clearAndLoadList(list);
                        });
                    }
                });
    }

    /**
     * Load previous page asynchronously
     */
    private void loadPrevPageAsync() {
        if (volatileCurrPage <= 1)
            return;

        todoJobMapper.findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage - 1)
                .subscribeOn(taskScheduler)
                .subscribe(list -> {
                    if (!list.isEmpty()) {
                        Platform.runLater(() -> {
                            volatileCurrPage -= 1;
                            paginationBar.setCurrPage(volatileCurrPage);
                            clearAndLoadList(list);
                        });
                    }
                });
    }

    /**
     * Reload current page asynchronously
     */
    private void reloadCurrPageAsync() {
        todoJobMapper.findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage)
                .subscribeOn(taskScheduler)
                .subscribe(list -> {
                    boolean isFirstTimeLoading = firstTimeLoadingCurrPage.compareAndSet(true, false);

                    // insert a welcome to-do job if there is none and it's first time loading page (since app startup)
                    if (list.isEmpty() && isFirstTimeLoading) {
                        TodoJob welcomeTodo = new TodoJob();
                        welcomeTodo.setName("Welcome using this TODO app! :D");
                        welcomeTodo.setExpectedEndDate(LocalDate.now());
                        welcomeTodo.setDone(false);

                        todoJobMapper.insertAsync(welcomeTodo)
                                .subscribeOn(taskScheduler)
                                .subscribe(id -> {
                                    welcomeTodo.setId(id);
                                    list.add(welcomeTodo);
                                    clearAndLoadList(list);
                                });
                    }

                    if (!list.isEmpty()) {
                        clearAndLoadList(list);
                    }
                });
    }

    private void clearAndLoadList(List<TodoJob> list) {
        Platform.runLater(() -> {
            listView.getItems().clear();
            list.stream()
                    .map(t -> new TodoJobView(t, environment))
                    .forEach(tjv -> addTodoJobView(tjv));
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
                // executed in UI thread
                todoJobMapper.updateByIdAsync((TodoJob) evt.getNewValue())
                        .subscribe(isUpdated -> {
                            if (isUpdated) {
                                toastError("Failed to update to-do, please try again");
                                reloadCurrPageAsync();
                            }
                        });
            }));
            jobView.prefWidthProperty().bind(listView.widthProperty().subtract(LISTVIEW_PADDING));
            jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(LISTVIEW_PADDING)
                    .subtract(Integer.parseInt(properties.getLocalizedProperty(TODO_VIEW_TEXT_WRAP_WIDTH_KEY))));
            listView.getItems().add(jobView);
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
                } else if (e.getCode().equals(KeyCode.F)) {
                    Platform.runLater(() -> {
                        searchBar.getSearchTextField().requestFocus();
                    });
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
                todoJobMapper.insertAsync(redo.getTodoJob())
                        .subscribe(id -> {
                            if (id != null) {
                                reloadCurrPageAsync();
                            } else {
                                toastError("Unknown error happens when try to redo");
                            }
                        });

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

                    // executed in task scheduler, rather than in UI thread
                    todoJobMapper.updateByIdAsync(updated)
                            .subscribeOn(taskScheduler)
                            .subscribe(isUpdated -> {
                                if (isUpdated)
                                    reloadCurrPageAsync();
                                else
                                    toastError("Failed to update to-do, please try again");
                            });
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

                            // executed in UI thread because we want to access the listView
                            todoJobMapper.deleteByIdAsync(id)
                                    .subscribe(isDeleted -> {
                                        if (isDeleted) {
                                            var jobCopy = listView.getItems().remove(selected).createTodoJobCopy();
                                            synchronized (redoStack) {
                                                redoStack.push(new Redo(RedoType.DELETE, jobCopy));
                                            }
                                        } else {
                                            toastInfo("Failed to delete to-do, please try again");
                                        }
                                    });
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
            if (selected >= 0) {
                TodoJobView todoJobView = listView.getItems().get(selected);
                copyToClipBoard(todoJobView.getName());
//                        todoJobExportObjectPrinter.printObject(listView.getItems().get(selected).createTodoJobCopy())
            }
        });
    }

    private void onExportHandler(ActionEvent e) {
        Mono.zip(todoJobMapper.findEarliestDateAsync(), todoJobMapper.findLatestDateAsync())
                .subscribeOn(taskScheduler)
                .subscribe((tuple) -> {
                    Platform.runLater(() -> {
                        if (listView.getItems().isEmpty())
                            return;

                        // 1. pick date range
                        LocalDate now = LocalDate.now();
                        int daysAfterMonday = now.getDayOfWeek().getValue() - 1;
                        LocalDate startDateToPick = daysAfterMonday == 0 ? now.minusWeeks(1) : now.minusDays(daysAfterMonday);
                        DateRangeDialog dateRangeDialog = new DateRangeDialog(startDateToPick, now);

                        dateRangeDialog.showEarliestDate(tuple.getT1());
                        dateRangeDialog.showLatestDate(tuple.getT2());
                        var opt = dateRangeDialog.showAndWait();
                        if (opt.isPresent()) {
                            DateRange dr = opt.get();
                            // 2. choose where to export
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle(properties.getLocalizedProperty(TITLE_EXPORT_TODO_KEY));
                            fileChooser.setInitialFileName("Export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", "") + ".txt");
                            fileChooser.getExtensionFilters().add(getTxtExtFilter());
                            final File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                            if (nFile == null)
                                return;

                            final String searchedText = searchBar.getSearchTextField().getText();
                            todoJobMapper.findBetweenDatesAsync(searchedText, dr.getStart(), dr.getEnd())
                                    .subscribeOn(taskScheduler)
                                    .subscribe((list) -> {
                                        ioHandler.writeObjectsAsync(list,
                                                todoJobExportObjectPrinter,
                                                nFile);
                                    });
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

//    private void onAppendHandler(ActionEvent e) {
//        Platform.runLater(() -> {
//            if (readOnly.get())
//                return;
//            FileChooser fileChooser = new FileChooser();
//            fileChooser.setTitle(properties.getLocalizedProperty(TITLE_APPEND_TODO_KEY));
//            fileChooser.getExtensionFilters().add(getJsonExtFilter());
//            File nFile = fileChooser.showOpenDialog(App.getPrimaryStage());
//            if (nFile == null || !nFile.exists()) {
//                log.info("No file selected, abort operation");
//                return;
//            }
//
//            CompletableFuture.supplyAsync(() -> {
//                try {
//                    var list = ioHandler.loadTodoJob(nFile);
//                    list.forEach(job -> {
//                        Integer id = todoJobMapper.insert(job);
//                        if (id == null) {
//                            toastError("Failed to add new to-do, please try again");
//                            return;
//                        }
//                    });
//                    return list.size();
//                } catch (FailureToLoadException ex) {
//                    ex.printStackTrace();
//                    return 0;
//                }
//            }, taskExec).thenAccept((todoCount) -> {
//                toastInfo(String.format("Loaded %d TO-DOs", todoCount));
//                if (todoCount > 0) {
//                    volatileCurrPage = 1;
//                    loadNextPageAsync();
//                }
//            });
//        });
//    }

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
                properties.changeToLocale(environment.getLanguage().locale, () -> {
                    reloadCurrPageAsync();
                    // override the previous menu
                    registerContextMenu();
                    // override the previous pagination bar and search bar
                    setupPaginationBar();
                    setupSearchBar();
                    layoutComponents();
                });
            }
            ioHandler.writeConfigAsync(new Config(environment));
        });
    }

    private void registerContextMenu() {
        Platform.runLater(() -> {
            listView.setContextMenu(createCtxMenu());
        });
    }

    private void setupPaginationBar() {
        paginationBar = new PaginationBar();
        paginationBar.getPrevPageBtn().setOnAction(e -> {
            loadPrevPageAsync();
        });
        paginationBar.getNextPageBtn().setOnAction(e -> {
            loadNextPageAsync();
        });
    }

    private void setupSearchBar() {
        searchBar = new SearchBar();
        searchBar.searchTextFieldPrefWidthProperty().bind(listView.widthProperty().subtract(150));
        searchBar.onSearchTextFieldEnterPressed(() -> {
            Platform.runLater(() -> {
                if (searchBar.isSearchTextChanged()) {
                    searchBar.setSearchTextChanged(false);
                    volatileCurrPage = 1;
                    paginationBar.setCurrPage(volatileCurrPage);
                }
                reloadCurrPageAsync();
            });
        });

    }

    private void subscribeTickingFluxForReloading() {
        if (tickSubscription != null)
            // register a flux that ticks for every 5 minute
            tickSubscription = Flux.interval(Duration.ofMinutes(5)).subscribe((l) -> {
                // reload page
                reloadCurrPageAsync();
            });
    }
}



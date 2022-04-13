package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Environment;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.dao.MapperFactory;
import com.curtisnewbie.dao.MapperFactoryBase;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.TextFactory.selectableText;

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

    /** Properties Loader, thread-safe */
    private final PropertiesLoader properties = PropertiesLoader.getInstance();

    /** Github link in About */
    private final String GITHUB_ABOUT = properties.getCommonProperty(APP_GITHUB);
    /** Author in About */
    private final String AUTHOR_ABOUT = properties.getCommonProperty(APP_AUTHOR);

    /** Path to DB */
    private final String dbAbsPath;

    private final AtomicReference<TodoJobMapper> _todoJobMapper = new AtomicReference<>();

    /** IO Handler, thread-safe */
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    /** Redo stack, thread-safe */
    private final RedoStack redoStack = new RedoStack();
    /** ObjectPrinter for {@link TodoJob }, thread-safe */
    private final ObjectPrinter<TodoJob> todoJobExportObjectPrinter;

    /** record whether it's the first time that the current page being loaded */
    private final AtomicBoolean firstTimeLoadingCurrPage = new AtomicBoolean(true);

    /**
     * the last date used and updated by the {@link #_subscribeTickingFluxForReloading()}, must be synchronized using
     * {@link #lastTickDateLock}
     */
    private LocalDate lastTickDate = LocalDate.now();
    /** Lock for {@link #lastTickDate } */
    private final Object lastTickDateLock = new Object();

    /** Scheduler for reactor */
    private final Scheduler taskScheduler = Schedulers.fromExecutor(ForkJoinPool.commonPool());

    @FxThreadConfinement
    private final Environment environment;
    @FxThreadConfinement
    private final ListView<TodoJobView> listView = new ListView<>();
    @FxThreadConfinement
    private volatile int volatileCurrPage = 1;
    @FxThreadConfinement
    private SearchBar searchBar;
    @FxThreadConfinement
    private PaginationBar paginationBar;
    @FxThreadConfinement
    private final BorderPane innerPane;
    @FxThreadConfinement
    private QuickTodoBar quickTodoBar = new QuickTodoBar();
    @FxThreadConfinement
    private final BorderPane outerPane;

    /**
     * Create and bind the new Controller to a BorderPane
     */
    public Controller(BorderPane parent) {
        this.outerPane = parent;
        this.innerPane = new BorderPane();
        this.outerPane.setCenter(this.innerPane);

        final MapperFactory mapperFactory = new MapperFactoryBase();
        dbAbsPath = mapperFactory.getDatabaseAbsolutePath();

        /**
         * speed up initialisation process, we make it async, and we rely on the {@link #_todoJobMapper() } method to
         * delay the timing that we retrieve the mapper
         */
        mapperFactory.getNewTodoJobMapperAsync()
                .subscribeOn(taskScheduler)
                .subscribe((mapper) -> {
                    if (this._todoJobMapper.compareAndSet(null, mapper)) {
                        log.info("TodoJobMapper loaded");
                    }
                });

        // read configuration from file, setup environment
        this.environment = new Environment(ioHandler.readConfig());

        // load locale-specific resource bundle
        properties.changeToLocale(environment.getLanguage().locale);

        // setup to-do job printer
        this.todoJobExportObjectPrinter = new TodoJobObjectPrinter(properties, environment);

        // setup quick to-do text area
        _setupQuickTodoBar();
        // setup control panel for pagination
        _setupPaginationBar(1);
        // setup search bar
        _setupSearchBar();
        // layout the components on borderpane
        layoutComponents();
        // register a ContextMenu for the ListView
        _registerContextMenu();
        // register key pressed event handler for ListView
        _registerKeyPressedEventHandler();
        // reload page when we are on next day
        _subscribeTickingFluxForReloading();

        // load the first page
        this.reloadCurrPageAsync();
    }

    private void layoutComponents() {
        innerPane.setTop(searchBar);
        innerPane.setBottom(paginationBar);
        innerPane.setCenter(listView);
    }

    public static Controller initialize(BorderPane parent) {
        return new Controller(parent);
    }

    /**
     * Load next page asynchronously
     */
    private void loadNextPageAsync() {
        _todoJobMapper().findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage + 1)
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

        _todoJobMapper().findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage - 1)
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
        _todoJobMapper().findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage)
                .subscribeOn(taskScheduler)
                .subscribe(list -> {
                    boolean isFirstTimeLoading = firstTimeLoadingCurrPage.compareAndSet(true, false);

                    // insert a welcome to-do job if there is none and it's first time loading page (since app startup)
                    if (list.isEmpty() && isFirstTimeLoading) {
                        TodoJob welcomeTodo = new TodoJob();
                        welcomeTodo.setName("Welcome using this TODO app! :D");
                        welcomeTodo.setExpectedEndDate(LocalDate.now());
                        welcomeTodo.setDone(false);
                        welcomeTodo.setId(_todoJobMapper().insert(welcomeTodo));
                        list.add(welcomeTodo);
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
     */
    private void addTodoJobView(TodoJobView jobView) {
        Platform.runLater(() -> {
            jobView.onModelChange((evt -> {
                _todoJobMapper().updateByIdAsync((TodoJob) evt.getNewValue())
                        .subscribeOn(taskScheduler)
                        .subscribe(isUpdated -> {
                            if (isUpdated)
                                reloadCurrPageAsync();
                            else
                                toastError("Failed to update to-do, please try again");
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
        ctxMenu.addMenuItem(properties.getLocalizedProperty(TITLE_ADD_KEY), this::_onAddHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_DELETE_KEY), this::_onDeleteHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_UPDATE_KEY), this::_onUpdateHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_COPY_KEY), this::_onCopyHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_EXPORT_KEY), this::_onExportHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_CHOOSE_LANGUAGE_KEY), this::_onLanguageHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_CHOOSE_SEARCH_ON_TYPE_KEY), this::_searchOnTypingConfigHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_EXPORT_PATTERN_KEY), this::_onChangeExportPatternHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_SWITCH_QUICK_TODO_KEY), this::_onSwitchQuickTodoHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_ABOUT_KEY), this::_onAboutHandler);
        return ctxMenu;
    }

    private void _onSwitchQuickTodoHandler(ActionEvent e) {
        Platform.runLater(() -> {
            if (this.outerPane.getTop() != null)
                this.outerPane.setTop(null);
            else
                this.outerPane.setTop(quickTodoBar);
        });
    }

    /**
     * <p>
     * Register ctrl+? key event handler for ListView
     * </p>
     * <p>
     * E.g., Ctrl+z, triggers {@link #redo()} for redoing previous action if possible
     * </p>
     */
    private void _registerKeyPressedEventHandler() {
        listView.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                if (e.getCode().equals(KeyCode.Z)) {
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
                    deleteSelected();
                } else if (e.getCode().equals(KeyCode.F5)) {
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
                doInsertTodo(redo.getTodoJob());
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
            Alert alert = new Alert(type);
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

    private void _onAddHandler(ActionEvent e) {
        Platform.runLater(() -> {

            TodoJobDialog dialog = new TodoJobDialog(TodoJobDialog.DialogType.ADD_TODO_JOB, null);
            dialog.setTitle(properties.getLocalizedProperty(TITLE_ADD_NEW_TODO_KEY));
            Optional<TodoJob> result = dialog.showAndWait();
            if (result.isPresent() && !StrUtil.isEmpty(result.get().getName())) {
                TodoJob newTodo = result.get();

                _todoJobMapper().insertAsync(newTodo)
                        .subscribeOn(taskScheduler)
                        .subscribe(id -> {
                            reloadCurrPageAsync();
                        }, (err) -> {
                            toastError("Failed to add new to-do, please try again");
                            log.error("Failed to add new to-do", err);
                        });
            }
        });
    }

    private void _onUpdateHandler(ActionEvent e) {
        Platform.runLater(() -> {
            final int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                TodoJobView jobView = listView.getItems().get(selected);
                TodoJob old = jobView.createTodoJobCopy();
                TodoJobDialog dialog = new TodoJobDialog(TodoJobDialog.DialogType.UPDATE_TODO_JOB,
                        jobView.createTodoJobCopy());
                dialog.setTitle(properties.getLocalizedProperty(TITLE_UPDATE_TODO_NAME_KEY));
                Optional<TodoJob> result = dialog.showAndWait();
                if (result.isPresent()) {

                    final TodoJob updated = result.get();
                    updated.setDone(old.isDone());
                    updated.setId(old.getId());

                    // executed in task scheduler, rather than in UI thread
                    _todoJobMapper().updateByIdAsync(updated)
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

    private void _onDeleteHandler(ActionEvent e) {
        deleteSelected();
    }

    private void deleteSelected() {
        Platform.runLater(() -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                final TodoJobView tjv = listView.getItems().get(selected);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setResizable(true);
                alert.setTitle(properties.getLocalizedProperty(TITLE_DELETE_KEY));
                alert.setContentText(properties.getLocalizedProperty(TEXT_DELETE_CONFIRM_KEY) + "\n\n" + tjv.getName() + "\n");
                DialogUtil.disableHeader(alert);
                alert.showAndWait()
                        .filter(resp -> resp == ButtonType.OK)
                        .ifPresent(resp -> {
                            // executed in UI thread because we want to access the listView
                            _todoJobMapper().deleteByIdAsync(tjv.getTodoJobId())
                                    .subscribe(isDeleted -> {
                                        if (isDeleted) {
                                            TodoJob jobCopy = listView.getItems().remove(selected).createTodoJobCopy();
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

    private void _onCopyHandler(ActionEvent e) {
        copySelected();
    }

    private void copySelected() {
        Platform.runLater(() -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                TodoJobView todoJobView = listView.getItems().get(selected);
                copyToClipBoard(todoJobView.getName());
            }
        });
    }

    private void _onExportHandler(ActionEvent e) {
        Mono.zip(_todoJobMapper().findEarliestDateAsync(), _todoJobMapper().findLatestDateAsync())
                .subscribeOn(taskScheduler)
                .subscribe((tuple) -> {
                    Platform.runLater(() -> {
                        if (listView.getItems().isEmpty())
                            return;

                        // 1. pick date range and searched text
                        LocalDate now = LocalDate.now();
                        int daysAfterMonday = now.getDayOfWeek().getValue() - 1;
                        LocalDate startDateToPick = daysAfterMonday == 0 ? now.minusWeeks(1) : now.minusDays(daysAfterMonday);

                        final ExportDialog dialog = new ExportDialog(startDateToPick, now, searchBar.getSearchTextField().getText());
                        dialog.showEarliestDate(tuple.getT1());
                        dialog.showLatestDate(tuple.getT2());
                        Optional<ExportDialog.ExportParam> opt = dialog.showAndWait();
                        if (opt.isPresent()) {
                            final ExportDialog.ExportParam ep = opt.get();

                            // 2. choose where to export
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle(properties.getLocalizedProperty(TITLE_EXPORT_TODO_KEY));
                            fileChooser.setInitialFileName("Export_" + DateUtil.toLongDateStrDash(new Date()).replace(":", "") + ".txt");
                            fileChooser.getExtensionFilters().add(getTxtExtFilter());
                            final File nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                            if (nFile == null)
                                return;

                            final String exportPattern = environment.getPattern(); // nullable
                            final DateRange dateRange = ep.getDateRange();
                            _todoJobMapper().findBetweenDatesAsync(ep.getSearchText(), dateRange.getStart(), dateRange.getEnd())
                                    .subscribeOn(taskScheduler)
                                    .subscribe((list) -> ioHandler.writeObjectsAsync(list, todo -> todoJobExportObjectPrinter.printObject(todo, exportPattern), nFile));
                        }
                    });
                });
    }

    private void _onAboutHandler(ActionEvent e) {
        Platform.runLater(() -> {
            Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
            GridPane gPane = new GridPane();
            aboutDialog.setTitle(properties.getLocalizedProperty(TITLE_ABOUT_KEY));

            String desc = String.format("%s: '%s'", properties.getLocalizedProperty(TITLE_CONFIG_PATH_KEY), ioHandler.getConfPath()) + "\n";
            desc += String.format("%s: '%s'", properties.getLocalizedProperty(TITLE_SAVE_PATH_KEY), dbAbsPath) + "\n";
            desc += GITHUB_ABOUT + "\n";
            desc += AUTHOR_ABOUT;
            gPane.add(selectableText(desc), 0, 0);

            aboutDialog.getDialogPane().setContent(gPane);
            DialogUtil.disableHeader(aboutDialog);
            aboutDialog.show();
        });
    }

    private void _onLanguageHandler(ActionEvent e) {
        final String engChoice = "English";
        final String chnChoice = "中文";
        Platform.runLater(() -> {
            final Language oldLang = environment.getLanguage();

            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(properties.getLocalizedProperty(TITLE_CHOOSE_LANGUAGE_KEY));
            choiceDialog.setSelectedItem(oldLang.equals(Language.ENG) ? engChoice : chnChoice);
            choiceDialog.getItems().add(engChoice);
            choiceDialog.getItems().add(chnChoice);
            DialogUtil.disableHeader(choiceDialog);

            Optional<String> opt = choiceDialog.showAndWait();
            if (opt.isPresent()) {
                final Language newLang = opt.get().equals(engChoice) ? Language.ENG : Language.CHN;
                // not changed, do nothing
                if (newLang.equals(oldLang))
                    return;

                environment.setLanguage(newLang);
                // reload resource bundle for the updated locale
                properties.changeToLocale(environment.getLanguage().locale);
                reloadCurrPageAsync();
                // override the previous menu
                _registerContextMenu();
                // override the previous pagination bar and search bar
                _setupPaginationBar(volatileCurrPage);
                _setupSearchBar();
                _setupQuickTodoBar();
                layoutComponents();
                updateConfigAsync(environment);
            }

        });
    }

    private void _onChangeExportPatternHandler(ActionEvent e) {
        Platform.runLater(() -> {
            final String pattern = environment.getPattern();
            TxtAreaDialog dialog = new TxtAreaDialog(pattern != null ? pattern : "");
            dialog.setTitle(properties.getLocalizedProperty(TITLE_EXPORT_PATTERN_KEY));
            dialog.setContentText(properties.getLocalizedProperty(TEXT_EXPORT_PATTERN_DESC_KEY));
            DialogUtil.disableHeader(dialog);

            Optional<String> opt = dialog.showAndWait();
            if (opt.isPresent()) {
                environment.setPattern(opt.get());
                updateConfigAsync(environment);
            }
        });
    }

    private void _searchOnTypingConfigHandler(ActionEvent e) {
        final String enable = properties.getLocalizedProperty(TEXT_ENABLE);
        final String disable = properties.getLocalizedProperty(TEXT_DISABLE);
        Platform.runLater(() -> {
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(properties.getLocalizedProperty(TITLE_CHOOSE_SEARCH_ON_TYPE_KEY));
            choiceDialog.setSelectedItem(environment.isSearchOnTypingEnabled() ? enable : disable);
            choiceDialog.getItems().add(enable);
            choiceDialog.getItems().add(disable);
            DialogUtil.disableHeader(choiceDialog);
            Optional<String> opt = choiceDialog.showAndWait();
            if (opt.isPresent()) {
                final boolean prevIsEnabled = environment.isSearchOnTypingEnabled();
                final boolean currIsEnabled = opt.get().equals(enable);
                if (currIsEnabled != prevIsEnabled) {
                    // changed
                    environment.setSearchOnTypingEnabled(currIsEnabled);
                    searchBar.setSearchOnTypeEnabled(currIsEnabled);
                    updateConfigAsync(environment);
                }
            }
        });
    }

    private void updateConfigAsync(Environment environment) {
        ioHandler.writeConfigAsync(new Config(environment));
    }

    private void _registerContextMenu() {
        Platform.runLater(() -> listView.setContextMenu(createCtxMenu()));
    }

    private void _setupPaginationBar(int currPage) {
        paginationBar = new PaginationBar(currPage);
        paginationBar.getPrevPageBtn().setOnAction(e -> {
            loadPrevPageAsync();
        });
        paginationBar.getNextPageBtn().setOnAction(e -> {
            loadNextPageAsync();
        });
    }

    private void _setupQuickTodoBar() {
        quickTodoBar = new QuickTodoBar();
        quickTodoBar.textFieldPrefWidthProperty().bind(listView.widthProperty().subtract(100));
        quickTodoBar.setOnEnter(name -> {
            final LocalDate now = LocalDate.now();
            TodoJob tj = new TodoJob();
            tj.setDone(false);
            tj.setExpectedEndDate(now);
            tj.setActualEndDate(null);
            tj.setName(name);
            doInsertTodo(tj);
        });

        if (outerPane.getTop() != null)
            outerPane.setTop(quickTodoBar);
    }

    private void doInsertTodo(TodoJob job) {
        _todoJobMapper()
                .insertAsync(job)
                .subscribeOn(taskScheduler)
                .subscribe(id -> {
                    if (id != null) {
                        reloadCurrPageAsync();
                    } else {
                        toastError("Unknown error happens when try to redo");
                    }
                });
    }

    private void _setupSearchBar() {
        searchBar = new SearchBar();
        searchBar.setSearchOnTypeEnabled(environment.isSearchOnTypingEnabled());
        searchBar.searchTextFieldPrefWidthProperty().bind(listView.widthProperty().subtract(100));
        searchBar.onSearchTextFieldEnterPressed(() -> {
            Platform.runLater(() -> {
                if (searchBar.isSearchTextChanged()) {
                    searchBar.setSearchTextChanged(false);
                    volatileCurrPage = 1;
                    paginationBar.setCurrPage(volatileCurrPage);
                    reloadCurrPageAsync();
                }
            });
        });
    }

    /**
     * Get {@link TodoJobMapper }
     * <p>
     * the process of initializing a new mapper is async, before returning the mapper, this method will wait until it's
     * fully initialized
     * </p>
     */
    private TodoJobMapper _todoJobMapper() {
        // before we use the mapper, we want to make sure that the mapper is initialised properly
        TodoJobMapper todoJobMapper;
        while ((todoJobMapper = this._todoJobMapper.get()) == null) {
            log.debug("Waiting for TodoJobMapper to initialize");
        }
        return todoJobMapper;
    }

    private void _subscribeTickingFluxForReloading() {
        // register a flux that ticks every 5 seconds
        Flux.interval(Duration.ofSeconds(5))
                .subscribeOn(taskScheduler)
                .subscribe((l) -> {
                    boolean isNextDate = false;

                    synchronized (lastTickDateLock) {
                        LocalDate now = LocalDate.now();
                        if (now.isAfter(lastTickDate)) {
                            lastTickDate = now;
                            isNextDate = true;
                        }
                    }

                    if (isNextDate) {
                        log.info("Next date, reload current page");
                        // we are now on next date, reload current page to refresh the timeLeftLabel in TodoJobView
                        reloadCurrPageAsync();
                    }
                });
    }
}



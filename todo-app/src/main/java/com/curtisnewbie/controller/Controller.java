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
import java.util.concurrent.atomic.AtomicReference;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.MarginFactory.padding;
import static com.curtisnewbie.util.TextFactory.selectableText;
import static javafx.application.Platform.*;

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

    /** Properties Loader, thread-safe */
    private final PropertiesLoader properties = PropertiesLoader.getInstance();
    /** GitHub link in About */
    private final String GITHUB_ABOUT = properties.getCommonProperty(APP_GITHUB);
    /** Author in About */
    private final String AUTHOR_ABOUT = properties.getCommonProperty(APP_AUTHOR);

    /** Path to DB */
    private final String dbAbsPath;
    /** AtomicReference for mapper */
    private final AtomicReference<TodoJobMapper> _todoJobMapper = new AtomicReference<>();

    /** IO Handler, thread-safe */
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    /** Redo stack, thread-safe */
    private final RedoStack redoStack = new RedoStack();
    /** ObjectPrinter for {@link TodoJob }, thread-safe */
    private final ObjectPrinter<TodoJob> todoJobExportObjectPrinter;

    /**
     * The last date used and updated by the {@link #_subscribeTickingFluxForReloading()},
     * must be synchronized using {@link #lastTickDateLock}
     */
    @LockedBy(field = "lastTickDateLock")
    private LocalDate lastTickDate = LocalDate.now();
    private final Object lastTickDateLock = new Object();

    /**
     * Scheduler for reactor
     */
    private final Scheduler taskScheduler = Schedulers.fromExecutor(ForkJoinPool.commonPool());

    @RequiresFxThread
    private final Environment environment = new Environment(ioHandler.readConfig());
    @RequiresFxThread
    private final TodoJobListView todoJobListView = new TodoJobListView();
    @RequiresFxThread
    private volatile int volatileCurrPage = 1;
    @RequiresFxThread
    private final SearchBar searchBar = new SearchBar();
    @RequiresFxThread
    private final QuickTodoBar quickTodoBar = new QuickTodoBar();
    @RequiresFxThread
    private final PaginationBar paginationBar = new PaginationBar(1);
    @RequiresFxThread
    private final BorderPane innerPane;
    @RequiresFxThread
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
        _prepareMapper(mapperFactory);

        // load locale-specific resource bundle
        properties.changeToLocale(environment.getLanguage().locale);

        // setup to-do job printer
        this.todoJobExportObjectPrinter = new TodoJobObjectPrinter(properties, environment);

        // setup components for the first time
        _setupTodoJobListView();
        _setupSearchBar();
        _setupQuickTodoBar();
        _setupPaginationBar();

        // reload page when we are on next day
        _subscribeTickingFluxForReloading();

        // load the first page
        reloadCurrPageAsync();
    }

    /** Initialize a Controller that is bound to the given BorderPane */
    public static Controller initialize(BorderPane parent) {
        return new Controller(parent);
    }

    /**
     * Refresh view
     */
    private synchronized void refreshView() {
        searchBar.refresh();
        quickTodoBar.refresh();
        todoJobListView.setContextMenu(createCtxMenu());
    }

    /**
     * Load next page asynchronously
     */
    private void loadNextPageAsync() {
        _todoJobMapper().findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage + 1)
                .subscribeOn(taskScheduler)
                .subscribe(list -> {
                    if (!list.isEmpty()) {
                        runLater(() -> {
                            volatileCurrPage += 1;
                            paginationBar.setCurrPage(volatileCurrPage);
                            todoJobListView.clearAndLoadList(list, environment);
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
                        runLater(() -> {
                            volatileCurrPage -= 1;
                            paginationBar.setCurrPage(volatileCurrPage);
                            todoJobListView.clearAndLoadList(list, environment);
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
                    if (!list.isEmpty())
                        runLater(() -> todoJobListView.clearAndLoadList(list, environment));
                });
    }


    private CnvCtxMenu createCtxMenu() {
        CnvCtxMenu ctxMenu = new CnvCtxMenu();
        ctxMenu.addMenuItem(properties.getLocalizedProperty(TITLE_ADD_KEY), this::_onAddHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_DELETE_KEY), e -> deleteSelected())
                .addMenuItem(properties.getLocalizedProperty(TITLE_UPDATE_KEY), this::_onUpdateHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_COPY_KEY), e -> copySelected())
                .addMenuItem(properties.getLocalizedProperty(TITLE_EXPORT_KEY), this::_onExportHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_CHOOSE_LANGUAGE_KEY), this::_onLanguageHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_CHOOSE_SEARCH_ON_TYPE_KEY), this::_searchOnTypingConfigHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_EXPORT_PATTERN_KEY), this::_onChangeExportPatternHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_SWITCH_QUICK_TODO_KEY), this::_onSwitchQuickTodoHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_ABOUT_KEY), this::_onAboutHandler);
        return ctxMenu;
    }

    @RunInFxThread
    private void _onSwitchQuickTodoHandler(ActionEvent e) {
        runLater(() -> {
            if (this.outerPane.getTop() != null)
                this.outerPane.setTop(null);
            else
                this.outerPane.setTop(padding(quickTodoBar, 3, 0, 3, 0));
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

    /**
     * Toast a message
     */
    @RunInFxThread
    private void toast(String msg) {
        runLater(() -> ToastUtil.toast(msg));
    }

    /**
     * Copy content to clipboard (This method is always ran within a Javafx' UI Thread)
     *
     * @param content text
     */
    @RunInFxThread
    private void copyToClipBoard(String content) {
        runLater(() -> {
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

    @RunInFxThread
    private void _onAddHandler(ActionEvent e) {
        runLater(() -> {
            TodoJobDialog dialog = new TodoJobDialog(TodoJobDialog.DialogType.ADD_TODO_JOB, null);
            dialog.setTitle(properties.getLocalizedProperty(TITLE_ADD_NEW_TODO_KEY));
            Optional<TodoJob> result = dialog.showAndWait();
            if (!result.isPresent() || StrUtil.isEmpty(result.get().getName()))
                return;

            TodoJob newTodo = result.get();
            _todoJobMapper().insertAsync(newTodo)
                    .subscribeOn(taskScheduler)
                    .subscribe(id -> {
                        reloadCurrPageAsync();
                    }, (err) -> {
                        toast("Failed to add new to-do, please try again");
                        log.error("Failed to add new to-do", err);
                    });
        });
    }

    @RunInFxThread
    private void _onUpdateHandler(ActionEvent e) {
        runLater(() -> {
            final int selected = todoJobListView.getSelectedIndex();
            if (selected < 0)
                return;

            final TodoJobView jobView = todoJobListView.get(selected);
            final TodoJob old = jobView.createTodoJobCopy();
            final TodoJobDialog dialog = new TodoJobDialog(TodoJobDialog.DialogType.UPDATE_TODO_JOB,
                    jobView.createTodoJobCopy());
            dialog.setTitle(properties.getLocalizedProperty(TITLE_UPDATE_TODO_NAME_KEY));
            Optional<TodoJob> result = dialog.showAndWait();
            if (!result.isPresent())
                return;

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
                            toast("Failed to update to-do, please try again");
                    });
        });
    }

    @RunInFxThread
    private void deleteSelected() {
        runLater(() -> {
            int selected = todoJobListView.getSelectedIndex();
            if (selected >= 0) {
                final TodoJobView tjv = todoJobListView.get(selected);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setResizable(true);
                alert.setTitle(properties.getLocalizedProperty(TITLE_DELETE_KEY));
                alert.setContentText(properties.getLocalizedProperty(TEXT_DELETE_CONFIRM_KEY) + "\n\n" + tjv.getName() + "\n");
                DialogUtil.disableHeader(alert);
                alert.showAndWait()
                        .filter(resp -> resp == ButtonType.OK)
                        .ifPresent(resp -> {
                            _todoJobMapper().deleteByIdAsync(tjv.getTodoJobId())
                                    .subscribe(isDeleted -> {
                                        if (isDeleted) {
                                            TodoJob jobCopy = todoJobListView.remove(selected).createTodoJobCopy();
                                            synchronized (redoStack) {
                                                redoStack.push(new Redo(RedoType.DELETE, jobCopy));
                                            }
                                        } else {
                                            toast("Failed to delete to-do, please try again");
                                        }
                                    });
                        });
            }
            reloadCurrPageAsync();
        });
    }

    @RunInFxThread
    private void copySelected() {
        runLater(() -> {
            int selected = todoJobListView.getSelectedIndex();
            if (selected >= 0) {
                TodoJobView todoJobView = todoJobListView.get(selected);
                copyToClipBoard(todoJobView.getName());
            }
        });
    }

    private void _onExportHandler(ActionEvent e) {
        Mono.zip(_todoJobMapper().findEarliestDateAsync(), _todoJobMapper().findLatestDateAsync())
                .subscribeOn(taskScheduler)
                .subscribe((tuple) -> {
                    runLater(() -> {
                        if (todoJobListView.isEmpty())
                            return;

                        // 1. pick date range and searched text
                        final LocalDate now = LocalDate.now();
                        final ExportDialog dialog = new ExportDialog(now, now, searchBar.getSearchTextField().getText());
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

    @RunInFxThread
    private void _onAboutHandler(ActionEvent e) {
        runLater(() -> {
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

    @RunInFxThread
    private void _onLanguageHandler(ActionEvent e) {
        final String engChoice = "English";
        final String chnChoice = "中文";
        runLater(() -> {
            final Language oldLang = environment.getLanguage();

            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(properties.getLocalizedProperty(TITLE_CHOOSE_LANGUAGE_KEY));
            choiceDialog.setSelectedItem(oldLang.equals(Language.ENG) ? engChoice : chnChoice);
            choiceDialog.getItems().add(engChoice);
            choiceDialog.getItems().add(chnChoice);
            DialogUtil.disableHeader(choiceDialog);

            final Optional<String> opt = choiceDialog.showAndWait();
            if (!opt.isPresent())
                return;

            final Language newLang = opt.get().equals(engChoice) ? Language.ENG : Language.CHN;
            if (newLang.equals(oldLang))
                return;

            environment.setLanguage(newLang);
            writeConfigAsync(environment);

            properties.changeToLocale(environment.getLanguage().locale);
            reloadCurrPageAsync();
            refreshView();
        });
    }

    @RunInFxThread
    private void _onChangeExportPatternHandler(ActionEvent e) {
        runLater(() -> {
            final String pattern = environment.getPattern();
            TxtAreaDialog dialog = new TxtAreaDialog(pattern != null ? pattern : "");
            dialog.setTitle(properties.getLocalizedProperty(TITLE_EXPORT_PATTERN_KEY));
            dialog.setContentText(properties.getLocalizedProperty(TEXT_EXPORT_PATTERN_DESC_KEY));
            DialogUtil.disableHeader(dialog);

            Optional<String> opt = dialog.showAndWait();
            if (opt.isPresent()) {
                environment.setPattern(opt.get());
                writeConfigAsync(environment);
            }
        });
    }

    @RunInFxThread
    private void _searchOnTypingConfigHandler(ActionEvent e) {
        final String enable = properties.getLocalizedProperty(TEXT_ENABLE);
        final String disable = properties.getLocalizedProperty(TEXT_DISABLE);

        runLater(() -> {
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
                    writeConfigAsync(environment);
                }
            }
        });
    }

    private void writeConfigAsync(Environment environment) {
        ioHandler.writeConfigAsync(new Config(environment));
    }

    @RequiresFxThread
    private void _setupPaginationBar() {
        innerPane.setBottom(paginationBar);
        paginationBar.getPrevPageBtn().setOnAction(e -> {
            loadPrevPageAsync();
        });
        paginationBar.getNextPageBtn().setOnAction(e -> {
            loadNextPageAsync();
        });
    }

    @RequiresFxThread
    private void _setupQuickTodoBar() {
        quickTodoBar.textFieldPrefWidthProperty().bind(todoJobListView.widthProperty().subtract(15));
        quickTodoBar.setOnEnter(name -> {
            final LocalDate now = LocalDate.now();
            TodoJob tj = new TodoJob();
            tj.setDone(false);
            tj.setExpectedEndDate(now);
            tj.setActualEndDate(null);
            tj.setName(name);
            doInsertTodo(tj);
        });
    }

    /**
     * Insert to-do job and reload current page async
     */
    private void doInsertTodo(TodoJob job) {
        _todoJobMapper()
                .insertAsync(job)
                .subscribeOn(taskScheduler)
                .subscribe(id -> {
                    if (id != null) {
                        reloadCurrPageAsync();
                    } else {
                        toast("Unknown error happens when try to redo");
                    }
                });
    }

    @RequiresFxThread
    private void _setupSearchBar() {
        innerPane.setTop(searchBar);
        searchBar.setSearchOnTypeEnabled(environment.isSearchOnTypingEnabled());
        searchBar.searchTextFieldPrefWidthProperty().bind(todoJobListView.widthProperty().subtract(15));
        searchBar.onSearchTextFieldEnterPressed(() -> {
            runLater(() -> {
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

    private void _prepareMapper(MapperFactory mapperFactory) {
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
    }

    private void _setupTodoJobListView() {
        this.innerPane.setCenter(this.todoJobListView);
        this.todoJobListView.setContextMenu(createCtxMenu());

        todoJobListView.onKeyPressed(e -> {
            if (e.isControlDown()) {
                if (e.getCode().equals(KeyCode.Z))
                    redo();
                else if (e.getCode().equals(KeyCode.C))
                    copySelected();
                else if (e.getCode().equals(KeyCode.F))
                    runLater(() -> searchBar.getSearchTextField().requestFocus());
            } else {
                if (e.getCode().equals(KeyCode.DELETE))
                    deleteSelected();
                else if (e.getCode().equals(KeyCode.F5))
                    reloadCurrPageAsync();

            }
        });
        // do on each to-do changes
        todoJobListView.onModelChanged(evt -> {
            _todoJobMapper().updateByIdAsync((TodoJob) evt.getNewValue())
                    .subscribeOn(taskScheduler)
                    .subscribe(isUpdated -> {
                        if (isUpdated)
                            reloadCurrPageAsync();
                        else
                            toast("Failed to update to-do, please try again");
                    });
        });
    }

}



package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.*;
import com.curtisnewbie.dao.MapperFactory;
import com.curtisnewbie.dao.MapperFactoryBase;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.dao.TodoJobMapper;
import com.curtisnewbie.io.*;
import com.curtisnewbie.util.*;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.FileExtUtil.*;
import static com.curtisnewbie.util.FxThreadUtil.*;
import static com.curtisnewbie.util.MarginFactory.padding;
import static com.curtisnewbie.util.TextFactory.selectableText;
import static com.curtisnewbie.util.ToastUtil.toast;
import static java.lang.String.*;
import static java.util.concurrent.CompletableFuture.runAsync;
import static javafx.application.Platform.runLater;

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

    private static final long SUGGESTION_TOAST_DURATION = TimeUnit.SECONDS.toMillis(10);

    /** Properties Loader, thread-safe */
    private final PropertiesLoader properties = PropertiesLoader.getInstance();
    /** Path to DB */
    private final String dbAbsPath;
    /** AtomicReference for mapper */
    private final AtomicReference<TodoJobMapper> _todoJobMapper = new AtomicReference<>();
    /** IO Handler, thread-safe */
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    /** Atomic Reference to the Environment */
    private final AtomicReference<Environment> _environment = new AtomicReference<>();

    /**
     * The last date used and updated by the {@link #_subscribeTickingFluxForReloading()}, must be synchronized using
     * {@link #lastTickDateLock}
     */
    @LockedBy(name = "lastTickDateLock")
    private LocalDate lastTickDate = LocalDate.now();
    private final Object lastTickDateLock = new Object();

    /**
     * Scheduler for reactor
     */
    private final Scheduler taskScheduler = Schedulers.fromExecutor(ForkJoinPool.commonPool());

    @RequiresFxThread
    private final TodoJobListView todoJobListView;
    @RequiresFxThread
    private volatile int volatileCurrPage = 1;
    @RequiresFxThread
    private final SearchBar searchBar;
    @RequiresFxThread
    private final QuickTodoBar quickTodoBar;
    @RequiresFxThread
    private final PaginationBar paginationBar;
    @RequiresFxThread
    private final BorderPane innerPane = new BorderPane();
    @RequiresFxThread
    private final BorderPane outerPane;

    /** ObjectPrinter for {@link TodoJob }, thread-safe */
    private final ObjectPrinter<TodoJob> todoJobExportObjectPrinter;

    /**
     * Create and bind the new Controller to a BorderPane
     */
    public Controller(BorderPane parent) {
        this.outerPane = parent;
        this.outerPane.setCenter(this.innerPane);

        final MapperFactory mapperFactory = new MapperFactoryBase();
        dbAbsPath = mapperFactory.getDatabaseAbsolutePath();
        _prepareMapperAsync(mapperFactory);

        // load locale-specific resource bundle
        setEnvironment(new Environment(ioHandler.readConfig()));
        properties.changeToLocale(getEnvironment().getLanguage().locale);

        // instantiate view components after we changed the locale
        todoJobListView = new TodoJobListView();
        searchBar = new SearchBar();
        quickTodoBar = new QuickTodoBar();
        paginationBar = new PaginationBar(volatileCurrPage);
        todoJobExportObjectPrinter = new TodoJobObjectPrinter();

        // setup components for the first time
        _setupTodoJobListView();
        _setupSearchBar();
        _setupQuickTodoBar();
        _setupPaginationBar();

        // reload page when we are on next day
        _subscribeTickingFluxForReloading();
    }

    /** Initialize a Controller that is bound to the given BorderPane */
    public static void initialize(BorderPane parent) {
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        final Controller controller = new Controller(parent);
        timer.stop();
        log.info("Controller initialized, took: {} ms", timer.getMilliSec());

        // load the first page
        controller.loadCurrPageAsync();
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
                .thenAcceptAsync(list -> {
                    runLater(() -> {
                        if (list.isEmpty())
                            return;
                        volatileCurrPage += 1;
                        paginationBar.setCurrPage(volatileCurrPage);
                        todoJobListView.clearAndLoadList(list, getEnvironment());
                    });
                });
    }

    /**
     * Load previous page asynchronously
     */
    private void loadPrevPageAsync() {
        if (volatileCurrPage <= 1)
            return;

        _todoJobMapper()
                .findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage - 1)
                .thenAcceptAsync(list -> {
                    runLater(() -> {
                        volatileCurrPage -= 1;
                        paginationBar.setCurrPage(volatileCurrPage);
                        todoJobListView.clearAndLoadList(list, getEnvironment());
                    });
                });
    }

    /**
     * Reload current page asynchronously
     */
    private void loadCurrPageAsync() {
        _todoJobMapper().findByPageAsync(searchBar.getSearchTextField().getText(), volatileCurrPage)
                .thenAcceptAsync(list -> {
                    runLater(() -> todoJobListView.clearAndLoadList(list, getEnvironment()));
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
                .addMenuItem(properties.getLocalizedProperty(TITLE_CHANGE_COPY_MODE_KEY), this::_onChangeCopyModeHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_CHOOSE_SEARCH_ON_TYPE_KEY), this::_searchOnTypingConfigHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_EXPORT_PATTERN_KEY), this::_onChangeExportPatternHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_SWITCH_SPECIAL_TAG_ENABLE_KEY), this::_onToggleSecialTagEnabledConfigHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_SWITCH_QUICK_TODO_KEY), this::_onToggleQuickTodoHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_SWITCH_SPECIAL_TAG_HIDDEN_KEY), this::_onToggleSpecialTagHiddenHandler)
                .addMenuItem(properties.getLocalizedProperty(TITLE_ABOUT_KEY), this::_onAboutHandler);
        return ctxMenu;
    }

    @RunInFxThread
    private void _onToggleQuickTodoHandler(ActionEvent e) {
        runLater(() -> {
            final Environment env = getEnvironment();
            final boolean wasDisplayed = env.isQuickTodoBarDisplayed();

            if (wasDisplayed)
                this.outerPane.setTop(null);
            else
                this.outerPane.setTop(padding(quickTodoBar, 3, 0, 3, 0));

            setEnvironment(env.setQuickTodoBarDisplayed(!wasDisplayed));
            writeConfigAsync();
        });
    }

    @RunInFxThread
    private void _onToggleSpecialTagHiddenHandler(ActionEvent e) {
        runLater(() -> {
            final Environment env = getEnvironment();
            setEnvironment(env.setSpecialTagHidden(!env.isSpecialTagHidden()));
            writeConfigAsync();
            loadCurrPageAsync();
        });
    }

    private void _onCopyHandler(ActionEvent e) {
        copySelected();
        runAsync(() -> {
            final Environment env = getEnvironment();
            final SuggestionType type = SuggestionType.COPY_HANDLER;
            if (env.isSuggestionToggleOn(type)) {
                toast(properties.getLocalizedProperty(TEXT_SUGGESTION_COPY_HANDLER), SUGGESTION_TOAST_DURATION);
                setEnvironment(env.toggleSuggestionOff(type));
                writeConfigAsync();
            }
        });
    }

    /**
     * Copy content to clipboard (This method is always ran within a Javafx' UI Thread)
     *
     * @param content text
     */
    @RunInFxThread
    private void copyToClipBoard(String content, Runnable doOnFinished) {
        runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent cc = new ClipboardContent();
            cc.putString(content);
            clipboard.setContent(cc);

            if (doOnFinished != null)
                doOnFinished.run();
        });
    }

    @RunInFxThread
    private void _onAddHandler(ActionEvent e) {
        runLater(() -> {
            TodoJobDialog dialog = new TodoJobDialog(TodoJobDialog.DialogType.ADD_TODO_JOB, null);
            dialog.setTitle(properties.getLocalizedProperty(TITLE_ADD_NEW_TODO_KEY));
            Optional<TodoJob> result = dialog.showAndWait();
            if (!result.isPresent() || StrUtil.isEmpty(result.get().getName())) {
                final Environment env = getEnvironment();
                final SuggestionType type = SuggestionType.NEW_TODO_HANDLER;
                if (env.isSuggestionToggleOn(type)) {
                    toast(properties.getLocalizedProperty(TEXT_SUGGESTION_NEW_TODO_HANDLER), SUGGESTION_TOAST_DURATION);
                    setEnvironment(env.toggleSuggestionOff(type));
                    writeConfigAsync();
                }
                return;
            }

            TodoJob newTodo = result.get();
            _todoJobMapper().insertAsync(newTodo)
                    .thenAcceptAsync(id -> loadCurrPageAsync())
                    .exceptionally(err -> {
                        toast("Failed to add new to-do, please try again\n\n" + err.getMessage());
                        return null;
                    })
                    .thenRunAsync(() -> {
                        final Environment env = getEnvironment();
                        final SuggestionType type = SuggestionType.NEW_TODO_HANDLER;
                        if (env.isSuggestionToggleOn(type)) {
                            toast(properties.getLocalizedProperty(TEXT_SUGGESTION_NEW_TODO_HANDLER), SUGGESTION_TOAST_DURATION);
                            setEnvironment(env.toggleSuggestionOff(type));
                            writeConfigAsync();
                        }
                    });
        });

    }

    @RunInFxThread
    private void _onDeleteHandler(ActionEvent e) {
        deleteSelected(() -> {
            runAsync(() -> {
                final Environment env = getEnvironment();
                final SuggestionType type = SuggestionType.DELETE_TODO_HANDLER;
                if (env.isSuggestionToggleOn(type)) {
                    toast(properties.getLocalizedProperty(TEXT_SUGGESTION_DELETE_TODO_HANDLER), SUGGESTION_TOAST_DURATION);
                    setEnvironment(env.toggleSuggestionOff(type));
                    writeConfigAsync();
                }
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
            _todoJobMapper()
                    .updateByIdAsync(updated)
                    .thenAcceptAsync(isUpdated -> {
                        if (isUpdated)
                            loadCurrPageAsync();
                        else
                            toast("Failed to update to-do, please try again");
                    });
        });
    }

    @RunInFxThread
    private void deleteSelected(Runnable afterDialog) {
        runLater(() -> {
            int selected = todoJobListView.getSelectedIndex();
            if (selected < 0)
                return;

            final TodoJobView tjv = todoJobListView.get(selected);
            final TodoJob copy = tjv.createTodoJobCopy();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setResizable(true);
            alert.setTitle(properties.getLocalizedProperty(TITLE_DELETE_KEY));
            alert.setContentText(properties.getLocalizedProperty(TEXT_DELETE_CONFIRM_KEY) + "\n\n" + tjv.getName() + "\n");
            DialogUtil.disableHeader(alert);
            alert.showAndWait()
                    .filter(resp -> resp == ButtonType.OK)
                    .ifPresent(resp -> doDeleteAndReloadAsync(tjv.getTodoJobId(), copy));

            if (afterDialog != null)
                afterDialog.run();
        });
    }

    private void doDeleteAndReloadAsync(int id, TodoJob copy) {
        _todoJobMapper()
                .deleteByIdAsync(id)
                .thenAcceptAsync(isDeleted -> {
                    if (!isDeleted)
                        toast("Failed to delete to-do, please try again");
                })
                .thenRun(this::loadCurrPageAsync);
    }

    @RunInFxThread
    private void copySelected() {
        runLater(() -> {
            int selected = todoJobListView.getSelectedIndex();
            if (selected >= 0) {
                final TodoJobView todoJobView = todoJobListView.get(selected);
                final Environment env = getEnvironment();
                final String copied;
                if (env.isCopyNameOnly()) {
                    copied = env.isSpecialTagEnabled() ? Tag.EXCL.strip(todoJobView.getName()) : todoJobView.getName();
                } else {
                    final TodoJob todoJobCopy = todoJobView.createTodoJobCopy();
                    copied = todoJobExportObjectPrinter.printObject(todoJobCopy, env.getPattern(), PrintContext.builder()
                            .environment(env)
                            .build());
                }
                copyToClipBoard(copied, null);
            }
        });
    }

    private void _onExportHandler(ActionEvent e) {
        final CompletableFuture<LocalDate> c1 = _todoJobMapper().findEarliestDateAsync();
        final CompletableFuture<LocalDate> c2 = _todoJobMapper().findLatestDateAsync();
        CompletableFuture.allOf(c1, c2)
                .thenApplyAsync(ignored -> {
                    try {
                        return new Pair(c1.get(), c2.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new IllegalStateException(ex);
                    }
                })
                .thenAcceptAsync(pair -> {
                    runLater(() -> {
                        if (todoJobListView.isEmpty())
                            return;

                        // 1. pick date range and searched text
                        final LocalDate now = LocalDate.now();
                        final ExportDialog dialog = new ExportDialog(now, now, searchBar.getSearchTextField().getText(),
                                (LocalDate) pair.getLeft(), (LocalDate) pair.getRight());
                        final Optional<ExportDialog.ExportParam> opt = dialog.showAndWait();
                        if (!opt.isPresent())
                            return;

                        final ExportDialog.ExportParam ep = opt.get();
                        final boolean isToFile = ep.isExportToFile();

                        final File nFile;
                        if (isToFile) {
                            // 2. choose where to export
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle(properties.getLocalizedProperty(TITLE_EXPORT_TODO_KEY));
                            fileChooser.setInitialFileName("TodoApp_" + DateUtil.toLongDateStrDash(new Date()).replace(":", "") + ".txt");
                            fileChooser.getExtensionFilters().addAll(txtExtFilter(), markdownExtFilter());
                            nFile = fileChooser.showSaveDialog(App.getPrimaryStage());
                            if (nFile == null)
                                return;
                        } else {
                            nFile = null;
                        }

                        final Environment environment = getEnvironment();
                        final String exportPattern = environment.getPattern(); // nullable
                        final DateRange dateRange = ep.getDateRange();
                        final PrintContext printContext = PrintContext.builder()
                                .environment(environment)
                                .isNumbered(ep.isNumbered())
                                .build();

                        _todoJobMapper()
                                .findBetweenDatesAsync(ep.getSearchText(), dateRange.getStart(), dateRange.getEnd())
                                .thenAcceptAsync((list) -> {
                                    final StringBuilder sb = new StringBuilder();
                                    final int len = list.size();
                                    for (int i = 0; i < len; i++) {
                                        if (i > 0) sb.append("\n");
                                        sb.append(todoJobExportObjectPrinter.printObject(list.get(i), exportPattern, printContext));
                                    }
                                    final String content = sb.toString();
                                    if (isToFile)
                                        ioHandler.writeObjectsAsync(sb.toString(), nFile);
                                    else
                                        copyToClipBoard(content, () -> {
                                            toast(format("Copied %s Todos to clipboard", len), 1_500);
                                        });
                                });
                    });
                })
                .exceptionally(ex -> {
                    toast("Error occurred, please try again\n\n" + ex.getMessage());
                    return null;
                });
    }

    @RunInFxThread
    private void _onAboutHandler(ActionEvent e) {
        runLater(() -> {
            Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
            GridPane gPane = new GridPane();
            aboutDialog.setTitle(properties.getLocalizedProperty(TITLE_ABOUT_KEY));

            String desc = format("%s: %s", properties.getLocalizedProperty(TITLE_CONFIG_PATH_KEY), ioHandler.getConfPath()) + "\n";
            desc += format("%s: %s", properties.getLocalizedProperty(TITLE_SAVE_PATH_KEY), dbAbsPath) + "\n";
            desc += properties.getCommonProperty(APP_GITHUB) + "\n";
            desc += properties.getCommonProperty(APP_AUTHOR) + "\n\n";
            desc += properties.getLocalizedProperty(TEXT_ABOUT_TIPS);

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
            final Environment prevEnv = getEnvironment();
            final Language oldLang = prevEnv.getLanguage();

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

            final Environment newEnv = prevEnv.setLanguage(newLang);
            setEnvironment(newEnv);
            writeConfigAsync();

            properties.changeToLocale(newEnv.getLanguage().locale);
            loadCurrPageAsync();
            refreshView();
        });
    }

    @RunInFxThread
    private void _onChangeCopyModeHandler(ActionEvent e) {
        final String nameOnly = properties.getLocalizedProperty(TEXT_COPY_MODE_NAME_ONLY);
        final String exportFormat = properties.getLocalizedProperty(TEXT_COPY_MODE_EXPORT_FORMAT);

        runLater(() -> {
            final Environment env = getEnvironment();
            final String prev = env.isCopyNameOnly() ? nameOnly : exportFormat;

            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(properties.getLocalizedProperty(TITLE_CHANGE_COPY_MODE_KEY));
            choiceDialog.setSelectedItem(prev);
            choiceDialog.getItems().add(nameOnly);
            choiceDialog.getItems().add(exportFormat);
            DialogUtil.disableHeader(choiceDialog);

            final Optional<String> opt = choiceDialog.showAndWait();
            if (!opt.isPresent())
                return;

            final String selected = opt.get();
            if (prev.equals(selected))
                return;

            setEnvironment(env.setCopyNameOnly(selected.equals(nameOnly)));
            writeConfigAsync();
        });
    }

    @RunInFxThread
    private void _onChangeExportPatternHandler(ActionEvent e) {
        runLater(() -> {
            final Environment prevEnv = getEnvironment();
            final String pattern = prevEnv.getPattern();
            final TxtAreaDialog dialog = new TxtAreaDialog(pattern != null ? pattern : "");
            dialog.setTitle(properties.getLocalizedProperty(TITLE_EXPORT_PATTERN_KEY));
            dialog.setContentText(properties.getLocalizedProperty(TEXT_EXPORT_PATTERN_DESC_KEY));
            DialogUtil.disableHeader(dialog);

            final Optional<String> opt = dialog.showAndWait();
            if (opt.isPresent()) {
                setEnvironment(prevEnv.setPattern(opt.get()));
                writeConfigAsync();
            }
        });
    }

    @RunInFxThread
    private void _searchOnTypingConfigHandler(ActionEvent e) {
        final String enable = properties.getLocalizedProperty(TEXT_ENABLE);
        final String disable = properties.getLocalizedProperty(TEXT_DISABLE);

        runLater(() -> {
            final Environment prevEnv = getEnvironment();
            final ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(properties.getLocalizedProperty(TITLE_CHOOSE_SEARCH_ON_TYPE_KEY));
            choiceDialog.setSelectedItem(prevEnv.isSearchOnTypingEnabled() ? enable : disable);
            choiceDialog.getItems().add(enable);
            choiceDialog.getItems().add(disable);
            DialogUtil.disableHeader(choiceDialog);
            final Optional<String> opt = choiceDialog.showAndWait();
            if (!opt.isPresent())
                return;

            final boolean prevIsEnabled = prevEnv.isSearchOnTypingEnabled();
            final boolean currIsEnabled = opt.get().equals(enable);
            if (currIsEnabled == prevIsEnabled)
                return;

            setEnvironment(prevEnv.setSearchOnTypingEnabled(currIsEnabled));
            searchBar.setSearchOnTypeEnabled(currIsEnabled);
            writeConfigAsync();
        });
    }

    @RunInFxThread
    private void _onToggleSecialTagEnabledConfigHandler(ActionEvent e) {
        final String enable = properties.getLocalizedProperty(TEXT_ENABLE);
        final String disable = properties.getLocalizedProperty(TEXT_DISABLE);

        runLater(() -> {
            final Environment prevEnv = getEnvironment();
            final boolean isPrevEnabled = prevEnv.isSpecialTagEnabled();
            final ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
            choiceDialog.setTitle(properties.getLocalizedProperty(TITLE_SWITCH_SPECIAL_TAG_ENABLE_KEY));
            choiceDialog.setSelectedItem(isPrevEnabled ? enable : disable);
            choiceDialog.getItems().add(enable);
            choiceDialog.getItems().add(disable);
            DialogUtil.disableHeader(choiceDialog);
            final Optional<String> opt = choiceDialog.showAndWait();
            if (!opt.isPresent())
                return;

            final boolean isCurrEnabled = opt.get().equals(enable);
            if (isCurrEnabled == isPrevEnabled)
                return;

            setEnvironment(prevEnv.setSpecialTagEnabled(isCurrEnabled));
            writeConfigAsync();
        });
    }

    /** Write Environment to file as async */
    private void writeConfigAsync() {
        ioHandler.writeConfigAsync(new Config(getEnvironment()));
    }

    @RequiresFxThread
    private void _setupPaginationBar() {
        checkThreadConfinement();

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
        checkThreadConfinement();

        if (getEnvironment().isQuickTodoBarDisplayed())
            this.outerPane.setTop(padding(quickTodoBar, 3, 0, 3, 0));

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
                .thenAcceptAsync(id -> {
                    if (id != null) {
                        loadCurrPageAsync();
                    } else {
                        toast("Unknown error happens when try to redo");
                    }
                });
    }

    @RequiresFxThread
    private void _setupSearchBar() {
        checkThreadConfinement();

        innerPane.setTop(searchBar);
        searchBar.setSearchOnTypeEnabled(getEnvironment().isSearchOnTypingEnabled());
        searchBar.searchTextFieldPrefWidthProperty().bind(todoJobListView.widthProperty().subtract(15));
        searchBar.onSearchTextFieldEnterPressed(() -> {
            runLater(() -> {
                if (searchBar.isSearchTextChanged()) {
                    searchBar.setSearchTextChanged(false);
                    volatileCurrPage = 1;
                    paginationBar.setCurrPage(volatileCurrPage);
                    loadCurrPageAsync();
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
                        loadCurrPageAsync();
                    }
                });
    }

    private void _prepareMapperAsync(MapperFactory mapperFactory) {
        /**
         * speed up initialisation process, we make it async, and we rely on the {@link #_todoJobMapper() } method to
         * delay the timing that we retrieve the mapper
         */
        mapperFactory.getNewTodoJobMapperAsync()
                .thenAcceptAsync((mapper) -> {
                    if (this._todoJobMapper.compareAndSet(null, mapper)) {
                        log.info("TodoJobMapper loaded");
                    }
                });
    }

    private void _setupTodoJobListView() {
        this.innerPane.setCenter(this.todoJobListView);
        this.todoJobListView.setContextMenu(createCtxMenu());

        todoJobListView.onKeyPressed(e -> {
            if (e.isControlDown() || e.isMetaDown()) { // metaDown is for mac
                final KeyCode code = e.getCode();
                if (code.equals(KeyCode.N))
                    _onAddHandler(null);
                else if (code.equals(KeyCode.C))
                    copySelected();
                else if (code.equals(KeyCode.F))
                    runLater(() -> searchBar.getSearchTextField().requestFocus());
                else if (code.equals(KeyCode.R))
                    loadCurrPageAsync();
                else if (code.equals(KeyCode.E))
                    _onUpdateHandler(null);
            } else {
                if (e.getCode().equals(KeyCode.DELETE) || e.getCode().equals(KeyCode.BACK_SPACE))
                    deleteSelected(null);
                else if (e.getCode().equals(KeyCode.F5))
                    loadCurrPageAsync();
            }
        });
        // do on each to-do changes
        todoJobListView.onModelChanged(evt -> {
            _todoJobMapper()
                    .updateByIdAsync((TodoJob) evt.getNewValue())
                    .thenAcceptAsync(isUpdated -> {
                        if (isUpdated)
                            loadCurrPageAsync();
                        else
                            toast("Failed to update to-do, please try again");
                    });
        });
    }

    private Environment getEnvironment() {
        return _environment.get();
    }

    private void setEnvironment(Environment environment) {
        this._environment.set(environment);
    }

}



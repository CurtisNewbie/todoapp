package com.curtisnewbie.controller;

import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.util.*;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

import static com.curtisnewbie.util.FxThreadUtil.*;
import static com.curtisnewbie.util.LabelFactory.*;
import static com.curtisnewbie.util.MarginFactory.padding;

/**
 * Dialog for exporting
 *
 * @author yongjie.zhuang
 */
@RequiresFxThread
public class ExportDialog extends Dialog<ExportDialog.ExportParam> {

    private final String copyMode;
    private final String fileMode;

    private final GridPane grid;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final CheckBox isNumberedCheckBox;
    private final ChoiceBox<String> exportModeChoiceBox;
    private final TextField searchedTextField;
    private int rowCount = 0;

    /**
     * Create TodoJobDialog with {@code start} and {@code end} as the default value of the DatePicker(s)
     */
    public ExportDialog(LocalDate start, LocalDate end, String searchText, LocalDate earliest, LocalDate latest) {
        checkThreadConfinement();
        final PropertiesLoader propertiesLoader = PropertiesLoader.getInstance();
        final DialogPane dialogPane = getDialogPane();
        this.startDatePicker = new DatePicker();
        this.startDatePicker.setConverter(new LocalDateStringConverter());
        this.endDatePicker = new DatePicker();
        this.endDatePicker.setConverter(new LocalDateStringConverter());
        this.searchedTextField = new TextField(searchText);
        this.isNumberedCheckBox = CheckBoxFactory.getClassicCheckBox();

        copyMode = propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_EXPORT_MODE_COPY);
        fileMode = propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_EXPORT_MODE_FILE);
        this.exportModeChoiceBox = new ChoiceBox<>();
        this.exportModeChoiceBox.getItems().addAll(fileMode, copyMode);
        this.exportModeChoiceBox.getSelectionModel().selectFirst();

        this.startDatePicker.setValue(start);
        this.endDatePicker.setValue(end);

        final BorderPane bp = new BorderPane();
        this.grid = new GridPane();
        bp.setTop(padding(searchedTextField, new Insets(1, 0, 5, 0)));
        bp.setCenter(grid);

        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        final Insets pad = new Insets(1, 2, 5, 0);
        this.grid.add(rightPaddedLabel(propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_SEARCH)), 0, rowCount);
        this.grid.add(padding(searchedTextField, pad), 1, rowCount++);

        this.grid.add(rightPaddedLabel(propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_EXPORT_NUMBERED)), 0, rowCount);
        this.grid.add(padding(isNumberedCheckBox, pad), 1, rowCount++);

        this.grid.add(rightPaddedLabel(propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_EXPORT_MODE)), 0, rowCount);
        this.grid.add(padding(exportModeChoiceBox, pad), 1, rowCount++);

        this.grid.add(padding(startDatePicker, pad), 0, rowCount);
        this.grid.add(padding(endDatePicker, pad), 1, rowCount++);

        final LocalDate now = LocalDate.now();
        final LocalDate earliestOption = earliest != null ? earliest : now;
        final String earliestStr = propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_EARLIEST_KEY);
        Button earliestBtn = new Button(earliestStr + ": " + DateUtil.toDDmmUUUUSlash(earliestOption));
        grid.add(padding(earliestBtn, new Insets(1, 2, 5, 2)), 0, rowCount);
        earliestBtn.setOnAction(e -> {
            startDatePicker.setValue(earliestOption);
        });

        final LocalDate latestOption = latest != null ? latest : now;
        final String latestStr = propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_LATEST_KEY);
        Objects.requireNonNull(latestStr);
        Button latestBtn = new Button(latestStr + ": " + DateUtil.toDDmmUUUUSlash(latestOption));
        grid.add(padding(latestBtn, new Insets(1, 2, 5, 2)), 1, rowCount++);
        latestBtn.setOnAction(e -> {
            endDatePicker.setValue(latestOption);
        });

        final LocalDate today = LocalDate.now();
        final DayOfWeek dayOfWeek = today.getDayOfWeek();
        final LocalDate startOfWeek = today.minusDays(dayOfWeek.getValue() - 1);

        final String currentWeekText = propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_CURRENT_WEEK_KEY);
        final Button currentWeek = new Button(currentWeekText + ": " + DateUtil.toDDmmUUUUSlash(startOfWeek) + " - " + DateUtil.toDDmmUUUUSlash(today));
        grid.add(padding(currentWeek, new Insets(1, 2, 5, 2)), 0, rowCount);
        currentWeek.setOnAction(e -> {
            startDatePicker.setValue(startOfWeek);
            endDatePicker.setValue(today);
        });

        dialogPane.setContent(bp);

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            if (data == ButtonBar.ButtonData.OK_DONE) {
                LocalDate sDate = startDatePicker.getValue();
                LocalDate eDate = endDatePicker.getValue();
                final DateRange dr;
                if (sDate.compareTo(eDate) > 0)
                    dr = new DateRange(eDate, sDate);
                else
                    dr = new DateRange(sDate, eDate);

                final String selectedItem = exportModeChoiceBox.getSelectionModel().getSelectedItem();
                final boolean isExportedToFile = selectedItem == null || selectedItem.equals(fileMode);
                return ExportParam.builder()
                        .dateRange(dr)
                        .searchText(searchedTextField.getText())
                        .isNumbered(isNumberedCheckBox.isSelected())
                        .exportToFile(isExportedToFile)
                        .build();
            } else {
                return null;
            }
        });
        DialogUtil.disableHeader(this);
    }

    @Data
    @Builder
    public static class ExportParam {
        private final DateRange dateRange;
        private final String searchText;
        private final boolean isNumbered;
        private final boolean exportToFile;
    }
}

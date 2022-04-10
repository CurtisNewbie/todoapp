package com.curtisnewbie.controller;

import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.util.DateUtil;
import com.curtisnewbie.util.DialogUtil;
import com.curtisnewbie.util.FxThreadConfinement;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;

import static com.curtisnewbie.util.LabelFactory.rightPaddedLabel;
import static com.curtisnewbie.util.MarginFactory.wrapWithPadding;

/**
 * Dialog for exporting
 *
 * @author yongjie.zhuang
 */
@FxThreadConfinement
public class ExportDialog extends Dialog<ExportDialog.ExportParam> {

    private final GridPane grid;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final TextField searchedTextField;
    private boolean earliestDateIsShown = false;
    private boolean latestDateIsShown = false;
    private final PropertiesLoader properties = PropertiesLoader.getInstance();

    /**
     * Create TodoJobDialog with {@code start} and {@code end} as the default value of the DatePicker(s)
     */
    public ExportDialog(LocalDate start, LocalDate end, String searchText) {
        final DialogPane dialogPane = getDialogPane();
        this.startDatePicker = new DatePicker();
        this.startDatePicker.setConverter(new LocalDateStringConverter());
        this.endDatePicker = new DatePicker();
        this.endDatePicker.setConverter(new LocalDateStringConverter());
        this.searchedTextField = new TextField(searchText);

        this.startDatePicker.setValue(start);
        this.endDatePicker.setValue(end);

        final BorderPane bp = new BorderPane();
        this.grid = new GridPane();
        bp.setTop(wrapWithPadding(searchedTextField, new Insets(1, 0, 5, 0)));
        bp.setCenter(grid);

        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.grid.add(rightPaddedLabel(properties.getLocalizedProperty(PropertyConstants.TEXT_SEARCH)), 0, 0);
        this.grid.add(wrapWithPadding(searchedTextField, new Insets(1, 2, 5, 0)), 1, 0);
        this.grid.add(wrapWithPadding(startDatePicker, new Insets(1, 2, 5, 0)), 0, 1);
        this.grid.add(wrapWithPadding(endDatePicker, new Insets(1, 2, 5, 0)), 1, 1);
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

                return ExportParam.builder()
                        .dateRange(dr)
                        .searchText(searchedTextField.getText())
                        .build();
            } else {
                return null;
            }
        });
        DialogUtil.disableHeader(this);
    }

    static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    /**
     * Show a button which displays the earliest startDate that the user can select, clicking on it makes the {@code
     * startDatePicker} set to this given {@code earliestDate}
     * <p>
     * Note that this method only works for once
     */
    public void showEarliestDate(LocalDate earliestDate) {
        LocalDate d = earliestDate == null ? LocalDate.now() : earliestDate;
        if (earliestDateIsShown)
            return;
        earliestDateIsShown = true;

        String earliestStr = properties.getLocalizedProperty(PropertyConstants.TEXT_EARLIEST_KEY);
        Objects.requireNonNull(earliestStr);
        Button earliestBtn = new Button(earliestStr + ": " + DateUtil.toDDmmUUUUSlash(d));
        grid.add(wrapWithPadding(earliestBtn, new Insets(1, 2, 5, 2)), 0, 2);
        earliestBtn.setOnAction(e -> {
            startDatePicker.setValue(d);
        });
    }

    /**
     * Show a button which displays the latest startDate that the user can select, clicking on it makes the {@code
     * endDatePicker} set to this given {@code latest}
     * <p>
     * Note that this method only works for once
     */
    public void showLatestDate(LocalDate latestDate) {
        LocalDate d = latestDate == null ? LocalDate.now() : latestDate;
        if (latestDateIsShown)
            return;
        latestDateIsShown = true;

        String latestStr = properties.getLocalizedProperty(PropertyConstants.TEXT_LATEST_KEY);
        Objects.requireNonNull(latestStr);
        Button latestBtn = new Button(latestStr + ": " + DateUtil.toDDmmUUUUSlash(d));
        grid.add(wrapWithPadding(latestBtn, new Insets(1, 2, 5, 2)), 1, 2);
        latestBtn.setOnAction(e -> {
            endDatePicker.setValue(d);
        });
    }

    @Data
    @Builder
    public static class ExportParam {
        private final DateRange dateRange;
        private final String searchText;
    }
}

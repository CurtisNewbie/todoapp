package com.curtisnewbie.controller;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static com.curtisnewbie.util.MarginFactory.*;

/**
 * Dialog for picking range of dates
 *
 * @author yongjie.zhuang
 */
public class DateRangeDialog extends Dialog<DateRange> {

    private final GridPane grid;
    private final Label label;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;

    /**
     * Create TodoJobDialog with current date start date and end date
     */
    public DateRangeDialog() {
        this(new Date().getTime(), new Date().getTime());
    }

    /**
     * Create TodoJobDialog with {@code start} (in milliseconds) and {@code end} (in milliseconds) as the default value of dateRange
     */
    public DateRangeDialog(long start, long end) {
        final DialogPane dialogPane = getDialogPane();
        this.startDatePicker = new DatePicker();
        this.endDatePicker = new DatePicker();

        // -- label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        this.startDatePicker.setValue(Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate());
        this.endDatePicker.setValue(Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate());

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.grid.add(label, 0, 0);
        this.grid.add(wrapWithPadding(startDatePicker, new Insets(1, 2, 5, 2)), 1, 0);
        this.grid.add(wrapWithPadding(endDatePicker, new Insets(1, 2, 5, 2)), 1, 1);
        dialogPane.setContent(grid);

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            if (data == ButtonBar.ButtonData.OK_DONE) {
                LocalDate sDate = startDatePicker.getValue();
                LocalDate eDate = endDatePicker.getValue();
                if (sDate.compareTo(eDate) > 0)
                    return new DateRange(startTimeOf(eDate), startTimeOf(sDate.plusDays(1)));
                else
                    return new DateRange(startTimeOf(sDate), startTimeOf(eDate.plusDays(1)));
            } else {
                return null;
            }
        });
    }

    static javafx.scene.control.Label createContentLabel(String text) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    private static long startTimeOf(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
    }
}

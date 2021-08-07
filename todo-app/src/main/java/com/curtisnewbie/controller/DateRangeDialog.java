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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.util.Objects;

import static com.curtisnewbie.util.MarginFactory.wrapWithPadding;

/**
 * Dialog for picking range of dates
 *
 * @author yongjie.zhuang
 */
@FxThreadConfinement
public class DateRangeDialog extends Dialog<DateRange> {

    private final GridPane grid;
    private final Label label;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private boolean earliestDateIsShown = false;
    private boolean latestDateIsShown = false;
    private final PropertiesLoader properties = PropertiesLoader.getInstance();

    /**
     * Create TodoJobDialog with {@code start} and {@code end} as the default value of the DatePicker(s)
     */
    public DateRangeDialog(LocalDate start, LocalDate end) {
        final DialogPane dialogPane = getDialogPane();
        this.startDatePicker = new DatePicker();
        this.startDatePicker.setConverter(new LocalDateStringConverter());
        this.endDatePicker = new DatePicker();
        this.endDatePicker.setConverter(new LocalDateStringConverter());

        // -- label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        this.startDatePicker.setValue(start);
        this.endDatePicker.setValue(end);

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.grid.add(label, 0, 0);
        this.grid.add(wrapWithPadding(startDatePicker, new Insets(1, 2, 5, 2)), 1, 0);
        this.grid.add(wrapWithPadding(endDatePicker, new Insets(1, 2, 5, 2)), 2, 0);
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
                    return new DateRange(eDate, sDate);
                else
                    return new DateRange(sDate, eDate);
            } else {
                return null;
            }
        });
        DialogUtil.disableHeader(this);
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
        grid.add(wrapWithPadding(earliestBtn, new Insets(1, 0, 5, 2)), 1, 1);
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
        grid.add(wrapWithPadding(latestBtn, new Insets(1, 2, 5, 2)), 2, 1);
        latestBtn.setOnAction(e -> {
            endDatePicker.setValue(d);
        });
    }
}

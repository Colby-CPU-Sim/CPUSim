/*
 * Copyright 2013-2014 SmartBear Software
 * Copyright 2014-2015 The TestFX Contributors
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence"); You may
 * not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the Licence for the
 * specific language governing permissions and limitations under the Licence.
 */
package org.textfx.matcher.control;

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.testfx.api.annotation.Unstable;

import static org.testfx.matcher.base.GeneralMatchers.typeSafeMatcher;

@Unstable(reason = "needs more tests")
public abstract class MoreTableViewMatchers {

    private MoreTableViewMatchers() {
        throw new UnsupportedOperationException();
    }

    //---------------------------------------------------------------------------------------------
    // STATIC METHODS.
    //---------------------------------------------------------------------------------------------

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static Matcher<Node> isEmpty() {
        String descriptionText = "is empty (has no items)";
        return typeSafeMatcher(TableView.class, descriptionText, MoreTableViewMatchers::isTableEmpty);
    }

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static Matcher<Node> hasRowWith(Object value) {
        String descriptionText = "has TableRow with \"" + value + "\"";
        return typeSafeMatcher(TableView.class, descriptionText,
                node -> hasRowWith(node, value));
    }

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static <T> Matcher<Node> rowItem(Matcher<T> value) {
        String descriptionText = "row item matches \"" + value + "\"";
        return typeSafeMatcher(TableRow.class, descriptionText,
                node -> rowItem(node, value));
    }

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static <T> Matcher<Node> cellItem(Matcher<T> value) {
        String descriptionText = "table cell item matches \"" + value + "\"";
        return typeSafeMatcher(TableCell.class, descriptionText,
                node -> cellItem(node, value));
    }

    //---------------------------------------------------------------------------------------------
    // PRIVATE STATIC METHODS.
    //---------------------------------------------------------------------------------------------

    private static boolean hasRowWith(TableView<?> view,
                                      Object value) {
        return view.getItems().stream()
                .anyMatch(item -> item != null && item.equals(value));
    }

    private static boolean rowItem(TableRow<?> view,
                                      Matcher<?> matcher) {
        return matcher.matches(view.getItem());
    }

    private static boolean cellItem(TableCell<?, ?> cell,
                                    Matcher<?> matcher) {
        return matcher.matches(cell.getItem());
    }

    private static boolean isTableEmpty(TableView view) {
        return view.getItems().isEmpty();
    }
}
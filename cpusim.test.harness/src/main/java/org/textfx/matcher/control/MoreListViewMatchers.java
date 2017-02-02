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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.testfx.api.FxAssert;
import org.testfx.api.annotation.Unstable;
import org.testfx.service.finder.NodeFinder;
import org.testfx.service.query.NodeQuery;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.testfx.matcher.base.GeneralMatchers.typeSafeMatcher;

@Unstable(reason = "needs more tests")
public abstract class MoreListViewMatchers {

    //---------------------------------------------------------------------------------------------
    // CONSTANTS.
    //---------------------------------------------------------------------------------------------

    /** @see org.testfx.matcher.control.ListViewMatchers#SELECTOR_LIST_CELL */
    private static final String SELECTOR_LIST_CELL = ".list-cell";

    private MoreListViewMatchers() {
        throw new UnsupportedOperationException();
    }

    //---------------------------------------------------------------------------------------------
    // STATIC METHODS.
    //---------------------------------------------------------------------------------------------

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static Matcher<Node> hasCellValueType(Class<?> value) {
        String descriptionText = "has TableRow with \"" + value + "\"";
        return typeSafeMatcher(ListView.class, descriptionText,
                node -> hasCellValueType(node, value));
    }

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static Matcher<Node> hasValues(Collection<?> values) {
        String descriptionText = "has call values of " + values;
        return typeSafeMatcher(ListView.class, descriptionText,
                node -> hasListValuesInOrder(node, values));
    }

    //---------------------------------------------------------------------------------------------
    // PRIVATE STATIC METHODS.
    //---------------------------------------------------------------------------------------------

    private static boolean hasCellValueType(ListView<?> view,
                                            Class<?> value) {
        checkNotNull(view);
        checkNotNull(value);

        return view.getItems().stream().anyMatch(o -> o != null && value.isAssignableFrom(o.getClass()));
    }

    private static boolean hasListValuesInOrder(ListView<?> view,
                                                Collection<?> values) {
        checkNotNull(view, "view == null");
        checkNotNull(values, "values == null");

        NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
        NodeQuery nodeQuery = nodeFinder.from(view);

        Set<ListCell<?>> cells = nodeQuery.lookup(SELECTOR_LIST_CELL)
                .queryAll();

        if (values.size() != cells.size()) {
            return false;
        }

        Iterator<?> queryIter = cells.stream().map(ListCell::getItem).iterator();
        Iterator<?> valuesIter = values.iterator();

        while (queryIter.hasNext() && valuesIter.hasNext()) {
            if (!Objects.equals(queryIter.next(), valuesIter.next())) {
                return false;
            }
        }

        return !queryIter.hasNext() && !valuesIter.hasNext();
    }
}
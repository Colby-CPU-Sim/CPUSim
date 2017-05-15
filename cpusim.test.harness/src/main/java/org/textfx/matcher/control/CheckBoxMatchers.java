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
import javafx.scene.control.CheckBox;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.testfx.api.annotation.Unstable;

import static org.testfx.matcher.base.GeneralMatchers.typeSafeMatcher;

@Unstable(reason = "needs more tests")
public abstract class CheckBoxMatchers {

    private CheckBoxMatchers() {
        throw new UnsupportedOperationException();
    }

    //---------------------------------------------------------------------------------------------
    // STATIC METHODS.
    //---------------------------------------------------------------------------------------------

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static Matcher<Node> isSelected(boolean value) {
        String descriptionText = "is " + (value ? "" : "not ") + "selected";
        return typeSafeMatcher(CheckBox.class, descriptionText, node -> isSelected(node, value));
    }

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static Matcher<Node> isSelected() {
        return isSelected(true);
    }

    @Factory
    @Unstable(reason = "is missing apidocs")
    public static Matcher<Node> isNotSelected() {
        return isSelected(false);
    }

    //---------------------------------------------------------------------------------------------
    // PRIVATE STATIC METHODS.
    //---------------------------------------------------------------------------------------------

    private static boolean isSelected(CheckBox checkBox,
                                      boolean value) {
        return checkBox.isSelected() == value;
    }
}
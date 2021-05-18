/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml;

import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.ballerinalang.toml.parser.SettingsBuilder;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Test class to test invalid `Settings.toml` file.
 */
public class InvalidSettingsTomlTest {

    private final String expMsg = ".*invalid 'Settings.toml' file.*";

    @Test(description = "Test invalid Settings.toml", expectedExceptions = SettingsTomlException.class,
            expectedExceptionsMessageRegExp = expMsg)
    public void testParseTomlContentFromFile() throws IOException, URISyntaxException, SettingsTomlException {
        URI settingsTomlURI =
                Objects.requireNonNull(getClass().getClassLoader().getResource("invalid-settings.toml")).toURI();
        SettingsBuilder.parseTomlContentFromFile(Paths.get(settingsTomlURI));
    }
}

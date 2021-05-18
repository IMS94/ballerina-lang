/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.command;

import com.google.gson.JsonObject;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Command Execution Test Cases for compiler plugin based code actions.
 */
public class CompilerPluginCodeActionExecutionTest extends AbstractCommandExecutionTest {

    private Project project;

    @BeforeClass
    @Override
    public void init() throws Exception {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_with_codeactions");
        project = compileResult.project();
        super.init();
    }

    @Test(dataProvider = "create-function-data-provider")
    public void testCreateFunction(String config, String source, String command) throws IOException {
        performTest(config, source, command);
    }

    @DataProvider(name = "create-function-data-provider")
    public Object[][] createFunctionDataProvider() {
        return new Object[][]{
                {
                        "compiler_plugin_code_action_exec_config1.json",
                        "package_plugin_user_with_codeactions_1/main.bal",
                        "lstest_package_comp_plugin_with_codeactions_CREATE_VAR"
                }
        };
    }

    @Override
    protected List<Object> getArgs(JsonObject argsObject) {
        return List.of(
                CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE, argsObject.getAsJsonObject("node.range")),
                CommandArgument.from("var.type", argsObject.get("var.type").getAsString())
        );
    }

    @Override
    protected String getSourceRoot() {
        return "compiler-plugins";
    }

    @AfterClass
    public void tearDown() {
        BCompileUtil.clearCachedBala(project);
    }
}

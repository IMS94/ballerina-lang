/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.debugger.test.remote;

import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.ballerinalang.debugger.test.utils.DebugUtils.findFreePort;

/**
 * Test class to test positive scenarios of remote debugging ballerina run command.
 */
public class BallerinaRunRemoteDebugTest extends BaseTestCase {

    private BMainInstance balClient;
    DebugTestRunner debugTestRunner;
    private static final String REMOTE_DEBUG_LISTENING = "Listening for transport dt_socket at address: ";

    @BeforeClass
    public void setup() throws BallerinaTestException {
        String testProjectName = "basic-project";
        String testSingleFileName = "hello_world.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testSingleFileName, false);
        balClient = new BMainInstance(debugTestRunner.getBalServer());
    }

    @Test
    public void testSuspendOnBallerinaModuleRun() throws BallerinaTestException {
        int port = findFreePort();
        String msg = REMOTE_DEBUG_LISTENING + port;
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.debugMain("run", new String[]{"--debug", String.valueOf(port)}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, debugTestRunner.testProjectPath, 10);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testSuspendOnBallerinaFileRun() throws BallerinaTestException {
        int port = findFreePort();
        String msg = REMOTE_DEBUG_LISTENING + port;
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.debugMain("run", new String[]{"--debug", String.valueOf(port),
            debugTestRunner.testEntryFilePath}, null, new String[]{}, new LogLeecher[]{clientLeecher},
            debugTestRunner.testProjectPath, 10);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testSuspendOnBallerinaJarRun() throws BallerinaTestException {
        String testProjectName = "executable-breakpoint-tests";
        String testSingleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testSingleFileName, true);
        balClient = new BMainInstance(debugTestRunner.getBalServer());
        String executablePath = Paths.get("target", "bin", testProjectName.replaceAll("-", "_") + ".jar")
            .toFile().getPath();

        LogLeecher clientLeecher = new LogLeecher(executablePath);
        balClient.runMain("build", new String[]{}, null, new String[]{},
            new LogLeecher[]{clientLeecher}, debugTestRunner.testProjectPath);
        clientLeecher.waitForText(20000);

        int port = findFreePort();
        String msg = REMOTE_DEBUG_LISTENING + port;
        clientLeecher = new LogLeecher(msg);
        balClient.debugMain("run", new String[]{"--debug", String.valueOf(port), executablePath}, null,
            new String[]{}, new LogLeecher[]{clientLeecher}, debugTestRunner.testProjectPath, 10);
        clientLeecher.waitForText(20000);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}

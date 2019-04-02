/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.services;

import java.io.PrintStream;

/**
 * Class contains utility methods for ballerina server error handling.
 */
public class ErrorHandlerUtils {

    private static final PrintStream outStream = System.err;

    /**
     * Print the error.
     *
     * @param throwable Throwable associated with the error
     */
    public static void printError(Throwable throwable) {
        String errorMessage = throwable.getMessage();
        if (errorMessage != null) {
            outStream.println(errorMessage);
        }
    }

    /**
     * Print the error.
     *
     * @param error message to be printed.
     */
    public static void printError(String error) {
        if (error != null) {
            outStream.println(error);
        }
    }
}

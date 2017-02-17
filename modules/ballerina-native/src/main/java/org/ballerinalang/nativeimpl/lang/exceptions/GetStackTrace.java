/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.lang.exceptions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.ExceptionDef;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native functions for ballerina.model.exceptions to Set the message and the category.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.exceptions",
        functionName = "getStackTrace",
        args = {@Argument(name = "e", type = TypeEnum.EXCEPTION)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class GetStackTrace extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        StringBuilder stackTraceBuilder = new StringBuilder();
        ExceptionDef exceptionDef = ((BException) getArgument(context, 0)).value();
        int count = 0;
        while (exceptionDef != null) {
            stackTraceBuilder.append(exceptionDef).append("\n").append(exceptionDef.getStackTrace());
            exceptionDef = exceptionDef.getCause();
            count++;
            if (count > 10 && exceptionDef != null) {
                stackTraceBuilder.append("\tmore ...");
                break;
            }
            if (exceptionDef != null) {
                stackTraceBuilder.append("caused by: ");
            }
        }
        return new BValue[]{new BString(stackTraceBuilder.toString())};
    }
}

// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.test.identifiers;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Quoted identifiers.
 */
public class QuotedIdentifierTest {

    private CompileResult errorTestCompileResult;

    @BeforeClass
    public void setup() {
        errorTestCompileResult = BCompileUtil.compile("test-src/identifiers/field_named_as_error.bal");
    }

    @Test
    public void testErrorConstructorWithErrorField() {
        BRunUtil.invoke(errorTestCompileResult, "testErrorConstructorWithErrorField");
    }

    @Test
    public void testErrorDataWithErrorField() {
        BRunUtil.invoke(errorTestCompileResult, "testErrorDataWithErrorField");
    }

    @Test
    public void testErrorAsObjectField() {
        BRunUtil.invoke(errorTestCompileResult, "testErrorAsObjectField");
    }

    @AfterClass
    public void cleanup() {
        errorTestCompileResult = null;
    }
}

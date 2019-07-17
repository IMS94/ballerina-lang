/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.crypto.nativeimpl;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;

/**
 * Extern function ballerina.crypto:encryptAesGcm.
 *
 * @since 0.990.4
 */
@BallerinaFunction(orgName = "ballerina", packageName = "crypto", functionName = "encryptAesGcm", isPublic = true)
public class EncryptAesGcm {

    public static Object encryptAesGcm(Strand strand, ArrayValue inputValue, ArrayValue keyValue,
                                       ArrayValue ivValue, Object padding, long tagSize) {
        byte[] input = inputValue.getBytes();
        byte[] key = keyValue.getBytes();
        byte[] iv = null;
        if (ivValue != null) {
            iv = ivValue.getBytes();
        }
        return CryptoUtils.aesEncryptDecrypt(CryptoUtils.CipherMode.ENCRYPT, Constants.GCM, padding.toString(), key,
                                             input, iv, tagSize);
    }
}

// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/log;

final string REMOTE_BACKEND_URL1 = "ws://localhost:14400/websocketxyz";
http:WebSocketCaller? serverCaller = ();

@http:WebSocketServiceConfig {
    path: "/client/failure"
}
service clientFailure on new http:WebSocketListener(9091) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp;
        serverCaller = untaint wsEp;
        wsClientEp = new(REMOTE_BACKEND_URL1, config = { callbackService: errorHandlingService });
    }
}
service errorHandlingService = @http:WebSocketServiceConfig {} service {
    resource function onError(http:WebSocketClient caller, error err) {
        if (serverCaller is http:WebSocketCaller) {
            var closeErr = serverCaller->close(statusCode = 1011, reason = <string>err.detail().message,
            timeoutInSecs = 0);
            log:printError("Failed during closing the connection", err = closeErr);
        } else {
            log:printError("serverCaller has not been set");
        }
    }
};

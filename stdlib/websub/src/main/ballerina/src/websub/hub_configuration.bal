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


const string DEFAULT_HOST = "0.0.0.0";
const int DEFAULT_LEASE_SECONDS_VALUE = 86400; //one day
const string DEFAULT_SIGNATURE_METHOD = "SHA256";
const int DEFAULT_CACHE_EXPIRY_SECONDS = 172800; //two days

string hubBasePath = "/";
string hubSubscriptionResourcePath = "/";
string hubPublishResourcePath = "/publish";

http:ServiceAuth hubServiceAuth = {enabled: false};
http:ResourceAuth hubSubscriptionResourceAuth = {enabled: false};
http:ResourceAuth hubPublisherResourceAuth = {enabled: false};

int hubLeaseSeconds = DEFAULT_LEASE_SECONDS_VALUE;
string hubSignatureMethod = DEFAULT_SIGNATURE_METHOD;
RemotePublishConfig remotePublishConfig = {};
boolean hubTopicRegistrationRequired = false;
string hubPublicUrl = "";
http:ClientConfiguration? hubClientConfig = ();

function (WebSubContent content) onMessageFunction = defaultOnMessageFunction;
function (string callback, string topic, WebSubContent content) onDeliveryFunction = defaultOnDeliveryFunction;
function (string callback, string topic, WebSubContent content, http:Response|error response, FailureReason reason)
                  onDeliveryFailureFunction = defaultOnDeliveryFailureFunction;

HubPersistenceStore? hubPersistenceStoreImpl = ();
boolean hubPersistenceEnabled = false;
boolean asyncContentDelivery = false;

# Attaches and starts the Ballerina WebSub Hub service.
#
# + hubServiceListener - The `http:Listener` to which the service is attached
function startHubService(http:Listener hubServiceListener) {
    // TODO : handle errors
    checkpanic hubServiceListener.__attach(getHubService());
    checkpanic hubServiceListener.__start();
}

# Retrieves if persistence is enabled for the Hub.
#
# + return - `true` if persistence is enabled or else `false` otherwise
function isHubPersistenceEnabled() returns boolean {
    return hubPersistenceEnabled;
}

# Retrieves if topics need to be registered at the Hub prior to publishing/subscribing.
#
# + return - `true` if persistence is enabled or else `false` otherwise
function isHubTopicRegistrationRequired() returns boolean {
    return hubTopicRegistrationRequired;
}

function getSignatureMethod(SignatureMethod? signatureMethod) returns string {
    match signatureMethod {
        "SHA256" => {
            return "SHA256";
        }
        "SHA1" => {
            return "SHA1";
        }
    }
    return DEFAULT_SIGNATURE_METHOD;
}

function getRemotePublishConfig(RemotePublishConfig? remotePublish) returns RemotePublishConfig {
    return remotePublish is RemotePublishConfig ? remotePublish : {};
}

function defaultOnMessageFunction(WebSubContent content) {
    log:printDebug("Message received at the hub");
}

function defaultOnDeliveryFunction(string callback, string topic, WebSubContent content) {
    log:printDebug("Content delivery to callback[" + callback + "] successful for topic[" + topic + "]");
}

function defaultOnDeliveryFailureFunction(string callback, string topic, WebSubContent content,
                           http:Response|error response, FailureReason reason) {
    if (reason is FAILURE_REASON_SUBSCRIPTION_GONE) {
       log:printInfo("HTTP 410 response code received: Subscription deleted for callback[" + callback
                     + "], topic[" + topic + "]");
    } else if (reason is FAILURE_REASON_FAILURE_STATUS_CODE) {
       http:Response resp = <http:Response> response;
       log:printError("Error delivering content to callback[" + callback + "] for topic["
                                + topic + "]: received response code " + resp.statusCode.toString());
    } else {
       error err = <error> response;
       log:printError("Error delivering content to callback[" + callback + "] for topic["
                                   + topic + "]: " + <string> err.detail()?.message);
    }
}

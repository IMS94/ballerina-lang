/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.projects.plugins.codeaction;

import java.util.List;

/**
 * Represents a code action.
 */
public class CodeAction {

    private String id;
    private String title;
    private List<CodeActionArgument> arguments;

    private CodeAction(String id, String title, List<CodeActionArgument> arguments) {
        this.id = id;
        this.title = title;
        this.arguments = arguments;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public List<CodeActionArgument> getArguments() {
        return arguments;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setArguments(List<CodeActionArgument> arguments) {
        this.arguments = arguments;
    }

    public static CodeAction from(String id, String title, List<CodeActionArgument> arguments) {
        return new CodeAction(id, title, arguments);
    }
}
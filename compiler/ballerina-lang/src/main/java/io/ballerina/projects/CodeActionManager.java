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
package io.ballerina.projects;

import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.CodeActionPluginContext;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Manages interaction with code actions via compiler plugins.
 *
 * @since 2.0.0
 */
public class CodeActionManager {

    private final Map<String, List<CodeActionDescriptor>> codeActionsMap;

    private CodeActionManager(List<CompilerPluginContextIml> compilerPluginContexts) {
        this.codeActionsMap = new HashMap<>();
        compilerPluginContexts.forEach(compilerPluginContext -> {
            // Find code actions of this context
            compilerPluginContext.codeActions().forEach(codeAction -> {
                // Get supported diagnostic codes of that code action and create a mapping
                codeAction.supportedDiagnosticCodes().forEach(diagnosticCode -> {
                    List<CodeActionDescriptor> codeActions =
                            codeActionsMap.computeIfAbsent(diagnosticCode, k -> new ArrayList<>());
                    codeActions.add(new CodeActionDescriptor(codeAction, compilerPluginContext.compilerPluginInfo()));
                });
            });
        });
    }

    /**
     * Get all available code actions for the provided diagnostic from all available compiler plugins.
     *
     * @param context    Code action context
     * @param diagnostic Diagnostic for which code actions are to be shown
     * @return List of available code actions
     */
    public List<CodeActionInfo> codeActions(CodeActionPluginContext context, Diagnostic diagnostic) {
        List<CodeActionInfo> commands = new LinkedList<>();
        codeActionsMap.getOrDefault(diagnostic.diagnosticInfo().code(), Collections.emptyList())
                .forEach(codeActionDescriptor ->
                        codeActionDescriptor.codeAction().codeActionInfo(context, diagnostic).stream()
                                .peek(codeActionInfo -> {
                                    // We change the provider name with package prefix
                                    codeActionInfo.setProviderName(getModifiedCodeActionName(
                                            diagnostic.diagnosticInfo().code(),
                                            codeActionDescriptor.compilerPluginInfo(),
                                            codeActionDescriptor.codeAction().name()));
                                    codeActionInfo.setArguments(codeActionInfo.getArguments());
                                })
                                .forEach(commands::add));

        return commands;
    }

    /**
     * Executes the code action provided by name.
     *
     * @param codeActionName Name of the code action. Will be prefixed with the package name
     * @param context        Code action context
     * @param arguments      Arguments for the code action to execute
     * @return List of edits to be applied
     */
    public List<DocumentEdit> executeCodeAction(String codeActionName, CodeActionPluginContext context,
                                                List<CodeActionArgument> arguments) {
        String[] parts = codeActionName.split("/");

        String diagnosticCode;
        if (parts.length >= 2) {
            diagnosticCode = parts[0];
        } else {
            return Collections.emptyList();
        }

        CompilerPluginKind compilerPluginKind = null;
        if (parts.length >= 4) {
            compilerPluginKind = CompilerPluginKind.PACKAGE_PROVIDED;
        } else {
            compilerPluginKind = CompilerPluginKind.BUILT_IN;
        }

        if (!codeActionsMap.containsKey(diagnosticCode)) {
            return Collections.emptyList();
        }

        CodeAction codeAction = null;
        for (CodeActionDescriptor descriptor : codeActionsMap.get(diagnosticCode)) {
            if (descriptor.compilerPluginInfo().kind() != compilerPluginKind) {
                continue;
            }

            if (codeActionName.equals(getModifiedCodeActionName(diagnosticCode, descriptor.compilerPluginInfo(),
                    descriptor.codeAction().name()))) {
                codeAction = descriptor.codeAction();
                break;
            }
        }

        if (codeAction == null) {
            return Collections.emptyList();
        }

        return codeAction.execute(context, arguments);
    }

    static CodeActionManager from(List<CompilerPluginContextIml> compilerPluginContexts) {
        return new CodeActionManager(compilerPluginContexts);
    }

    private static String getProviderPrefix(String diagnosticCode, CompilerPluginInfo compilerPluginInfo) {
        if (compilerPluginInfo.kind() == CompilerPluginKind.PACKAGE_PROVIDED) {
            PackageDescriptor descriptor = ((PackageProvidedCompilerPluginInfo) compilerPluginInfo).packageDesc();
            return String.format("%s/%s/%s", diagnosticCode, descriptor.org().value(), descriptor.name().value());
        } else {
            return diagnosticCode;
        }
    }

    private static String getModifiedCodeActionName(String diagnosticCode, CompilerPluginInfo compilerPluginInfo,
                                                    String codeActionName) {
        return getProviderPrefix(diagnosticCode, compilerPluginInfo) + "/" + codeActionName;
    }

    /**
     * A wrapper class to keep track of information of a code action.
     */
    static class CodeActionDescriptor {

        private final CodeAction codeAction;
        private final CompilerPluginInfo compilerPluginInfo;

        public CodeActionDescriptor(CodeAction codeAction, CompilerPluginInfo compilerPluginInfo) {
            this.codeAction = codeAction;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        public CodeAction codeAction() {
            return codeAction;
        }

        public CompilerPluginInfo compilerPluginInfo() {
            return compilerPluginInfo;
        }
    }
}

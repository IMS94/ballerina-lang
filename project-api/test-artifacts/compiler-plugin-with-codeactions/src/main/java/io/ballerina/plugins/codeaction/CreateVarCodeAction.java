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
package io.ballerina.plugins.codeaction;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionProvider;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.projects.plugins.codeaction.ToolingCodeActionContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticProperty;

import java.util.Collections;
import java.util.List;

/**
 * A dummy code action provider.
 */
public class CreateVarCodeAction implements CodeActionProvider {

    public static final String VAR_ASSIGNMENT_REQUIRED = "variable assignment is required";

    @Override
    public List<CodeAction> getCodeActions(ToolingCodeActionContext context, Diagnostic diagnostic) {
        if (!(diagnostic.message().startsWith(VAR_ASSIGNMENT_REQUIRED))) {
            return Collections.emptyList();
        }

        if (diagnostic.properties().isEmpty()) {
            return Collections.emptyList();
        }

        TypeSymbol typeSymbol = ((DiagnosticProperty<TypeSymbol>) diagnostic.properties().get(0)).value();
        if (typeSymbol == null) {
            return Collections.emptyList();
        }

        List<CodeActionArgument> args = List.of(
                CodeActionArgument.from(AddGenericVarExecutor.ARG_NODE_RANGE, diagnostic.location().lineRange()),
                CodeActionArgument.from(AddGenericVarExecutor.ARG_VAR_TYPE, typeSymbol.signature())
        );
        return Collections.singletonList(CodeAction.from(AddGenericVarExecutor.COMMAND, "Create generic variable", args));
    }

    @Override
    public List<DocumentEdit> execute(ToolingCodeActionContext context, List<CodeActionArgument> arguments) {
        AddGenericVarExecutor executor = new AddGenericVarExecutor();
        return executor.execute(context, arguments);
    }

    @Override
    public String name() {
        return "CREATE_VAR";
    }
}

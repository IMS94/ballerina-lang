/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.datamapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.datamapper.utils.SyntaxKindMapper.mapSyntaxKind;

/**
 * Visitor to extract Record Type Structure information.
 */
public class DataMapperNodeVisitor extends NodeVisitor {
    public final HashMap<String, String> restFields;
    public final HashMap<String, Map<String, RecordFieldSymbol>> spreadFields;
    private String rhsSymbolName;
    private SemanticModel model;

    public DataMapperNodeVisitor(String rhsSymbolName) {
        this.restFields = new HashMap<>();
        this.spreadFields = new HashMap<>();
        this.rhsSymbolName = rhsSymbolName;
    }

    public void setModel(SemanticModel model) {
        this.model = model;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        if (variableDeclarationNode.initializer().isPresent()) {
            if (variableDeclarationNode.initializer().get().kind() == SyntaxKind.MAPPING_CONSTRUCTOR) {
                String rightSymbolName = variableDeclarationNode.typedBindingPattern().typeDescriptor().toString().
                        replaceAll(" ", "");
                rightSymbolName = rightSymbolName.replaceAll("\n", "");
                if (this.rhsSymbolName.equals(rightSymbolName)) {
                    MappingConstructorExpressionNode mappingConstructorExpressionNode =
                            (MappingConstructorExpressionNode) variableDeclarationNode.initializer().get();
                    SeparatedNodeList<MappingFieldNode> fields = mappingConstructorExpressionNode.fields();
                    for (MappingFieldNode field : fields) {
                        if (field.kind() == SyntaxKind.SPECIFIC_FIELD) {
                            SpecificFieldNode field1 = (SpecificFieldNode) field;
                            String fieldName = field1.fieldName().toString().replaceAll(" ", "");
                            if (field1.valueExpr().isPresent() && fieldName.contains("\"")) {
                                fieldName = fieldName.replaceAll("\"", "");
                                SyntaxKind fieldKind = field1.valueExpr().get().kind();
                                this.restFields.put(fieldName, mapSyntaxKind(fieldKind));
                            }
                        } else if (field.kind() == SyntaxKind.SPREAD_FIELD){
                            SpreadFieldNode field1 = (SpreadFieldNode) field;
                            Optional<Symbol> symbol = this.model.symbol(field1.valueExpr());
                            if (symbol.isPresent()){
                                TypeSymbol typeSymbol = ((VariableSymbol) symbol.get()).typeDescriptor();
                                typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
                                if(typeSymbol.typeKind() == TypeDescKind.RECORD){
                                    Map<String, RecordFieldSymbol> fieldSymbolMap = ((RecordTypeSymbol) typeSymbol).fieldDescriptors();
                                    if (symbol.get().getName().isPresent()) {
                                        this.spreadFields.put(symbol.get().getName().get(), fieldSymbolMap);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

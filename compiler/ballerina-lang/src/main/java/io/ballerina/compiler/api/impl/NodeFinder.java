/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Finds the enclosing AST node for the given position.
 *
 * @since 2.0.0
 */
class NodeFinder extends BLangNodeVisitor {

    private LineRange range;
    private BLangNode enclosingNode;

    BLangNode lookup(BLangCompilationUnit unit, LineRange range) {
        this.range = range;
        this.enclosingNode = null;

        for (TopLevelNode node : unit.topLevelNodes) {
            if (!PositionUtil.withinRange(this.range, node.getPosition()) || isLambdaFunction(node)) {
                continue;
            }

            ((BLangNode) node).accept(this);
        }

        return this.enclosingNode;
    }

    private void lookupNodes(List<? extends BLangNode> nodes) {
        for (BLangNode node : nodes) {
            if (!PositionUtil.withinRange(this.range, node.pos)) {
                continue;
            }

            node.accept(this);
            // TODO: Check whether we can return from here.
        }
    }

    private void lookupNode(BLangNode node) {
        if (node == null) {
            return;
        }

        if (PositionUtil.withinRange(this.range, node.pos)) {
            node.accept(this);
        }

        if (this.enclosingNode == null && !node.internal) {
            setEnclosingNode(node, node.pos);
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        lookupNodes(funcNode.requiredParams);
        lookupNode(funcNode.restParam);
        lookupNode(funcNode.returnTypeNode);
        lookupNode(funcNode.body);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        lookupNodes(blockFuncBody.stmts);
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        lookupNode(exprFuncBody.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        lookupNodes(externFuncBody.annAttachments);
    }

    @Override
    public void visit(BLangService serviceNode) {
        lookupNodes(serviceNode.resourceFunctions);
        lookupNodes(serviceNode.annAttachments);
        lookupNodes(serviceNode.attachedExprs);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        lookupNode(typeDefinition.typeNode);
    }

    @Override
    public void visit(BLangConstant constant) {
        if (setEnclosingNode(constant, constant.name.pos)) {
            return;
        }

        lookupNode(constant.typeNode);
        lookupNode(constant.expr);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (setEnclosingNode(varNode, varNode.name.pos)) {
            return;
        }

        lookupNode(varNode.typeNode);
        lookupNode(varNode.expr);
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // ignore
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        if (setEnclosingNode(annotationNode, annotationNode.name.pos)) {
            return;
        }

        lookupNode(annotationNode.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (setEnclosingNode(annAttachmentNode, annAttachmentNode.annotationName.pos)) {
            return;
        }

        lookupNode(annAttachmentNode.expr);
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
        // TODO: How to figure out the symbols for the specified keys
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        lookupNode(tableKeyTypeConstraint.keyType);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        lookupNodes(blockNode.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        lookupNode(lockStmtNode.body);
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        lookupNode(unLockNode.body);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        lookupNode(varDefNode.var);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        lookupNode(assignNode.varRef);
        lookupNode(assignNode.expr);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        lookupNode(compoundAssignNode.varRef);
        lookupNode(compoundAssignNode.expr);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        lookupNode(retryNode.retryBody);
        lookupNode(retryNode.retrySpec);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        lookupNode(retryTransaction.transaction);
        lookupNode(retryTransaction.retrySpec);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        lookupNode(retrySpec.retryManagerType);
        lookupNodes(retrySpec.argExprs);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        // ignore
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // ignore
    }

    @Override
    public void visit(BLangReturn returnNode) {
        lookupNode(returnNode.expr);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        lookupNode(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        lookupNode(xmlnsStmtNode.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        lookupNode(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangIf ifNode) {
        lookupNode(ifNode.expr);
        lookupNode(ifNode.body);
        lookupNode(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        lookupNodes(queryAction.queryClauseList);
        lookupNode(queryAction.doClause);
    }

    @Override
    public void visit(BLangMatch matchNode) {
        lookupNode(matchNode.expr);
        lookupNodes(matchNode.patternClauses);
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        lookupNode(patternClauseNode.matchExpr);
        lookupNode(patternClauseNode.variable);
        lookupNode(patternClauseNode.body);
    }

    @Override
    public void visit(BLangForeach foreach) {
        lookupNode((BLangNode) foreach.variableDefinitionNode);
        lookupNode(foreach.collection);
        lookupNode(foreach.body);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        lookupNode(fromClause.collection);
        lookupNode((BLangNode) fromClause.variableDefinitionNode);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        lookupNode(joinClause.collection);
        lookupNode((BLangNode) joinClause.variableDefinitionNode);
        lookupNode((BLangNode) joinClause.onClause);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable var : letClause.letVarDeclarations) {
            lookupNode((BLangNode) var.definitionNode);
        }
    }

    @Override
    public void visit(BLangOnClause onClause) {
        lookupNode(onClause.lhsExpr);
        lookupNode(onClause.rhsExpr);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        lookupNode(orderKeyClause.expression);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        for (OrderKeyNode key : orderByClause.orderByKeyList) {
            lookupNode((BLangNode) key);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        lookupNode(selectClause.expression);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        lookupNode(whereClause.expression);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        lookupNode(doClause.body);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        lookupNode(onConflictClause.expression);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        lookupNode(limitClause.expression);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        lookupNode(whileNode.expr);
        lookupNode(whileNode.body);
    }

    @Override
    public void visit(BLangLock lockNode) {
        lookupNode(lockNode.body);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        lookupNode(transactionNode.transactionBody);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        lookupNode(stmt.expr);
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        lookupNode(stmt.expr);
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        lookupNode(stmt.expr);
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        lookupNodes(forkJoin.workers);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        if (setEnclosingNode(workerSendNode.expr, workerSendNode.expr.pos)) {
            return;
        }

        setEnclosingNode(workerSendNode, workerSendNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        setEnclosingNode(workerReceiveNode, workerReceiveNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        lookupNode(rollbackNode.expr);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        this.enclosingNode = literalExpr;
    }

    @Override
    public void visit(BLangConstRef constRef) {
        this.enclosingNode = constRef;
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        this.enclosingNode = literalExpr;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        if (setEnclosingNode(recordLiteral, recordLiteral.pos)) {
            return;
        }

        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            lookupNode((BLangNode) field);
        }
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        if (setEnclosingNode(varRefExpr, varRefExpr.pos)) {
            return;
        }

        lookupNodes(varRefExpr.expressions);
        lookupNode((BLangNode) varRefExpr.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        // TODO: implement this
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        if (setEnclosingNode(varRefExpr, varRefExpr.pos)) {
            return;
        }

        lookupNode(varRefExpr.message);
        lookupNodes(varRefExpr.detail);
        lookupNode(varRefExpr.cause);
        lookupNode(varRefExpr.restVar);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (setEnclosingNode(varRefExpr, varRefExpr.variableName.pos)) {
            return;
        }

        setEnclosingNode(varRefExpr, varRefExpr.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (setEnclosingNode(fieldAccessExpr, fieldAccessExpr.pos)
                || setEnclosingNode(fieldAccessExpr, fieldAccessExpr.field.pos)) {
            return;
        }

        lookupNode(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (setEnclosingNode(indexAccessExpr, indexAccessExpr.pos)) {
            return;
        }

        lookupNode(indexAccessExpr.expr);
        lookupNode(indexAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (setEnclosingNode(invocationExpr, invocationExpr.pos)
                || setEnclosingNode(invocationExpr, invocationExpr.name.pos)) {
            return;
        }

        lookupNodes(invocationExpr.requiredArgs);
        lookupNodes(invocationExpr.restArgs);
        lookupNode(invocationExpr.expr);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        if (setEnclosingNode(typeInit, typeInit.pos)) {
            return;
        }

        lookupNode(typeInit.userDefinedType);
        lookupNodes(typeInit.argsExpr);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        if (setEnclosingNode(actionInvocationExpr, actionInvocationExpr.pos)
                || setEnclosingNode(actionInvocationExpr, actionInvocationExpr.name.pos)) {
            return;
        }

        lookupNodes(actionInvocationExpr.requiredArgs);
        lookupNodes(actionInvocationExpr.restArgs);
        lookupNode(actionInvocationExpr.expr);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        if (setEnclosingNode(ternaryExpr, ternaryExpr.pos)) {
            return;
        }

        lookupNode(ternaryExpr.expr);
        lookupNode(ternaryExpr.thenExpr);
        lookupNode(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        if (setEnclosingNode(awaitExpr, awaitExpr.pos)) {
            return;
        }

        lookupNodes(awaitExpr.exprList);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        if (setEnclosingNode(trapExpr, trapExpr.pos)) {
            return;
        }

        lookupNode(trapExpr.expr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (setEnclosingNode(binaryExpr, binaryExpr.pos)) {
            return;
        }

        lookupNode(binaryExpr.lhsExpr);
        lookupNode(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        if (setEnclosingNode(elvisExpr, elvisExpr.pos)) {
            return;
        }

        lookupNode(elvisExpr.lhsExpr);
        lookupNode(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        if (setEnclosingNode(groupExpr, groupExpr.pos)) {
            return;
        }

        lookupNode(groupExpr.expression);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        if (setEnclosingNode(letExpr, letExpr.pos)) {
            return;
        }

        for (BLangLetVariable var : letExpr.letVarDeclarations) {
            lookupNode((BLangNode) var.definitionNode);
        }

        lookupNode(letExpr.expr);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        if (setEnclosingNode(listConstructorExpr, listConstructorExpr.pos)) {
            return;
        }

        lookupNodes(listConstructorExpr.exprs);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        if (setEnclosingNode(tableConstructorExpr, tableConstructorExpr.pos)) {
            return;
        }

        lookupNode(tableConstructorExpr.tableKeySpecifier);
        lookupNodes(tableConstructorExpr.recordLiteralList);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        if (setEnclosingNode(tupleLiteral, tupleLiteral.pos)) {
            return;
        }

        lookupNodes(tupleLiteral.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        lookupNodes(arrayLiteral.exprs);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (setEnclosingNode(unaryExpr, unaryExpr.pos)) {
            return;
        }

        lookupNode(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        if (setEnclosingNode(typedescExpr, typedescExpr.pos)) {
            return;
        }

        lookupNode(typedescExpr.typeNode);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        lookupNodes(conversionExpr.annAttachments);
        lookupNode(conversionExpr.typeNode);
        lookupNode(conversionExpr.expr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        if (setEnclosingNode(xmlQName, xmlQName.pos)
                || setEnclosingNode(xmlQName, xmlQName.prefix.pos)) {
            return;
        }

        setEnclosingNode(xmlQName, xmlQName.localname.pos);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {

    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        if (setEnclosingNode(xmlElementLiteral, xmlElementLiteral.pos)) {
            return;
        }

        lookupNode(xmlElementLiteral.startTagName);
        lookupNodes(xmlElementLiteral.attributes);
        lookupNodes(xmlElementLiteral.children);
        lookupNode(xmlElementLiteral.endTagName);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        if (setEnclosingNode(xmlTextLiteral, xmlTextLiteral.pos)) {
            return;
        }

        lookupNode(xmlTextLiteral.concatExpr);
        lookupNodes(xmlTextLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        if (setEnclosingNode(xmlCommentLiteral, xmlCommentLiteral.pos)) {
            return;
        }

        lookupNode(xmlCommentLiteral.concatExpr);
        lookupNodes(xmlCommentLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        if (setEnclosingNode(xmlProcInsLiteral, xmlProcInsLiteral.pos)) {
            return;
        }

        lookupNode(xmlProcInsLiteral.dataConcatExpr);
        lookupNodes(xmlProcInsLiteral.dataFragments);
        lookupNode(xmlProcInsLiteral.target);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        if (setEnclosingNode(xmlQuotedString, xmlQuotedString.pos)) {
            return;
        }

        lookupNode(xmlQuotedString.concatExpr);
        lookupNodes(xmlQuotedString.textFragments);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        if (setEnclosingNode(stringTemplateLiteral, stringTemplateLiteral.pos)) {
            return;
        }

        lookupNodes(stringTemplateLiteral.exprs);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        if (setEnclosingNode(rawTemplateLiteral, rawTemplateLiteral.pos)) {
            return;
        }

        lookupNodes(rawTemplateLiteral.strings);
        lookupNodes(rawTemplateLiteral.insertions);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (setEnclosingNode(bLangLambdaFunction, bLangLambdaFunction.pos)) {
            return;
        }

        lookupNode(bLangLambdaFunction.function);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        if (setEnclosingNode(bLangArrowFunction, bLangArrowFunction.pos)) {
            return;
        }

        lookupNodes(bLangArrowFunction.params);
        lookupNode(bLangArrowFunction.body);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        if (setEnclosingNode(intRangeExpression, intRangeExpression.pos)) {
            return;
        }

        lookupNode(intRangeExpression.startExpr);
        lookupNode(intRangeExpression.endExpr);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        if (setEnclosingNode(bLangVarArgsExpression, bLangVarArgsExpression.pos)) {
            return;
        }

        lookupNode(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        if (setEnclosingNode(bLangNamedArgsExpression, bLangNamedArgsExpression.pos)) {
            return;
        }

        lookupNode(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        if (setEnclosingNode(assignableExpr, assignableExpr.pos)) {
            return;
        }

        lookupNode(assignableExpr.lhsExpr);
        lookupNode(assignableExpr.typeNode);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        if (setEnclosingNode(checkedExpr, checkedExpr.pos)) {
            return;
        }

        lookupNode(checkedExpr.expr);
    }

    @Override
    public void visit(BLangFail failExpr) {
        if (setEnclosingNode(failExpr, failExpr.pos)) {
            return;
        }

        lookupNode(failExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        if (setEnclosingNode(checkPanickedExpr, checkPanickedExpr.pos)) {
            return;
        }

        lookupNode(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        if (setEnclosingNode(serviceConstructorExpr, serviceConstructorExpr.pos)) {
            return;
        }

        lookupNode(serviceConstructorExpr.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        if (setEnclosingNode(typeTestExpr, typeTestExpr.pos)) {
            return;
        }

        lookupNode(typeTestExpr.expr);
        lookupNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        if (setEnclosingNode(typeTestExpr, typeTestExpr.pos)) {
            return;
        }

        lookupNode(typeTestExpr.expr);
        lookupNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        // ignore
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        setEnclosingNode(annotAccessExpr, annotAccessExpr.pos);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        if (setEnclosingNode(queryExpr, queryExpr.pos)) {
            return;
        }

        lookupNodes(queryExpr.queryClauseList);
    }

    @Override
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        super.visit(tableMultiKeyExpr);
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
        super.visit(transactionalExpr);
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        setEnclosingNode(commitExpr, commitExpr.pos);
    }

    @Override
    public void visit(BLangValueType valueType) {
        this.enclosingNode = valueType;
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        if (setEnclosingNode(arrayType, arrayType.pos)) {
            return;
        }

        lookupNode(arrayType.elemtype);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        this.enclosingNode = builtInRefType;
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        lookupNode(constrainedType.constraint);

        if (this.enclosingNode == null) {
            this.enclosingNode = constrainedType;
        }
    }

    @Override
    public void visit(BLangStreamType streamType) {
        if (setEnclosingNode(streamType, streamType.pos)) {
            return;
        }

        lookupNode(streamType.constraint);
        lookupNode(streamType.error);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        if (setEnclosingNode(tableType, tableType.pos)) {
            return;
        }

        lookupNode(tableType.constraint);
        lookupNode(tableType.tableKeySpecifier);
        lookupNode(tableType.tableKeyTypeConstraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        if (userDefinedType.type.tsymbol.origin == VIRTUAL
                || setEnclosingNode(userDefinedType, userDefinedType.pos)) {
            return;
        }

        setEnclosingNode(userDefinedType, userDefinedType.typeName.pos);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        if (setEnclosingNode(functionTypeNode, functionTypeNode.pos)) {
            return;
        }

        lookupNodes(functionTypeNode.params);
        lookupNode(functionTypeNode.restParam);
        lookupNode(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        if (setEnclosingNode(unionTypeNode, unionTypeNode.pos)) {
            return;
        }

        lookupNodes(unionTypeNode.memberTypeNodes);
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        if (setEnclosingNode(intersectionTypeNode, intersectionTypeNode.pos)) {
            return;
        }

        lookupNodes(intersectionTypeNode.constituentTypeNodes);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        // skip the generated class def for services
        if (classDefinition.flagSet.contains(Flag.SERVICE)) {
            return;
        }

        if (setEnclosingNode(classDefinition, classDefinition.pos)
                || setEnclosingNode(classDefinition, classDefinition.name.pos)) {
            return;
        }

        lookupNodes(classDefinition.annAttachments);
        lookupNodes(classDefinition.fields);
        lookupNodes(classDefinition.referencedFields);
        lookupNode(classDefinition.initFunction);
        lookupNodes(classDefinition.functions);
        lookupNodes(classDefinition.typeRefs);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        if (setEnclosingNode(objectTypeNode, objectTypeNode.pos)) {
            return;
        }

        lookupNodes(objectTypeNode.fields);
        lookupNodes(objectTypeNode.functions);
        lookupNodes(objectTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (setEnclosingNode(recordTypeNode, recordTypeNode.pos)) {
            return;
        }

        lookupNodes(recordTypeNode.fields);
        lookupNodes(recordTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        if (setEnclosingNode(finiteTypeNode, finiteTypeNode.pos)) {
            return;
        }

        lookupNodes(finiteTypeNode.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        if (setEnclosingNode(tupleTypeNode, tupleTypeNode.pos)) {
            return;
        }

        lookupNodes(tupleTypeNode.memberTypeNodes);
        lookupNode(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        if (setEnclosingNode(errorType, errorType.pos)) {
            return;
        }

        lookupNode(errorType.detailType);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        this.enclosingNode = localVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        this.enclosingNode = fieldVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        this.enclosingNode = packageVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        this.enclosingNode = functionVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        this.enclosingNode = typeLoad;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        if (setEnclosingNode(fieldAccessExpr, fieldAccessExpr.pos)) {
            return;
        }

        lookupNode(fieldAccessExpr.expr);
        setEnclosingNode(fieldAccessExpr, fieldAccessExpr.indexExpr.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        if (setEnclosingNode(functionVarRef, functionVarRef.pos)
                || setEnclosingNode(functionVarRef, functionVarRef.field.pos)) {
            return;
        }

        lookupNode(functionVarRef.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        if (setEnclosingNode(mapKeyAccessExpr, mapKeyAccessExpr.pos)) {
            return;
        }

        lookupNode(mapKeyAccessExpr.expr);
        lookupNode(mapKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        if (setEnclosingNode(arrayIndexAccessExpr, arrayIndexAccessExpr.pos)) {
            return;
        }

        lookupNode(arrayIndexAccessExpr.expr);
        lookupNode(arrayIndexAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        if (setEnclosingNode(tableKeyAccessExpr, tableKeyAccessExpr.pos)) {
            return;
        }

        lookupNode(tableKeyAccessExpr.expr);
        lookupNode(tableKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        if (setEnclosingNode(xmlAccessExpr, xmlAccessExpr.pos)) {
            return;
        }

        lookupNode(xmlAccessExpr.expr);
        lookupNode(xmlAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        for (RecordLiteralNode.RecordField field : mapLiteral.fields) {
            lookupNode((BLangNode) field);
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        for (RecordLiteralNode.RecordField field : structLiteral.fields) {
            lookupNode((BLangNode) field);
        }
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        if (setEnclosingNode(bFunctionPointerInvocation, bFunctionPointerInvocation.pos)
                || setEnclosingNode(bFunctionPointerInvocation, bFunctionPointerInvocation.name.pos)) {
            return;
        }

        lookupNodes(bFunctionPointerInvocation.requiredArgs);
        lookupNodes(bFunctionPointerInvocation.restArgs);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        if (setEnclosingNode(iExpr, iExpr.pos)
                || setEnclosingNode(iExpr, iExpr.name.pos)) {
            return;
        }

        lookupNode(iExpr.expr);
        lookupNodes(iExpr.requiredArgs);
        lookupNodes(iExpr.restArgs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        if (setEnclosingNode(jsonArrayLiteral, jsonArrayLiteral.pos)) {
            return;
        }

        lookupNodes(jsonArrayLiteral.exprs);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        if (setEnclosingNode(jsonAccessExpr, jsonAccessExpr.pos)) {
            return;
        }

        lookupNode(jsonAccessExpr.expr);
        lookupNode(jsonAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        if (setEnclosingNode(stringAccessExpr, stringAccessExpr.pos)) {
            return;
        }

        lookupNode(stringAccessExpr.expr);
        lookupNode(stringAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode, xmlnsNode.pos)
                || setEnclosingNode(xmlnsNode, xmlnsNode.prefix.pos)) {
            return;
        }

        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode, xmlnsNode.pos)
                || setEnclosingNode(xmlnsNode, xmlnsNode.prefix.pos)) {
            return;
        }

        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        if (setEnclosingNode(bLangXMLSequenceLiteral, bLangXMLSequenceLiteral.pos)) {
            return;
        }

        lookupNodes(bLangXMLSequenceLiteral.xmlItems);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        lookupNode(bLangStatementExpression.expr);
        lookupNode(bLangStatementExpression.stmt);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        // ignore
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        // ignore
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangMarkDownDeprecationDocumentation bLangMarkDownDeprecationDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangMarkDownDeprecatedParametersDocumentation bLangMarkDownDeprecatedParametersDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        lookupNodes(bLangTupleVariable.memberVariables);
        lookupNode(bLangTupleVariable.restVariable);
        lookupNode(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        lookupNode(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        for (BLangRecordVariable.BLangRecordVariableKeyValue var : bLangRecordVariable.variableList) {
            lookupNode(var.valueBindingPattern);
        }
        lookupNode((BLangNode) bLangRecordVariable.restParam);
        lookupNodes(bLangRecordVariable.annAttachments);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        lookupNode(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        lookupNode(bLangErrorVariable.message);

        for (BLangErrorVariable.BLangErrorDetailEntry detail : bLangErrorVariable.detail) {
            lookupNode(detail.valueBindingPattern);
        }

        lookupNode(bLangErrorVariable.detailExpr);
        lookupNode(bLangErrorVariable.cause);
        lookupNode(bLangErrorVariable.reasonMatchConst);
        lookupNode(bLangErrorVariable.restDetail);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        lookupNode(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        lookupNode(bLangMatchStmtStaticBindingPatternClause.matchExpr);
        lookupNode(bLangMatchStmtStaticBindingPatternClause.literal);
        lookupNode(bLangMatchStmtStaticBindingPatternClause.body);
    }

    @Override
    public void visit(BLangMatchStructuredBindingPatternClause bLangMatchStmtStructuredBindingPatternClause) {
        lookupNode(bLangMatchStmtStructuredBindingPatternClause.bindingPatternVariable);
        lookupNode(bLangMatchStmtStructuredBindingPatternClause.typeGuardExpr);
        lookupNode(bLangMatchStmtStructuredBindingPatternClause.matchExpr);
        lookupNode(bLangMatchStmtStructuredBindingPatternClause.body);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        if (setEnclosingNode(workerFlushExpr, workerFlushExpr.pos)) {
            return;
        }

        setEnclosingNode(workerFlushExpr, workerFlushExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        if (setEnclosingNode(syncSendExpr, syncSendExpr.pos)
                || setEnclosingNode(syncSendExpr.expr, syncSendExpr.expr.pos)) {
            return;
        }

        setEnclosingNode(syncSendExpr, syncSendExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        super.visit(waitForAllExpr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        super.visit(waitLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        lookupNode(recordKeyValue.key);
        lookupNode(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey recordKey) {
        lookupNode(recordKey.expr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        if (setEnclosingNode(spreadOperatorField, spreadOperatorField.pos)) {
            return;
        }

        lookupNode(spreadOperatorField.expr);
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        super.visit(waitKeyValue);
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        setEnclosingNode(xmlElementFilter, xmlElementFilter.elemNamePos);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        if (setEnclosingNode(xmlElementAccess, xmlElementAccess.pos)) {
            return;
        }

        lookupNode(xmlElementAccess.expr);
        lookupNodes(xmlElementAccess.filters);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        if (setEnclosingNode(xmlNavigation, xmlNavigation.pos)) {
            return;
        }

        lookupNode(xmlNavigation.expr);
        lookupNode(xmlNavigation.childIndex);
        lookupNodes(xmlNavigation.filters);
    }

    private boolean setEnclosingNode(BLangNode node, DiagnosticPos pos) {
        if (PositionUtil.withinRange(this.range, pos) && this.enclosingNode == null) {
            this.enclosingNode = node;
            return true;
        }

        return false;
    }

    private boolean isLambdaFunction(TopLevelNode node) {
        if (node.getKind() != NodeKind.FUNCTION) {
            return false;
        }

        BLangFunction func = (BLangFunction) node;
        return func.flagSet.contains(Flag.LAMBDA);
    }
}

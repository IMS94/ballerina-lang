/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.QueryConstructTypeNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link QueryExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class QueryExpressionNodeContext extends AbstractCompletionProvider<QueryExpressionNode> {

    public QueryExpressionNodeContext() {
        super(QueryExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, QueryExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (node.queryConstructType().isPresent() && this.onQueryConstructType(context, node.queryConstructType().get())) {
            // 1. table key() <cursor>
            // 2. stream <cursor>
            QueryConstructTypeNode queryConstructType = node.queryConstructType().get();
            int cursor = context.getCursorPositionInTree();
            if (queryConstructType.textRange().endOffset() < cursor &&
                    cursor <= queryConstructType.textRangeWithMinutiae().endOffset()) {
                completionItems.addAll(getKeywordCompletionItems(context, node));
            }
        } else if (this.onQueryPipeLine(context, node)) {
            /*
             * we suggest intermediate clause snippets, the keywords and the select keyword as well. This is to support
             * multiple intermediate clauses and the select clause.
             * query-expr := [query-construct-type] query-pipeline select-clause [on-conflict-clause]
             */
            completionItems.addAll(getKeywordCompletionItems(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onQueryConstructType(BallerinaCompletionContext context, QueryConstructTypeNode node) {
        int cursor = context.getCursorPositionInTree();
        return !node.isMissing() &&
                !node.keyword().isMissing() &&
                node.textRangeWithMinutiae().endOffset() >= cursor;
    }

    private boolean onQueryPipeLine(BallerinaCompletionContext context, QueryExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        QueryPipelineNode queryPipeline = node.queryPipeline();
        Optional<OnConflictClauseNode> onConflictClause = node.onConflictClause();

        return !queryPipeline.isMissing() && queryPipeline.textRange().endOffset() < cursor
                && (onConflictClause.isEmpty() || onConflictClause.get().textRange().startOffset() > cursor);
    }

    private List<LSCompletionItem> getKeywordCompletionItems(BallerinaCompletionContext context,
                                                             QueryExpressionNode node) {
        List<LSCompletionItem> completionItems = Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_FROM.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()),
                new SnippetCompletionItem(context, Snippet.KW_WHERE.get()),
                new SnippetCompletionItem(context, Snippet.KW_LET.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_LET.get()),
                new SnippetCompletionItem(context, Snippet.KW_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.KW_ORDERBY.get()),
                new SnippetCompletionItem(context, Snippet.KW_LIMIT.get())
        );

        if (!node.queryPipeline().fromClause().isMissing()) {
            /*
             * It is mandatory to have at least one pipeline clause.
             * Only if that is true we suggest the select clause
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SELECT.get()));
            // Similarly do clause requires at least one query pipeline clause
            completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_DO.get()));
        }

        return completionItems;
    }
}

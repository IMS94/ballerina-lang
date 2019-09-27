package org.ballerinalang.model.tree.types;
/**
 * @since 1.0.2
 * Used to identify the type of backticked reference in Markdown Documentation strings
 */
public enum DocumentationReferenceType {
    TYPE,
    SERVICE,
    VARIABLE,
    ANNOTATION,
    CONST,
    MODULE,
    FUNCTION,
    PARAMETER,
    BACKTICK_CONTENT // This content is extracted from only backticks. Hence should validate for a function
}

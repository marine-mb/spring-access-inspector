package com.theodo.tools.preauthorize.analyzer.impl.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;

import java.util.Collection;
import java.util.List;

public class LiteralExtraction {
    // Search Literal Value from an AST Element
    // Literal can be a "string" or a native value
    // Literal can be a class field reference -> need to search value of the field
    // Literal can be a method variable or parameter -> need to try to search the value of the variable
    public static String extract(CtElement element, boolean normalizeResult) {
        if (element == null) return null;
        try {
            element.accept(new CtScanner() {
                @Override
                public <T> void visitCtLiteral(CtLiteral<T> literal) {
                    throw new StopException(literal.getValue().toString());
                }

                @Override
                public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
                    if(BinaryOperatorKind.PLUS.equals(operator.getKind())){
                        CtExpression<?> leftHandOperand = operator.getLeftHandOperand();
                        CtExpression<?> rightHandOperand = operator.getRightHandOperand();

                        String left = extract(leftHandOperand, normalizeResult);
                        String right = extract(rightHandOperand, normalizeResult);

                        if(right == null && rightHandOperand instanceof CtVariableRead<?>){
                            right = "%s";
                        }
                        if(left != null && right != null) {
                            throw new StopException(left + right);
                        }
                        if(left != null) throw new StopException(left);
                        if(right != null) throw new StopException(right);
                    }
                }

                @Override
                public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
                    visitCtVariableRead(fieldRead);
                }

                @Override
                public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
                    CtVariableReference<T> ctVariableReference = variableRead.getVariable();
                    if (ctVariableReference == null) return;

                    CtVariable<T> declaration = ctVariableReference.getDeclaration();
                    if (declaration == null) {
                        handleFieldsReadsIfPossible(variableRead, ctVariableReference);
                        return;
                    }

                    CtExpression<T> defaultExpression = declaration.getDefaultExpression();
                    String extracted = extract(defaultExpression, normalizeResult);
                    if(extracted != null){
                        throw new StopException(extracted);
                    }
                }

                private <T> void handleFieldsReadsIfPossible(CtVariableRead<T> variableRead, CtVariableReference<T> ctVariableReference) {
                    if (variableRead instanceof CtFieldRead<?> ctFieldRead) {
                        CtFieldReference<?> fieldReference = ctFieldRead.getVariable();
                        CtTypeReference<?> declaringTypeRef = fieldReference.getDeclaringType();
                        if (declaringTypeRef == null) {
                            declaringTypeRef = fieldReference.getType();
                            if (declaringTypeRef == null) return;

                            CtType<?> declaringTypeThatContainsField = declaringTypeRef.getDeclaration();
                            if (declaringTypeThatContainsField != null) {
                                List<CtTypeMember> typeMembers = declaringTypeThatContainsField.getTypeMembers();
                                typeMembers.stream().filter(ctTypeMember -> ctTypeMember instanceof CtClass<?>)
                                        .forEach(ctSubclassType -> {
                                            Collection<CtFieldReference<?>> allFields = ((CtClass<?>) ctSubclassType).getAllFields();
                                            fuzzySearchOnFieldsName(ctVariableReference, allFields);
                                        });
                            }
                        }

                        Collection<CtFieldReference<?>> allFields = declaringTypeRef.getAllFields();
                        fuzzySearchOnFieldsName(ctVariableReference, allFields);
                    }
                }

                private <T> void fuzzySearchOnFieldsName(CtVariableReference<T> variable, Collection<CtFieldReference<?>> allFields) {
                    for (CtFieldReference<?> oneFieldRef : allFields) {
                        // CONTAINS and NOT EQUALS, there is a BUG (?) in spoon (fields from inner classes have wrong simple names)
                        if (variable.getSimpleName().contains(oneFieldRef.getSimpleName())) {
                            CtField<?> fieldRefDeclaration = oneFieldRef.getDeclaration();
                            if (fieldRefDeclaration != null && fieldRefDeclaration.getDefaultExpression() != null) {
                                String extract = extract(fieldRefDeclaration.getDefaultExpression(), normalizeResult);
                                if(extract != null){
                                    throw new StopException(extract);
                                }
                            }
                        }
                    }
                }
            });
        } catch (StopException e) {
            if(normalizeResult) {
                return e.value.replaceAll("%s", "{param}");
            } else {
                return e.value;
            }
        }
        return null;
    }

    @Getter
    @RequiredArgsConstructor
    private static class StopException extends RuntimeException {
        private final String value;
    }
}

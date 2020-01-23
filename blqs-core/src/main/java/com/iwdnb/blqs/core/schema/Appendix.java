package com.iwdnb.blqs.core.schema;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.iwdnb.blqs.core.common.Element;
import com.iwdnb.blqs.core.resolver.ast.Enums;
import com.iwdnb.blqs.core.resolver.ast.Fields;

import lombok.Getter;
import lombok.Setter;

/**
 * 附录
 */
@Setter
@Getter
public class Appendix extends Node {

    List<Element> elementList = new ArrayList<>();

    public boolean isEmpty() {
        return elementList.isEmpty();
    }

    @Nullable
    public static Appendix parse(JavadocComment n) {
        if (!n.getCommentedNode().isPresent()) {
            return null;
        }
        final com.github.javaparser.ast.Node node = n.getCommentedNode().get();
        if (!(node instanceof BodyDeclaration)) {
            return null;
        }
        final BodyDeclaration bodyDeclaration = (BodyDeclaration) node;
        if (!bodyDeclaration.isEnumDeclaration() && !bodyDeclaration.isClassOrInterfaceDeclaration()) {
            return null;
        }
        Appendix appendix = new Appendix();
        if (bodyDeclaration.isEnumDeclaration()) {
            appendix.getElementList().addAll(Enums.toDetails(bodyDeclaration.asEnumDeclaration()));
        } else if (bodyDeclaration.isClassOrInterfaceDeclaration()) {
            appendix.getElementList().addAll(Fields.getConstants(bodyDeclaration.asClassOrInterfaceDeclaration()));
        }
        if (node instanceof NodeWithSimpleName) {
            appendix.setName(((NodeWithSimpleName) node).getNameAsString());
        }
        appendix.accept(node.getComment());
        return appendix;
    }

}

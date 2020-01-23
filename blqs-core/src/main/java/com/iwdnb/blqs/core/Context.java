package com.iwdnb.blqs.core;

import java.util.Collection;
import java.util.Set;

import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.iwdnb.blqs.core.handler.TreeHandler;
import com.iwdnb.blqs.core.handler.swagger.SwaggerTreeHandler;
import com.iwdnb.blqs.core.schema.Tree;

import lombok.Getter;
import lombok.Setter;

/**
 * should use Blqs
 */
@Getter
@Setter
public class Context {

    public static final String               NAME               = "blqs";

    public static final Integer              DEFAULT_NODE_INDEX = 99;

    public static final ThreadLocal<Context> THREAD_LOCAL       = new ThreadLocal<>();

    Context(){
        THREAD_LOCAL.set(this);
    }

    public static Context getContext() {
        return THREAD_LOCAL.get();
    }

    public static BlqsContext getApigcc() {
        Context context = getContext();
        if (context instanceof BlqsContext) {
            return (BlqsContext) context;
        }
        throw new IllegalStateException("context error : " + context);
    }

    protected CombinedTypeSolver      typeSolver  = new CombinedTypeSolver();
    protected Options                 options;
    protected Tree                    tree        = new Tree();

    // protected Collection<TreeHandler> pipeline = Lists.newArrayList(new PostmanTreeHandler(), new
    // AsciidocTreeHandler(), new HtmlTreeHandler(),new SwaggerTreeHandler());
    protected Collection<TreeHandler> pipeline    = Lists.newArrayList(new SwaggerTreeHandler());

    protected Set<String>             ignoreTypes = Sets.newHashSet();
}

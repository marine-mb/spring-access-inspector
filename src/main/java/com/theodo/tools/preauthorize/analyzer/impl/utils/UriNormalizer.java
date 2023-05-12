package com.theodo.tools.preauthorize.analyzer.impl.utils;

public class UriNormalizer {
    // Make all URI looks like "/something/other/{param}/other2/{param}
    public static String normalizeUri(String methodContext) {
        if (methodContext == null) return null;

        // ensure URI starts with "/"
        if (!methodContext.startsWith("/")) {
            methodContext = "/" + methodContext;
        }

        // replace all path params {xyz} by {param}, confusing for URI matching
        methodContext = methodContext.replaceAll("\\$\\{[^}]*}", "{param}");
        methodContext = methodContext.replaceAll("\\{[^}]*}", "{param}");

        // remove trailing "/", confusing for URI matching
        if(methodContext.endsWith("/")){
            methodContext = methodContext.substring(0,methodContext.length() - 1);
        }

        // remove arguments params, not needed for URI matching
        int indexOf = methodContext.indexOf("?");
        if(indexOf != -1){
            methodContext = methodContext.substring(0, indexOf);
        }

        int lastIndexOf = methodContext.lastIndexOf("{param");
        if(lastIndexOf > 0){
            String substring = methodContext.substring(lastIndexOf - 1, lastIndexOf);
            if(!substring.equals("/")){
                methodContext = methodContext.substring(0, lastIndexOf);
            }
        }

        return methodContext;
    }
}

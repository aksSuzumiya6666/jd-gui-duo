package org.jd.gui.util.parser.jdt;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.jd.core.v1.util.StringConstants;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.decompiler.ContainerLoader;

import com.heliosdecompiler.transformerapi.common.ClasspathUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public final class ASTParserFactory {

    private static final ASTParserFactory INSTANCE = new ASTParserFactory(false, false, false);
    private static final ASTParserFactory BINDING_INSTANCE = new ASTParserFactory(true, true, true);

    private ASTParserFactory(boolean resolveBindings, boolean bindingRecovery, boolean statementRecovery) {
        this.resolveBindings = resolveBindings;
        this.bindingRecovery = bindingRecovery;
        this.statementRecovery = statementRecovery;
    }

    public static ASTParserFactory getInstance() {
        return INSTANCE;
    }

    public static ASTParserFactory getInstanceWithBindings() {
        return BINDING_INSTANCE;
    }

    private final boolean resolveBindings;
    private final boolean bindingRecovery;
    private final boolean statementRecovery;

    public ASTParser newASTParser(Container.Entry entry) throws IOException {
        URI jarURI = entry.getContainer().getRoot().getParent().getUri();
        char[] source = ContainerLoader.loadEntry(entry, StandardCharsets.UTF_8);
        String unitName = entry.getPath();
        return newASTParser(source, unitName, jarURI);
    }

    public ASTParser newASTParser(char[] source, String unitName, URI jarURI) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source);
        parser.setResolveBindings(resolveBindings);
        parser.setBindingsRecovery(bindingRecovery);
        parser.setStatementsRecovery(statementRecovery);
        String[] classpathEntries = ClasspathUtil.createClasspathEntries(jarURI, Collections.emptyList());
        boolean includeRunningVMBootclasspath = true;
        if (unitName.endsWith(".java")) {
            String[] sourcepathEntries = { jarURI.getPath() };
            String[] encodings = { StandardCharsets.UTF_8.name() };
            parser.setEnvironment(classpathEntries, sourcepathEntries, encodings, includeRunningVMBootclasspath);
            parser.setUnitName(unitName);
        }
        if (unitName.endsWith(StringConstants.CLASS_FILE_SUFFIX)) {
            parser.setEnvironment(classpathEntries, null, null, includeRunningVMBootclasspath);
            parser.setUnitName(unitName.replace(StringConstants.CLASS_FILE_SUFFIX, ".java"));
        }

        Map<String, String> options = getDefaultOptions();
        options.put(JavaCore.COMPILER_PB_MAX_PER_UNIT, String.valueOf(Integer.MAX_VALUE));
        options.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, "warning");
        parser.setCompilerOptions(options);
        return parser;
    }

    private static Map<String, String> getDefaultOptions() {
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.CORE_ENCODING, StandardCharsets.UTF_8.name());
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion());
        return options;
    }
}

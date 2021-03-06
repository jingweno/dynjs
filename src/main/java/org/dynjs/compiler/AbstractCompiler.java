package org.dynjs.compiler;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import me.qmx.jitescript.JDKVersion;
import me.qmx.jitescript.JiteClass;

import org.dynjs.Config;
import org.dynjs.parser.Statement;
import org.dynjs.parser.ast.BlockStatement;
import org.dynjs.parser.ast.Expression;
import org.dynjs.parser.ast.ExpressionStatement;
import org.dynjs.parser.ast.StringLiteralExpression;
import org.dynjs.runtime.DynamicClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

public class AbstractCompiler {

    private final AtomicInteger counter = new AtomicInteger();
    private Config config;
    private String type;

    public AbstractCompiler(Config config, String type) {
        this.config = config;
        this.type = type;
    }

    public Config getConfig() {
        return this.config;
    }

    public int nextCounterValue() {
        return this.counter.getAndIncrement();
    }

    public String nextClassName() {
        return nextClassName("");
    }

    public String nextClassName(String grist) {
        return this.config.getBasePackage().replace('.', '/') + "/" + grist + type + nextCounterValue();
    }

    protected Class<?> defineClass(JiteClass jiteClass) {
        byte[] bytecode = jiteClass.toBytes(JDKVersion.V1_7);

        if (config.isDebug()) {
            ClassReader reader = new ClassReader(bytecode);
            CheckClassAdapter.verify(reader, true, new PrintWriter(System.out));
        }
        return new DynamicClassLoader(config.getClassLoader()).define(jiteClass.getClassName().replace('/', '.'), bytecode);
    }
    
    public boolean isStrict(Statement statement) {
        boolean isStrict = false;

        if (statement instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) statement;
            for (Statement each : block.getBlockContent()) {
                if (each instanceof ExpressionStatement) {
                    Expression expr = ((ExpressionStatement) each).getExpr();
                    if (expr instanceof StringLiteralExpression) {
                        if (((StringLiteralExpression) expr).getLiteral().equals("use strict")) {
                            isStrict = true;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return isStrict;
    }

}

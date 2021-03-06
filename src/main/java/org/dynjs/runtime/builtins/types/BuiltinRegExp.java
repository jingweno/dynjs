package org.dynjs.runtime.builtins.types;

import org.dynjs.exception.ThrowException;
import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.JSObject;
import org.dynjs.runtime.Types;
import org.dynjs.runtime.builtins.types.regexp.DynRegExp;
import org.dynjs.runtime.builtins.types.regexp.prototype.Exec;
import org.dynjs.runtime.builtins.types.regexp.prototype.Test;
import org.dynjs.runtime.builtins.types.regexp.prototype.ToString;

public class BuiltinRegExp extends AbstractBuiltinType {

    public BuiltinRegExp(final GlobalObject globalObject) {
        super(globalObject, "pattern", "flags");

        DynRegExp proto = new DynRegExp(globalObject, "", "");
        setPrototypeProperty(proto);
    }

    @Override
    public void initialize(GlobalObject globalObject, JSObject proto) {
        proto.setPrototype(globalObject.getPrototypeFor("Object"));
        defineNonEnumerableProperty(proto, "constructor", this);
        defineNonEnumerableProperty(proto, "exec", new Exec(globalObject));
        defineNonEnumerableProperty(proto, "test", new Test(globalObject));
        defineNonEnumerableProperty(proto, "toString", new ToString(globalObject));
    }

    @Override
    public Object call(ExecutionContext context, Object self, Object... args) {

        if (args[0] instanceof JSObject && ((JSObject) args[0]).getClassName().equals("RegExp")) {
            return args[0];
        }

        String pattern = "";
        if (args[0] != Types.UNDEFINED) {
            pattern = Types.toString(context, args[0]);
        }
        
        if (self == Types.UNDEFINED) {
            return newRegExp(context, pattern, Types.toString(context, args[1]));
        } else {
            String flags = null;

            if (args[1] != Types.UNDEFINED) {
                flags = Types.toString(context, args[1]);
            }

            ((DynRegExp) self).setPatternAndFlags(context, pattern, flags);

            return self;
        }
    }
    
    @Override
    public JSObject createNewObject(ExecutionContext context) {
        return new DynRegExp(context.getGlobalObject());
    }

    public static DynRegExp newRegExp(ExecutionContext context, String pattern, String flags) {
        BuiltinRegExp ctor = (BuiltinRegExp) context.getGlobalObject().get(context, "__Builtin_RegExp");
        return (DynRegExp) context.construct(ctor, pattern, flags);
    }

}
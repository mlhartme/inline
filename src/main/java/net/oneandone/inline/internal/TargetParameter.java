package net.oneandone.inline.internal;

import net.oneandone.inline.types.Repository;

import java.lang.reflect.Type;

public class TargetParameter extends Target {
    private final Object[] actuals;
    private final int idx;

    protected TargetParameter(Repository schema, Type type, Object[] actuals, int idx) {
        super(schema, type);
        this.actuals = actuals;
        this.idx = idx;
    }

    @Override
    public boolean before() {
        return true;
    }

    @Override
    public void doSet(Object dest, Object value) {
        actuals[idx] = value;
    }
}

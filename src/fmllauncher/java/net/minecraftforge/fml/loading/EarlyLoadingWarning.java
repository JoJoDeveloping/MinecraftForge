package net.minecraftforge.fml.loading;

import java.util.List;

public class EarlyLoadingWarning {

    private final String i18message;
    private final Object[] args;
    public EarlyLoadingWarning(final String message, Object... args) {
        this.i18message = message;
        this.args = args;
    }

    public String getI18message() {
        return i18message;
    }

    public Object[] getArgs() {
        return args;
    }

}

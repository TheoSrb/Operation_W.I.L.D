package net.tiew.operationWild.entity.client.render;

import java.lang.reflect.Method;

public final class OWShaderCompat {

    private static final String IRIS_API = "net.irisshaders.iris.api.v0.IrisApi";

    private static boolean resolved;
    private static Object irisApi;
    private static Method shaderPackInUse;

    private OWShaderCompat() {
    }

    public static boolean isShaderPackInUse() {
        if (!resolved) resolve();
        if (shaderPackInUse == null) return false;
        try {
            return (boolean) shaderPackInUse.invoke(irisApi);
        } catch (ReflectiveOperationException | RuntimeException e) {
            shaderPackInUse = null;
            return false;
        }
    }

    private static void resolve() {
        resolved = true;
        try {
            Class<?> api = Class.forName(IRIS_API);
            irisApi = api.getMethod("getInstance").invoke(null);
            shaderPackInUse = api.getMethod("isShaderPackInUse");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            irisApi = null;
            shaderPackInUse = null;
        }
    }
}

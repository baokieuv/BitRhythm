package com.bit_rhythm.gateway.error;

/** Walks exception cause chains; the proxy and servlet container wrap upstream failures. */
public final class Causes {

    private static final int MAX_DEPTH = 15;

    private Causes() {
    }

    public static boolean has(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /** Matches by class name so optional libraries (Resilience4j, Tomcat) need no hard dependency. */
    public static boolean hasClassNamed(Throwable throwable, String className) {
        Throwable current = throwable;
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            if (current.getClass().getName().equals(className)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

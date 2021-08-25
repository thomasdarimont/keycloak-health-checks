package com.github.thomasdarimont.keycloak.healthchecker.support;

public class ExceptionUtils {

    /**
     * Retrieve the innermost cause of the given exception, if any.
     *
     * @param original the original exception to introspect
     * @return the innermost exception, or {@code null} if none
     */
    public static Throwable getRootCause(Throwable original) {

        if (original == null) {
            return null;
        }
        Throwable rootCause = null;
        Throwable cause = original.getCause();
        while (cause != null && cause != rootCause) {
            rootCause = cause;
            cause = cause.getCause();
        }
        return rootCause;
    }

}
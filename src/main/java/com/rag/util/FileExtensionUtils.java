package com.rag.util;

/**
 * Utility methods for working with file names and extensions.
 */
public final class FileExtensionUtils {

    private FileExtensionUtils() {
    }

    /**
     * Extracts the lowercase file extension including the leading dot.
     *
     * @param filename file name to inspect
     * @return extension such as {@code .pdf}, or an empty string when absent
     */
    public static String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }
}

package fileLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileLogger {

    public static final String MERGEXPLAIN_LOG_FILE = "mergeXPlainLog.txt";
    public static final String MHS_LOG_FILE = "mhsLog.txt";
    public static final String MHS_PARTIAL_EXPLANATIONS_LOG_FILE = "mhsPartialExplanationsLog.txt";
    private static final String FILE_DIRECTORY = "logs";

    public static void appendToFile(String fileName, String log) {
        createFileIfNotExists(fileName);
        try {
            Files.write(Paths.get(getFilePath(fileName)), log.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void deleteLogs() {
        File logMergeXPlain = new File(getFilePath(MERGEXPLAIN_LOG_FILE));
        File logMHS = new File(getFilePath(MHS_LOG_FILE));
        File logPartialExplanationsMHS = new File(getFilePath(MHS_PARTIAL_EXPLANATIONS_LOG_FILE));
        logMergeXPlain.delete();
        logMHS.delete();
        logPartialExplanationsMHS.delete();
    }

    private static void createFileIfNotExists(String fileName) {
        File file = new File(getFilePath(fileName));
        try {
            file.createNewFile();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static String getFilePath(String fileName) {
        File directory = new File(FILE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return FILE_DIRECTORY.concat(File.separator).concat(fileName);
    }
}

package fileLogger;

import common.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileLogger {

    public static final String MERGEXPLAIN_LOG_FILE__PREFIX = "mergeXPlain";
    public static final String MHS_LOG_FILE__PREFIX = "mhs";
    public static final String MHS_PARTIAL_EXPLANATIONS_LOG_FILE__PREFIX = "mhsPartialExplanations";
    public static final String LOG_FILE__POSTFIX = ".log";
    private static final String FILE_DIRECTORY = "logs";

    public static void appendToFile(String fileName, long currentTimeMillis, String log) {
        createFileIfNotExists(fileName, currentTimeMillis);
        try {
            Files.write(Paths.get(getFilePath(fileName, currentTimeMillis)), log.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static void createFileIfNotExists(String fileName, long currentTimeMillis) {
        File file = new File(getFilePath(fileName, currentTimeMillis));
        try {
            file.createNewFile();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static String getFilePath(String fileName, long currentTimeMillis) {
        String[] inputFile = Configuration.INPUT_FILE.split(File.separator);
        String input = inputFile[inputFile.length - 1];
        String inputFileName = input;
        String[] inputFileParts = input.split("\\.");
        if (inputFileParts.length > 0) {
            inputFileName = inputFileParts[0];
        }

        String directoryPath = FILE_DIRECTORY.concat(File.separator).concat(Configuration.REASONER.name()).concat(File.separator).concat(inputFileName);
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String observation = Configuration.OBSERVATION.replaceAll("\\s+", "_");
        return directoryPath.concat(File.separator).concat("" + currentTimeMillis + "__").concat(observation + "__").concat(fileName).concat(LOG_FILE__POSTFIX);
    }
}

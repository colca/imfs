package imfs.api;

import imfs.filesystem.Directory;
import imfs.filesystem.FileModifyOptions;

/**
 * File interface for an in-memory file system.
 * This interface provides APIs for common file operations as move, delete, .etc.
 */
public interface File {
    public static String DELIMITER = "/";
    public static String SINGLE_LEVEL_PARENT = "..";
    public static String CURRENT_DIR_DOT = ".";

    /**
     * Get content from the file. The content could be string if file contains pure text content,
     * or serialized string if file content is non-string type.
     * @return string file content.
     */
    Object getFileContent();

    /**
     * Set name of the file
     * @param name new name of the file
     * @return true if setting successfully, otherwise false
     */
    boolean setName(String name);

    /**
     * Get parent {@link Directory} of the file
     * @return parent directory
     */
    Directory getParent();

    /**
     * Change parent {@link Directory} of the file
     * @return
     */
    boolean setParent(File parent);

    /**
     * Returns true if the file is type of {@link Directory}
     * @return true if file is directory, false otherwise.
     */
    boolean isDirectory();

    /**
     * Move the file to a new location.
     * @param des new destination to move file to
     * @return true if moving operation succeeds, otherwise false.
     */
    boolean move(String des, boolean createOnNonExist, FileModifyOptions modifyOption);

    /**
     * Delete this file and corresponding entry in its parent {@link Directory}.
     * @return true if deletion operation succeeds, otherwise false.
     */
    boolean delete();

    /**
     * Get name of the file
     * @return name of the file
     */
    String getName();

    /**
     * Get full path of the file starting from root directory.
     * @return string full path of the file
     */
    String getFullPath();

    /**
     * Print the file's short file name.
     */
    default void printName() {
        System.out.println(getName());
    }

    /**
     * Print the full path of the file.
     */
    default void printFullPath() {
        System.out.println(getFullPath());
    }
}

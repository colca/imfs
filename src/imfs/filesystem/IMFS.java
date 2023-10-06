package imfs.filesystem;

import com.google.common.annotations.VisibleForTesting;
import imfs.api.File;
import imfs.factories.SingletonRootDirectoryFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Class of In-Memory File System, it instantiates a default root {@link Directory}
 * and provides a set of file system operations similar to a Linux console commands.
 * TODO: build a factory initialising from a backup IMFS file
 */
public class IMFS {
    private Directory _root;

    private Directory _cwd;

    private PathTraverser _pathTraverser;

    public IMFS() {
        // universal root
        _root = SingletonRootDirectoryFactory.getInstance().getRoot();
        _cwd = _root;
        _pathTraverser = new PathTraverser(_root, _cwd);
    }

    /**
     * Print current working directory.
     */
    public void printCwd() {
        _cwd.printFullPath();
    }

    /**
     * Change current working directory to the given path.
     * @param des file to change cwd to
     * @param createOnNonExisting creation enforcing flag if path given doesn't exist
     * @return directory if changing successfully, otherwise null
     */
    public @Nullable Directory changeCwd(String des, boolean createOnNonExisting) {
        File found = traverseTo(des, createOnNonExisting);

        if (found == null) {
            System.err.println("Directory: " + des + " doesn't exist, please create the directory first or use creation enforce flag!");
            return null;
        } else if (!found.isDirectory()) {
            System.err.println("A single file with name " + des + " already exists, can not change cwd to this file!");
            return null;
        }
        _cwd = (Directory) found;
        _pathTraverser.setCwd(_cwd);
        return _cwd;
    }

    /**
     * Create a new sub file in current working directory, {@link ConcreteFile} or {@link Directory}.
     * @param newFileName name of the new file
     * @param isDirectory whether file should be created as {@link Directory}
     * @return the created file
     */
    public File createNewFile(String newFileName, boolean isDirectory) {
        return _cwd.createNewSubFile(newFileName, isDirectory);
    }

    /**
     * List all {@link File}s under current working directory.
     * @param displayOption listing info options like nameOnly/fullPath/
     */
    public void ls(FilePrintOptions displayOption) {
        Collection<File> allFiles = (Collection<File>) _cwd.getFileContent();
        for(File file : allFiles) {
            switch (displayOption) {
                case FilePrintOptions.NameOnly:
                    file.printName();
                    break;
                case FilePrintOptions.FullPath:
                    file.printFullPath();
                    break;
                case FilePrintOptions.FullMetaData:
                    // TODO: change to full metadata print
                    file.printFullPath();
            }
        }
    }

    /**
     * Delete the given {@link File}
     * @return true if deletion succeeds, otherwise false
     */
    public boolean delete(String fileToDelete) {
        File found = traverseTo(fileToDelete, false);
        if (found == null) {
            System.err.println("File " + fileToDelete + " doesn't exist, could not delete!");
            return false;
        }
        return found.delete();
    }

    /**
     * Write content to the given {@link File}, supports writing to {@link ConcreteFile} only.
     * TODO: enable file creation flag
     * TODO: enable buffer type content writing
     */
    public boolean writeFileContent(String fileToWrite, String content) {
        File found = traverseTo(fileToWrite, false);
        if (found == null) {
            System.err.println("File " + fileToWrite + " doesn't exist, please create file first");
            return false;
        }
        if (found.isDirectory()) {
            System.err.println("File " + fileToWrite + " is a directory and doesn't support content writing");
            return false;
        }
        ((ConcreteFile)found).writeFileContent(content);
        return true;
    }

    /**
     * Move a {@link ConcreteFile} to new destination.
     * TODO: support directory move
     * TODO: support same name file modification options
     * @param ori original file to move
     * @param des destination file to move to
     * @return true if file is moved successfully, otherwise false
     */
    public boolean moveFile(String ori, String des, boolean createOnNotExist, FileModifyOptions modifyOption) {
        File oriFile = traverseTo(ori, false);
        if (oriFile == null) {
            System.err.println("File " + ori + " doesn't exist, could not move!");
            return false;
        }
        if (oriFile.isDirectory()) {
            System.err.println("Directory " + ori + " move is not supported yet!");
            return false;
        }
        oriFile.move(des, createOnNotExist, modifyOption);
        return true;
    }

    /**
     * Find all {@link File}s matching the given name exactly, can be recursively.
     * @param name file name to match
     * @param recursive recursive flag
     * @return list of matching files
     */
    public List<File> findExactMatch(String name, boolean recursive) {
        List<File> res = new ArrayList<>();
        _cwd.findExactMath(name, res, recursive);
        res.stream().forEach(e -> e.printFullPath());
        return res;
    }

    /**
     * Get current working directory
     */
    public Directory getCwd() {
        return _cwd;
    }

    /**
     * Print the content of given {@link File}.
     * @param file path of the file
     */
    public void printFileContent(String file) {
        File found = traverseTo(file, false);
        if (found == null) {
            System.err.println("File " + file + " doesn't exist, nothing to display!");
            return;
        }
        if (found.isDirectory()) {
            Directory previousCwd = _cwd;
            changeCwd(file, false);
            ls(FilePrintOptions.FullPath);
            _cwd = previousCwd;
        }
        System.out.println();
        System.out.println(found.getFileContent());
    }

    /**
     * Helper to traverse to a target {@link File}.
     * it doesn't change current working directory and doesn't perform error/null check on result.
     * @param des file to traverse to
     * @param createOnNonExisting creation enforcing flag
     * @return file being traversed to, null if no such file is found when creation is not enforced
     */
    @VisibleForTesting
    public @Nullable File traverseTo(String des, boolean createOnNonExisting) {
        Directory cwdCurrent = _cwd;
        File found;
        if (!des.contains(File.DELIMITER)) {
            found = _pathTraverser.traverseOneLevel(des, createOnNonExisting);
        } else {
            found = _pathTraverser.traverseToAnyLevel(des, createOnNonExisting);
        }
        _cwd = cwdCurrent;
        _pathTraverser.setCwd(_cwd);
        return found;
    }
}

package imfs.demo;

import imfs.api.File;
import imfs.filesystem.FileModifyOptions;
import imfs.filesystem.FilePrintOptions;
import imfs.filesystem.IMFS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;


/**
 * A demo commandline app utilizing {@link IMFS} to provide an interactive
 * in-memory file system for user to play with.
 * This demo uses Spring Shell to build a comprehensive list of commands,
 * mimicking Linux's console commands including ls/rm/mkdir/cd/touch/move/find/cat/pwd
 *
 * Most commands here are self-explain and java doc are omitted
 * TODO: enable error code to replace true/false as operation results
 */
@SpringBootApplication
@ShellComponent
public class Main {
    public static IMFS _imfs;
    public static void main(String[] args)
    {
        _imfs = new IMFS();
        SpringApplication.run(Main.class, args);
    }

    @ShellMethod
    public static void ls(@ShellOption(value = {"-l"}) boolean printFullPath) {
        System.out.println();
        FilePrintOptions option = FilePrintOptions.NameOnly;
        if (printFullPath) {
            option = FilePrintOptions.FullPath;
        }
        _imfs.ls(option);
    }

    @ShellMethod
    public File mkdir(String name) {
       File dir = _imfs.createNewFile(name, true);
       return dir;
    }

    @ShellMethod
    public File touch(String name) {
        File newFile = _imfs.createNewFile(name, false);
        return newFile;
    }

    /**
     * {@link #move(String, String, boolean)} supports full path traversal.
     * TODO: supply replace/keepPrevious/rename option
     * @param ori original path
     * @param des destination path
     * @param createOnNotExist enforcing creation on not existing path
     * @return true if a move operation is performed successfully, otherwise false
     */
    @ShellMethod
    public boolean mv(String ori, String des,  @ShellOption(value = {"--force"})boolean createOnNotExist) {
        return _imfs.moveFile(ori, des, createOnNotExist, FileModifyOptions.Replace);
    }

    /**
     * {@link #rm(String)} supports full path traversal.
     * @param name file name to delete
     * @return true if a deletion operation is performed successfully, otherwise false
     */
    @ShellMethod
    public boolean rm(String name) {
        return _imfs.delete(name);
    }

    @ShellMethod
    public void pwd() {
        System.out.println();
        _imfs.printCwd();
    }

    /**
     * {@link #cd(String, boolean)} supports full or partial path traversal.
     * @param dir directory to change current working directory to
     * @param createOnNotExist creation enforce flag if path given isn't existing
     * @return true if cwd is changed successfully, otherwise false
     */
    @ShellMethod
    public boolean cd(String dir, @ShellOption(value = {"--force"})boolean createOnNotExist) {
        File newCwd =  _imfs.changeCwd(dir, createOnNotExist);
        return newCwd != null;
    }

    @ShellMethod
    public void find(String match, @ShellOption(value = {"-r"})boolean recursive) {
        _imfs.findExactMatch(match, recursive);
    }

    /**
     * {@link #write(String, String)} supports full or partial path file traversal and content writing.
     * TODO: support file writing modes append/replace
     * @param fileToWrite target file
     * @param appendingContent file content to append
     * @return
     */
    @ShellMethod
    public boolean write(String fileToWrite, String appendingContent) {
        return _imfs.writeFileContent(fileToWrite, appendingContent);
    }

    /**
     * {@link #cat(String)} supports full or partial path file traversal and content writing.
     * @param fileToWrite target file
     * @return
     */
    @ShellMethod
    public void cat(String fileToWrite) {
        _imfs.printFileContent(fileToWrite);
    }
}

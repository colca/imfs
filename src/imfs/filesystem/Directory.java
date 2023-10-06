package imfs.filesystem;

import com.google.common.base.Preconditions;
import imfs.api.File;
import imfs.utils.FileNamePredicate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Directory class representing a directory/folder in the in-memory file system.
 */
public class Directory implements File {
    public static final String ROOT_PATH = "/";

    private Directory _parent;
    private String _name;
    private Map<String, File> _nameToSubFile;
    private boolean isRoot = false;

    public Directory() {
        isRoot = true;
        _name = "";
        _parent = NoOpDirectory.getInstance();
        _nameToSubFile = new ConcurrentHashMap<>();
    }

    public Directory(String name, Directory des) {
        _name = name;
        _parent = des;
        _nameToSubFile = new ConcurrentHashMap<>();
    }

    /**
     * Create a new sub file of {@link ConcreteFile} or {@link Directory}.
     * @param fileToCreate file name
     * @param isDirectory  type of file
     * @return file if created successfully
     */
    public synchronized File createNewSubFile(String fileToCreate, boolean isDirectory) {
        Preconditions.checkArgument(FileNamePredicate.getInstance().test(fileToCreate),
                String.format("sub file or directory name %s provided is invalid", fileToCreate));
        Preconditions.checkState(!_nameToSubFile.containsKey(fileToCreate),
                String.format("A subdirectory or file %s already exists.", fileToCreate));
        File newSub;
        if(isDirectory) {
            newSub = new Directory(fileToCreate, this);
        } else {
            newSub = new ConcreteFile(fileToCreate, this);
        }
        _nameToSubFile.put(fileToCreate, newSub);
        return newSub;
    }

    /**
     * Delete a sub file from current {@link Directory}, performs a soft delete
     * clearing the sub file from internal map if file is dead already.
     * @param fileToDelete sub file name to delete
     * @param isDeadFile   whether sub file is already deleted, e.g. moved
     * @return true if a deletion is executed successfully, otherwise false
     */
    public boolean deleteSubFile(String fileToDelete, boolean isDeadFile) {
        File entry = _nameToSubFile.getOrDefault(fileToDelete, null);
        if (entry == null) {
            return false;
        }

        if (!isDeadFile) {
            entry.delete();
        }
        _nameToSubFile.remove(fileToDelete);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFileContent() {
        return _nameToSubFile.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setName(String name) {
        if (!FileNamePredicate.getInstance().test(name)) {
            return false;
        }
        _name = name;
        return true;    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory getParent() {
        return _parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullPath() {
        if (isRoot) {
            return ROOT_PATH;
        }
        return _parent.getFullPath().concat(_name).concat(DELIMITER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return true;
    }

    //TODO: complete directory deletion
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean move(String des, boolean createOnNonExist, FileModifyOptions modifyOption) {
        //No
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean delete() {
        if (isRoot) {
            System.err.println("Can not delete Root directory " + getFullPath());
            return false;
        }
        for (Map.Entry<String, File> entry : _nameToSubFile.entrySet()) {
            File sub = entry.getValue();
            sub.delete();
        }
        _nameToSubFile.clear();
        _nameToSubFile = null;
        _parent.getNameToSubFile().remove(_name);
        _name = null;
        _parent = null;
        return true;
    }

    /**
     * Move a target file to this {@link Directory}.
     * If same name file already exists, resolve according to the given {@link FileModifyOptions}
     * @param file file to move here
     * @param modifyOption resolve option of a file name collision
     * @return true if file is created successfully, otherwise false
     */
    public synchronized boolean moveFileToDirectory(File file, FileModifyOptions modifyOption) {
        if (_nameToSubFile.containsKey(file.getName())) {
            //TODO: add test cases for various modify options
            switch (modifyOption) {
                case Replace:
                    File existing = _nameToSubFile.get(file.getName());
                    if (existing.isDirectory() && !file.isDirectory()) {
                        System.err.println("Directory " + existing.getFullPath() + " already exists as a folder, can not be replaced by a file!");
                        return false;
                    }
                    System.out.println("File " + file.getName() + " already exists, replacing it now!");
                    break;
                case Abort:
                    System.out.println("File " + file.getName() + " already exists, aborting action!");
                    return false;
                case KeepPrevious:
                    //TODO: this would make the file inaccessible but leave a lingering file pointer
                    System.out.println("File " + file.getName() + " already exists, keeping existing version!");
                    return true;
                default:
                    System.err.println("File " + file.getName() + " already exists, please use replace/rename/keepPrevious options!");
            }
        }
        file.setParent(this);
        _nameToSubFile.put(file.getName(), file);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * Find sub {@link File}s in this {@link Directory} which exactly match the given name
     * @param name file name
     * @param res  provided result list
     * @param recursive flag for recursive find
     */
    public void findExactMath(String name, List<File> res, boolean recursive) {
        for (Map.Entry<String, File> sub : _nameToSubFile.entrySet()) {
            File file = sub.getValue();
            if (name.equals(sub.getKey())) {
                res.add(file);
            }
            if (recursive && file.isDirectory()) {
                Directory dir = (Directory) file;
                dir.findExactMath(name, res, recursive);
            }
        }
    }

    public Map<String, File> getNameToSubFile() {
        return _nameToSubFile;
    }

    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public boolean setParent(File parent) {
        if (parent == null || !parent.isDirectory()) {
            return false;
        }
        _parent = (Directory) parent;
        return true;
    }

    /**
     * Returns true if this directory contains the given sub file
     */
    public boolean hasFile(String subFileName) {
        return _nameToSubFile.containsKey(subFileName);
    }
}

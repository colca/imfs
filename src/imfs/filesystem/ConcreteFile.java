package imfs.filesystem;

import com.google.common.base.Preconditions;
import imfs.utils.FileNamePredicate;
import imfs.api.File;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;


/**
 * A concrete {@link File} class representing single file in the in-memory file system.
 */
public class ConcreteFile implements File {

    private String _name;
    private StringBuilder _content;
    private Directory _parent;

    public ConcreteFile(String name, Directory des) {
        Preconditions.checkState(FileNamePredicate.getInstance().test(name), "file name provided is invalid");

        _name = name;
        _parent = des;
        _parent.getNameToSubFile().put(_name, this);
        //TODO: replace with thread-safe StringBuffer
        _content = new StringBuilder();
    }

    /**
     * TODO: support serialized content for various file types
     *
     * @param newContent
     */
    public synchronized void writeFileContent(String newContent) {
        if (StringUtils.isEmpty(newContent)) {
            System.err.println("Content passed in is empty!");
            return;
        }
        _content.append(newContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFileContent() {
        return _content == null ? null : _content.toString();
    }

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
    public boolean isDirectory() {
        return false;
    }

    /**
     * Helper function to facilitate file moving by traversing to target directory of new moving location.
     * @param targetLocation full path of moving destination
     * @param createOnNonExist creation enforcing flag for not existing path
     * @return directory to move the file into
     */
    private @Nullable File getFolderToMoveTo(String targetLocation, boolean createOnNonExist) {
        PathTraverser traverser = new PathTraverser(findRootFromParent(), _parent);
        File folderToMoveTo;
        if (targetLocation.contains(File.DELIMITER)) {
            if (targetLocation.startsWith(File.DELIMITER)) {
                traverser.traverseToRoot();
            }
            String dirStr = targetLocation.substring(0, targetLocation.lastIndexOf(File.DELIMITER));
            if (StringUtils.isEmpty(dirStr)) {
                //moving to root
                folderToMoveTo = traverser.getCwd();
            } else {
                folderToMoveTo = traverser.traverseToAnyLevel(dirStr, createOnNonExist);
            }
            Preconditions.checkState(folderToMoveTo != null, String.format("File %s doesn't exist!", dirStr));
            // if concrete file under same name is found before last level
            Preconditions.checkState(folderToMoveTo.isDirectory(),
                    String.format("File %s already exists but it's not a directory!", dirStr));
        } else {
            // same directory move
            folderToMoveTo = _parent;
        }
        return folderToMoveTo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean move(String location, boolean createOnNonExist, FileModifyOptions modifyOption) {
        File folderToMoveTo = getFolderToMoveTo(location, createOnNonExist);

        if (folderToMoveTo == null) {
            System.err.println("Could not move to target directory!");
            return false;
        }
        Directory dir = (Directory) folderToMoveTo;
        String newFileName = location.contains(File.DELIMITER) ?
                location.substring(location.lastIndexOf(File.DELIMITER) + 1) :
                location;

        Preconditions.checkState(FileNamePredicate.getInstance().test(newFileName),
                String.format("file name \"%s\" provided is invalid", newFileName));

        //reserve name in case move op fails
        String oldName = _name;
        Directory oldParent = _parent;
        _name = newFileName;
        if (dir.moveFileToDirectory(this, modifyOption)) {
            oldParent.deleteSubFile(oldName, true);
            return true;
        }
        _name = oldName;
        return false;
    }

    //TODO: deletion failure cases handling
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean delete() {
        _parent.getNameToSubFile().remove(_name);
        _name = null;
        _parent = null;
        _content.setLength(0);
        _content = null;
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullPath() {
        return _name == null ? null : _parent.getFullPath().concat(_name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean setName(String name) {
        if (!FileNamePredicate.getInstance().test(name)) {
            return false;
        }
        _name = name;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setParent(File parent) {
        if (parent == null || !parent.isDirectory()) {
            return false;
        }
        _parent = (Directory) parent;
        return true;
    }

    /**
     * Find root by parent {@link Directory}.
     * @return root directory
     */
    private Directory findRootFromParent() {
        Directory root = _parent;
        while (!root.isRoot()) {
            root = root.getParent();
        }
        return root;
    }

    /**
     * Generate a new random name containing current name.
     */
    public String genNewName() {
        return _name + RandomStringUtils.random(100);
    }
}

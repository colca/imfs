package imfs.filesystem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import imfs.api.File;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A helper class to traverse to target {@link File} at different levels in an in-memory files system.
 */
public class PathTraverser {
    private Directory _root;

    private Directory _cwd;

    public PathTraverser(Directory root, Directory cwd) {
        _root = root;
        _cwd = cwd;
    }

    /**
     * Traverse a single level up or down from current working directory.
     * This traverse will change this {@link PathTraverser}'s current working folder
     * if target is found and also a valid directory. 
     * @param target target file or directory name, this will be the exact match
     * @return {@link File} if found the target with exact name, otherwise null.
     */
    public File traverseOneLevel(String target, boolean createOnNotExist) {
        String[] subs = new String[] {target};
        File found = locateChildren(subs, createOnNotExist, 0, _cwd);
        // change cwd to located dir, no-op is a concrete file is found
        if (found != null && found.isDirectory()) {
            _cwd = (Directory) found;
        }
        return found;
    }

    /**
     * Traverse to any nested level up and down from current working director,
     * or starting from root if the given path starts from the root {@link Directory}.
     * Note that if a concrete file is being traversed to, the current working directory
     * doesn't change but only the {@link ConcreteFile} is returned.
     * Also, if the createOnNotExist flag is set to true, any non-existing file in the
     * given path will be created as {@link Directory} including the last element in the path.
     * e.g. /foo/bar/foobar will create these directories:
     * /foo/
     * /foo/bar/
     * /foo/bar/foobar/
     * @param path a string path, can be partial from current working directory, or a full path starting from root "/"
     * @param createOnNotExist enforce directory creation if any element in the path is not existing
     * @return the target {@link File} to traverse to, this could be null if creationOnExist is not enforced
     */
    public @Nullable File traverseToAnyLevel(String path, boolean createOnNotExist) {
        if (StringUtils.isEmpty(path)) {
            return _cwd;
        }
        if (path.equals(Directory.ROOT_PATH)) {
            _cwd = _root;
            return _cwd;
        }

        List<String> subs = Arrays.asList(path.split(File.DELIMITER)).stream()
                .filter(e -> !StringUtils.isEmpty(e))
                .collect(Collectors.toList());
        String[] subArray = new String[subs.size()];
        subs.toArray(subArray);
        File found = path.startsWith(File.DELIMITER) ? locateChildren(subArray, createOnNotExist, 0, _root) :
                locateChildren(subArray, createOnNotExist, 0, _cwd);
        // change cwd to located dir, no-op is a concrete file is found
        if (found != null && found.isDirectory()) {
            _cwd = (Directory) found;
        }
        return found;
    }

    /**
     * Helper function to facilitate children traversal.
     * @param subs all sub {@link File}s on the traversal path
     * @param createOnNotExist enforcing flag to force creation of non-existing {@link File},
     *                         note that any non-existing {@link  File} will be created as {@link Directory},
     *                         instead of {@link ConcreteFile} to accommodate various file types
     * @param idx  index of the current {@link File} being traversed to
     * @param curFolder current {@link Directory} to look for the above {@link File} being traversed to
     * @return the found {@link File}, could be null if createOnNotExist option is not enforced
     */
    private @Nullable File locateChildren(String[] subs, boolean createOnNotExist, int idx, Directory curFolder) {
        Preconditions.checkArgument(idx < subs.length);
        String curSub = subs[idx];
        switch (curSub) {
            case File.CURRENT_DIR_DOT:
                if (idx == subs.length - 1) {
                    return curFolder;
                }
                return locateChildren(subs, createOnNotExist, idx + 1, curFolder);
            case File.SINGLE_LEVEL_PARENT:
                Directory parent = curFolder.isRoot()? curFolder : curFolder.getParent();
                if (idx == subs.length - 1) {
                    return parent;
                }
                return locateChildren(subs, createOnNotExist, idx + 1, parent);
            default:
                File found = curFolder.getNameToSubFile().getOrDefault(curSub, null);
                if (found == null) {
                    found = createOnNotExist ? curFolder.createNewSubFile(curSub, true) : null;
                }
                if (idx == subs.length - 1) {
                    return found;
                }
                if (found != null) {
                    // if concrete file under same name is found before last level
                    Preconditions.checkState(found.isDirectory(),
                            String.format("File with same name %s already exists but not a directory!", curSub));
                }

                return found == null ? null : locateChildren(subs, createOnNotExist, idx + 1, (Directory) found);
        }
    }

    /**
     * Use this {@link PathTraverser} to traverse to the root {@link Directory}
     * @return the root {@link Directory}
     */
    public Directory traverseToRoot() {
        Preconditions.checkNotNull(_cwd);
        while (!_cwd.isRoot()) {
            _cwd = _cwd.getParent();
        }
        return _cwd;
    }

    @VisibleForTesting
    public Directory getCwd() {
        return _cwd;
    }

    public void setCwd(Directory _cwd) {
        this._cwd = _cwd;
    }

}

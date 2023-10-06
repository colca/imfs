package imfs.filesystem;

/**
 * A no-op {@link Directory} working as a singleton instance.
 * Currently, the root {@link Directory} uses this {@link NoOpDirectory} as its virtual parent.
 */
public class NoOpDirectory extends Directory {
    private static final NoOpDirectory _instance;
    //private static NoOpDirectory _instance;
    private NoOpDirectory() {}

    static {
        try {
            _instance = new NoOpDirectory();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating singleton instance");
        }
    }
    public static synchronized NoOpDirectory getInstance() {
        return _instance;
    }

    @Override
    public String getName() {
        return "";
    }
    @Override
    public String getFullPath() {
        return "";
    }

    @Override
    public Object getFileContent() {
        return null;
    }

    @Override
    public Directory getParent() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean move(String des, boolean createOnNonExist, FileModifyOptions option) {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }
}

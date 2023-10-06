package imfs.factories;

import imfs.filesystem.Directory;


/**
 * Singleton Factory to provide a unified root {@link Directory}
 * to use across the In-Memory File System.
 */
public class SingletonRootDirectoryFactory {
    private static final SingletonRootDirectoryFactory ROOT_SINGLETON_FACTORY = new SingletonRootDirectoryFactory();
    private final Directory _root;
    private SingletonRootDirectoryFactory() {
        _root = new Directory();
    }

    public static SingletonRootDirectoryFactory getInstance() {
        return ROOT_SINGLETON_FACTORY;
    }

    public Directory getRoot() {
        return _root;
    }
}

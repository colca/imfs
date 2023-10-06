package imfs.filesystem;


/**
 * Enum for print variations of file name/paths
 */
public enum FilePrintOptions {
    NameOnly("name"),
    FullPath("full"),
    FullMetaData("all");

    private final String _name;

    FilePrintOptions(String name) {
        _name = name;
    }
}

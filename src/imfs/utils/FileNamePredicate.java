package imfs.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

import static imfs.api.File.CURRENT_DIR_DOT;
import static imfs.api.File.SINGLE_LEVEL_PARENT;

public class FileNamePredicate implements Predicate<String> {
    private static final FileNamePredicate VALID_FILE_NAME = new FileNamePredicate();

    public static FileNamePredicate getInstance() {
        return VALID_FILE_NAME;
    }

    @Override
    public boolean test(String s) {
        if (StringUtils.isEmpty(s)) {
            System.err.println("file name can't be empty!");
            return false;
        }
        if (s.equals(SINGLE_LEVEL_PARENT) || s.equals(CURRENT_DIR_DOT) || s.contains("/")) {
            System.err.println("file name can't contain system variables of \"..\" or \".\" or \"/\"");
            return false;
        }
        return true;
    }
}

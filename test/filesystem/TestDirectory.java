package filesystem;

import imfs.api.File;
import imfs.filesystem.Directory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestDirectory {
    private Directory _root = new Directory();

    @DataProvider
    public Object[][] dirNames() {
        //dirName, expectedDirFullPath
        return new String[][] {
                new String[] {"a", "/a/"},
                new String[] {"b", "/b/"},
                new String[] {"c", "/c/"}
        };
    }

    @DataProvider
    public Object[][] nestedDir() {
        //dirName, expectedDirFullPath
        return new String[][] {
                new String[] {"aFolder", "bFolder", "cFolder"},
        };
    }

    @Test(dataProvider = "dirNames")
    public void testDirectoryCreate(String name, String expected) {
        Directory dir = new Directory(name, _root);
        Assert.assertTrue(dir.isDirectory());
        Assert.assertEquals(dir.getFullPath(), expected);
    }

    @Test
    public void testNestedDirectoryCreate() {
        Directory sub = new Directory("sub1", _root);
        Assert.assertEquals(sub.getFullPath(), "/sub1/");

        sub = (Directory) sub.createNewSubFile("sub2", true);
        Assert.assertEquals(sub.getFullPath(), "/sub1/sub2/");
    }

    @Test
    public void testEmptyDirectoryDelete() {
        Directory dir = new Directory("aFolder", _root);
        Assert.assertTrue(dir.isDirectory());
        Directory parent = dir.getParent();

        dir.delete();
        Assert.assertNull(dir.getName());
        Assert.assertNull(dir.getParent());
        Assert.assertNull(dir.getNameToSubFile());
        Assert.assertFalse(parent.hasFile("aFolder"));
    }

    @Test
    public void testNestedDirectoryDelete() {
        String[] nestedDirs = new String[] {"aFolder", "bFolder", "cFolder"};
        File[] allLevelFiles = new File[nestedDirs.length + 1];

        Directory dir = new Directory(nestedDirs[0], _root);
        Directory parent = dir.getParent();
        allLevelFiles[0] = dir;

        for (int i = 1; i < nestedDirs.length; i++) {
            dir = (Directory) dir.createNewSubFile(nestedDirs[i], true);
            allLevelFiles[i] = dir;
        }
        allLevelFiles[allLevelFiles.length - 1] = dir.createNewSubFile("aFile", false);

        File topDir = allLevelFiles[0];
        topDir.delete();

        for (int i = 0; i < allLevelFiles.length; i++) {
            Assert.assertNull(allLevelFiles[i].getName());
        }

        Assert.assertFalse(parent.hasFile("aFolder"));
    }

    @Test
    public void testDeleteRoot() {
        boolean res = _root.delete();
        Assert.assertFalse(res);

        Assert.assertEquals(_root.getFullPath(), Directory.ROOT_PATH);
        Assert.assertEquals(_root.getNameToSubFile().size(), 0);
    }
    //TODO: add find test cases
}

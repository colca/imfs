package filesystem;

import imfs.api.File;
import imfs.filesystem.*;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestIMFS {
    private IMFS _imfs;
    private Directory _root;
    private PathTraverser _pathTraverser;

    @BeforeTest
    public void init() {
        _imfs = new IMFS();
        _root = _imfs.getCwd();
        _imfs.createNewFile("aFolder", true);
        _imfs.createNewFile("bFolder", true);
        _imfs.createNewFile("aFile", false);

        // /aFolder/subFolder1/subFolder2/subFolder3
        _imfs.changeCwd("aFolder", true);
        _imfs.changeCwd("subLevel1", true);
        _imfs.changeCwd("subLevel2", true);
        _imfs.changeCwd("..", false);
        _imfs.createNewFile("foo", false);
        _imfs.changeCwd("subLevel2", false);
        _imfs.changeCwd("subLevel3", true);
    }

    @DataProvider
    public Object[][] cwdNoCreate() {
        //dirName, expectedDirFullPath
        return new String[][] {
                new String[] {"/", "/", "/"},
                new String[] {"/aFolder/subFolder1/subFolder2/subFolder3/..", null, "/"},
                new String[] {"/aFolder/subLevel1/subLevel2/subLevel3/..", "/aFolder/subLevel1/subLevel2/", "/aFolder/subLevel1/subLevel2/"},
        };
    }

    @Test (dataProvider = "cwdNoCreate")
    public void testChangeCwdNoCreate(String newCwd, String newCwdDirPath, String expectedCwdAfterChange) {
        Directory newCwdDir = _imfs.changeCwd(newCwd, false);
        Directory dir = _imfs.getCwd();
        if (StringUtils.isEmpty(newCwdDirPath)) {
            Assert.assertNull(newCwdDir, "change cwd returns null if target directory is not found");
        }
        Assert.assertEquals(dir.getFullPath(), expectedCwdAfterChange);
    }

    @DataProvider
    public Object[][] cwdCreate() {
        //dirName, expectedDirFullPath
        return new String[][] {
                new String[] {"/", "/"},
                new String[] {"/aFolder/subFolder1/subFolder4/subFolder3/..", "/aFolder/subFolder1/subFolder4/"},
                new String[] {"/aFolder/subLevel1/subLevel2/subLevel3/subLevel4", "/aFolder/subLevel1/subLevel2/subLevel3/subLevel4/"},
        };
    }

    @Test (dataProvider = "cwdCreate")
    public void testChangeCwdWithCreate(String newCwd, String expectedCwdAfterChange) {
        Directory newCwdDir = _imfs.changeCwd(newCwd, true);
        Directory dir = _imfs.getCwd();

        Assert.assertEquals(dir.getFullPath(), expectedCwdAfterChange);
    }

    @Test
    public void testDelete() {
        Directory dir = _imfs.changeCwd("/a/b/c", true);
        boolean res = _imfs.delete("/a/b/c/NotExisting");
        Assert.assertFalse(res);

        dir = _imfs.changeCwd("/a/b/c/d", true);
        File nestedFile = _imfs.createNewFile("aFile", false);

        dir = _imfs.changeCwd("/a/b/", false);
        Directory sub = (Directory) _imfs.traverseTo("/a/b/c", false);
        res = _imfs.delete("c");
        Assert.assertTrue(res);
        Assert.assertFalse(dir.hasFile("c"));
        Assert.assertNull(sub.getNameToSubFile());
        Assert.assertNull(nestedFile.getName());
        Assert.assertNull(nestedFile.getFileContent());
        Assert.assertEquals(dir.getNameToSubFile().size(), 0);

        dir = dir.getParent();
        res = _imfs.delete("/a/b");
        Assert.assertFalse(dir.hasFile("b"));
        Assert.assertEquals(dir.getNameToSubFile().size(), 0);
    }

    @Test
    public void testMove() {
        Directory dir = _imfs.changeCwd("/a/b/c", true);
        ConcreteFile file = (ConcreteFile) _imfs.createNewFile("newFile", false);
        file.writeFileContent("hello world");

        // move to diff directory
        boolean res = _imfs.moveFile("/a/b/c/newFile", "../newFile", false, FileModifyOptions.Replace);
        Assert.assertTrue(res);
        file = (ConcreteFile) _imfs.traverseTo("/a/b/newFile", false);
        Assert.assertEquals(file.getFileContent(), "hello world");
        File findPrevious = _imfs.traverseTo("/a/b/c/newFile", false);
        Assert.assertNull(findPrevious);

        // move to diff directory new name
        res = _imfs.moveFile("/a/b/newFile", "/a/b/c/newFileRename", false, FileModifyOptions.Replace);
        Assert.assertTrue(res);
        file = (ConcreteFile) _imfs.traverseTo("/a/b/c/newFileRename", false);
        Assert.assertEquals(file.getFileContent(), "hello world");
        findPrevious = _imfs.traverseTo("/a/b/newFile", false);
        Assert.assertNull(findPrevious);

        // move to diff directory existing file of same name
        dir = _imfs.changeCwd("/a/b/dd/", true);
        ConcreteFile sameNameFile = (ConcreteFile) _imfs.createNewFile("newFileRename", false);
        sameNameFile.writeFileContent("random content");
        res = _imfs.moveFile("/a/b/c/newFileRename", "../dd/newFileRename", false, FileModifyOptions.Replace);
        Assert.assertTrue(res);
        file = (ConcreteFile) _imfs.traverseTo("/a/b/dd/newFileRename", false);
        Assert.assertEquals(file.getFileContent(), "hello world");
        findPrevious = _imfs.traverseTo("/a/b/c/newFileRename", false);
        Assert.assertNull(findPrevious);

        // move to same directory
        dir = _imfs.changeCwd("/a/b/dd", false);
        res = _imfs.moveFile("/a/b/dd/newFileRename", "./newFile", false, FileModifyOptions.Replace);
        Assert.assertTrue(res);
        file = (ConcreteFile) _imfs.traverseTo("/a/b/dd/newFile", false);
        Assert.assertEquals(file.getFileContent(), "hello world");
        findPrevious = _imfs.traverseTo("/a/b/dd/newFileRename", false);
        Assert.assertNull(findPrevious);
    }
    //TODO: add IMFS level tests
}

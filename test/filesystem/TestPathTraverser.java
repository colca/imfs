package filesystem;

import imfs.api.File;
import imfs.filesystem.Directory;
import imfs.filesystem.IMFS;
import imfs.filesystem.PathTraverser;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestPathTraverser {
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

        // /aFolder/subFolder1/subFolder2
        _imfs.changeCwd("aFolder", true);
        _imfs.changeCwd("subLevel1", true);
        _imfs.changeCwd("subLevel2", true);
        _imfs.changeCwd("..", false);
        _imfs.createNewFile("foo", false);
        _imfs.changeCwd("subLevel2", false);
        _imfs.changeCwd("subLevel3", true);
    }

    @DataProvider
    public Object[][] multiLevelPaths() {
        return new String[][] {
                //fileToTraverseTo, fullPathForTargetFile, CwdPathAfterTraverse
                new String[] {"/", "/", "/"},
                new String[] {"/..", "/", "/"},
                new String[] {"aFolder", "/aFolder/", "/aFolder/"},
                new String[] {"/aFolder", "/aFolder/", "/aFolder/"},
                new String[] {"bFolder", "/bFolder/", "/bFolder/"},
                //traversing to concrete file doesn't change cwd
                new String[] {"aFile", "/aFile", "/"},
                new String[] {"aFolder/subLevel1/foo", "/aFolder/subLevel1/foo", "/"},
                new String[] {"aFolder/subLevel1", "/aFolder/subLevel1/", "/aFolder/subLevel1/"},
                new String[] {"/aFolder/subLevel1/subLevel2", "/aFolder/subLevel1/subLevel2/", "/aFolder/subLevel1/subLevel2/"},
                new String[] {"aFolder/subLevel1/subLevel2/.", "/aFolder/subLevel1/subLevel2/", "/aFolder/subLevel1/subLevel2/"},
                new String[] {"aFolder/subLevel1/../.", "/aFolder/", "/aFolder/"},
                new String[] {"aFolder/subLevel1/subLevel2/..", "/aFolder/subLevel1/", "/aFolder/subLevel1/"},
                new String[] {"/aFolder/subLevel1/subLevel2/../subLevel2", "/aFolder/subLevel1/subLevel2/", "/aFolder/subLevel1/subLevel2/"},
                new String[] {"/aFolder/./subLevel1/subLevel2/../subLevel2", "/aFolder/subLevel1/subLevel2/", "/aFolder/subLevel1/subLevel2/"},
                new String[] {"/../aFolder/subLevel1/subLevel2/../subLevel2", "/aFolder/subLevel1/subLevel2/", "/aFolder/subLevel1/subLevel2/"},
                new String[] {"aFolder/../../..", "/", "/"},
        };
    }

    @DataProvider
    public Object[][] multiLevelPathsNotExist() {
        return new String[][] {
                //fileToTraverseTo, fullPathForTargetFile, CwdPathAfterTraverse
                new String[] {"fooFolder", "/fooFolder/", "/fooFolder/"},
                new String[] {"/barFolder", "/barFolder/", "/barFolder/"},
                new String[] {"fooFolder/subLevel1", "/fooFolder/subLevel1/", "/fooFolder/subLevel1/"},
                new String[] {"/barFolder/subLevel1/subLevel2", "/barFolder/subLevel1/subLevel2/", "/barFolder/subLevel1/subLevel2/"},
                new String[] {"barFolder/subLevel1/subLevel2/./subLevel3", "/barFolder/subLevel1/subLevel2/subLevel3/", "/barFolder/subLevel1/subLevel2/subLevel3/"},
                new String[] {"/aFolder/subLevel1/subLevel2/../subLevel2.1", "/aFolder/subLevel1/subLevel2.1/", "/aFolder/subLevel1/subLevel2.1/"},
                new String[] {"/../aFolder/subLevel1/./../subLevel1/subLevel2/./subLevel5", "/aFolder/subLevel1/subLevel2/subLevel5/", "/aFolder/subLevel1/subLevel2/subLevel5/"},
                new String[] {"foobarFolder/../../..", "/", "/"},
                new String[] {"/../../barfooFolder/../barfooFolder", "/barfooFolder/", "/barfooFolder/"},
        };
    }

    @DataProvider
    public Object[][] oneLevelPaths() {
        return new String[][] {
                new String[] {".", "/"},
                new String[] {"..", "/"},
                new String[] {"aFolder", "/aFolder/"},
                new String[] {"bFolder", "/bFolder/"},
                new String[] {"aFile", "/aFile"}
        };
    }

    @DataProvider
    public Object[][] oneLevelPathsNotExist() {
        return new String[][] {
                new String[] {"cFolder", "/cFolder/"}
        };
    }

    @Test(dataProvider = "oneLevelPaths")
    public void testOneLevelTraversal(String traverseTo, String expectedFullPath) {
        _pathTraverser = new PathTraverser(_root, _root);
        File file = _pathTraverser.traverseOneLevel(traverseTo, false);
        Assert.assertEquals(file.getFullPath(), expectedFullPath);
    }

    @Test(dataProvider = "oneLevelPathsNotExist")
    public void testOneLevelTraversalForceCreate(String traverseTo, String expectedFullPath) {
        _pathTraverser = new PathTraverser(_root, _root);
        File file = _pathTraverser.traverseOneLevel(traverseTo, true);
        Assert.assertEquals(file.getFullPath(), expectedFullPath);
    }

    @Test
    public void testOneLevelTraversalNotExist() {
        _pathTraverser = new PathTraverser(_root, _root);
        File dir = _pathTraverser.traverseOneLevel("NotExistingDir", false);
        Assert.assertNull(dir);
    }

    @Test(dataProvider = "multiLevelPaths")
    public void testAnyLevelTraversalNotCreate(String traverseTo, String expectedFullPath, String expectedCwdFullPath) {
        _pathTraverser = new PathTraverser(_root, _root);
        File file = _pathTraverser.traverseToAnyLevel(traverseTo, false);
        Assert.assertEquals(file.getFullPath(), expectedFullPath);
        Assert.assertEquals(_pathTraverser.getCwd().getFullPath(), expectedCwdFullPath);
    }

    @Test(dataProvider = "multiLevelPathsNotExist")
    public void testAnyLevelTraversalForceCreate(String traverseTo, String expectedFullPath, String expectedCwdFullPath) {
        _pathTraverser = new PathTraverser(_root, _root);
        File file = _pathTraverser.traverseToAnyLevel(traverseTo, true);
        Assert.assertEquals(file.getFullPath(), expectedFullPath);
        Assert.assertEquals(_pathTraverser.getCwd().getFullPath(), expectedCwdFullPath);
    }

    @Test
    public void testAnyLevelTraversalFromNonRoot() {
        _pathTraverser = new PathTraverser(_root, _root);
        _pathTraverser.traverseToAnyLevel("/create/a/random/folder/to/traverse/to", true);
        Assert.assertEquals(_pathTraverser.getCwd().getFullPath(), "/create/a/random/folder/to/traverse/to/");

        // Traverse starting from a sub dir
        File file = _pathTraverser.traverseToAnyLevel("traverse/../from/children/./level", true);
        Assert.assertEquals(file.getFullPath(), "/create/a/random/folder/to/traverse/to/from/children/level/");
        Assert.assertEquals(_pathTraverser.getCwd().getFullPath(), "/create/a/random/folder/to/traverse/to/from/children/level/");

        // Traverse starting from root again
        file = _pathTraverser.traverseToAnyLevel("/create/a/random/folder/to/traverse/to/traverse", false);
        Assert.assertEquals(file.getFullPath(), "/create/a/random/folder/to/traverse/to/traverse/");
        Assert.assertEquals(_pathTraverser.getCwd().getFullPath(), "/create/a/random/folder/to/traverse/to/traverse/");
    }
}

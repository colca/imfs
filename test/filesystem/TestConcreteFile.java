package filesystem;

import imfs.api.File;
import imfs.filesystem.ConcreteFile;
import imfs.filesystem.Directory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static imfs.filesystem.FileModifyOptions.KeepPrevious;
import static imfs.filesystem.FileModifyOptions.Replace;

public class TestConcreteFile {
    private Directory _root = new Directory();
    private Directory _cwd;

    @BeforeTest
    public void init() {
        // /fooFolder/barFolder
        _cwd = (Directory) _root.createNewSubFile("fooFolder", true);
        _cwd = (Directory) _cwd.createNewSubFile("barFolder", true);
        _cwd.createNewSubFile("fileNotFolder", false);
    }

    @DataProvider
    public Object[][] fileNames() {
        return new String[][] {
                new String[] {"a", "/a"},
                new String[] {"b", "/b"},
                new String[] {"c", "/c"}
        };
    }

    @DataProvider
    public Object[][] newFileData() {
        //fileName, fileContent, expectedFileContent
        return new String[][] {
                new String[] {"a", null, ""},
                new String[] {"b", "", ""},
                new String[] {"c", "random content", "random content"}
        };
    }

    @DataProvider
    public Object[][] moveFileDataExistingDir() {
        return new String[][] {
                /*new String[] {"a", "content1", "newA", "/newA"},*/
                new String[] {"b", "content2", "fooFolder/newB", "/fooFolder/newB"},
                new String[] {"c", "content3", "/fooFolder/new c", "/fooFolder/new c"},
                new String[] {"d", "content3", "fooFolder/barFolder/new d", "/fooFolder/barFolder/new d"},
                new String[] {"e", "content3", "fooFolder/barFolder/e", "/fooFolder/barFolder/e"},
                new String[] {"f", "content3", "/f", "/f"},
        };
    }

    @Test (dataProvider = "newFileData")
    public void testConcreteFileCreate(String name, String fileContent, String expected) {
        ConcreteFile file = new ConcreteFile(name, _root);
        file.writeFileContent(fileContent);
        String retrievedContent = (String)file.getFileContent();

        Assert.assertEquals(retrievedContent, expected, "file content should be same after writing op");

        Assert.assertEquals(file.getName(), name);
        Assert.assertFalse(file.isDirectory());
    }

    @Test (dataProvider = "fileNames")
    public void testFileFullPathInRoot(String name, String expected) {
        File file = new ConcreteFile(name, _root);
        Assert.assertEquals(file.getFullPath(), expected);
    }

    @Test
    public void testFileFullPathInNestedDir() {
        Directory sub = new Directory("sub1", _root);
        File file = sub.createNewSubFile("a", false);
        Assert.assertEquals(file.getFullPath(), "/sub1/a");

        sub = (Directory) sub.createNewSubFile("sub2", true);
        file = sub.createNewSubFile("a", false);
        Assert.assertEquals(file.getFullPath(), "/sub1/sub2/a");
    }

    //TODO: add concurrency test
    @Test
    public void testConcurrentFileWrite(){

    }

    @Test (dataProvider = "moveFileDataExistingDir")
    public void testMoveFileToNewLocationNoCreate(String name, String content,
                                               String newLocation, String expectedFullPath) {
        ConcreteFile file = new ConcreteFile(name, _root);
        String previousFullPath = file.getFullPath();
        Directory oldParent = file.getParent();
        file.writeFileContent(content);
        file.move(newLocation, false, Replace);

        Assert.assertEquals(file.getFileContent(), content, "content should stay same after file move");
        Assert.assertEquals(file.getFullPath(), expectedFullPath);

        Assert.assertFalse(oldParent.hasFile(name), "previous directory should not contain old file name anymore");
        Assert.assertFalse(oldParent.hasFile(file.getName()), "previous directory should not contain new file name neither");
    }

    @Test (expectedExceptions = { IllegalStateException.class },
            expectedExceptionsMessageRegExp = "file name \"\" provided is invalid")
    public void testMoveFileToInvalidDir() {
        ConcreteFile file = new ConcreteFile("file", _root);
        file.move("//", false, KeepPrevious);
    }

    @Test (expectedExceptions = { IllegalStateException.class },
            expectedExceptionsMessageRegExp = "File /fooFolder/barFolder/NotExistingFolder doesn't exist!")
    public void testMoveFileToInvalidPathNotExisting() {
        File file = new ConcreteFile("file", _cwd);
        file.move("/fooFolder/barFolder/NotExistingFolder/file", false, KeepPrevious);
    }

    @Test (expectedExceptions = { IllegalStateException.class },
            expectedExceptionsMessageRegExp = "File /fooFolder/barFolder/fileNotFolder already exists but it's not a directory!")
    public void testMoveFileToInvalidPathWithFile() {
        File file = new ConcreteFile("file", _cwd);
        file.move("/fooFolder/barFolder/fileNotFolder/file", false, KeepPrevious);
    }

    @Test
   public void testMoveFileToPathWithSameNameFile() {
        File file = new ConcreteFile("file", _root);
        File sameNameFile = _cwd.createNewSubFile("file", false);
        file.move(sameNameFile.getFullPath(), false, KeepPrevious);
        Assert.assertEquals(file.getFullPath(), "/file");
        Assert.assertEquals(sameNameFile.getFullPath(), "/fooFolder/barFolder/file");
    }

    @Test
    public void testFileDelete(){
        ConcreteFile file = new ConcreteFile("a", _root);
        file.writeFileContent("content");
        Directory parent = file.getParent();
        file.delete();
        Assert.assertFalse(parent.hasFile("a"));
    }
}

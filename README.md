# IMFS
An In-Memory File System for user to play with. 
You need to have Java installed.

## Overview
This is a simplified file system that supports both files and directories. 
The IMFS is built with Spring Shell to provide the interative command line console, mimiking Linux file system 
file operations. 

## Usage
Commands support any level file path traversal such as /../a/b/./c or ../b/c/
* mv
  * move a file to any level of directory, if a file with same name exists in destination it'll be replaced.
  * -f option allows force creation if destination directory doesn't exist
* rm
  * remove a file or directory at any level.
* find -r
  * -r option allows recursive find.
* write
  * write String content to a target file, the file has to be existing and only content appending is supported now.
* cat
  * print content of a file at any level.
* cd
  * change cwd to a directory at any level.
  * --force option allows force creation if destination directory doesn't exist
 
Commands support only current working directory
* ls
  * list all contents of current directory non-recursively
  * -l option allows printing full path
* pwd
  * print current working directory path
* mkdir
  * make a sub directory in current working directory
* touch
  * create a new file under current working directory

### Enter IMFS Console
If not build from source, the command line can be accessed by executing the released jar directly:
```
$cd release/
$java -cp imfs.jar imfs.demo.Main 
```
![Screenshot 2023-10-06 170154](https://github.com/colca/imfs/assets/3991118/6a48745d-afb9-4796-8e7f-18525cd03d66)

### Demo

![Screenshot 2023-10-06 170011](https://github.com/colca/imfs/assets/3991118/7be5df5b-4fd0-4b4b-91a8-a9e0aea1b804)


## Features Supported

* Change the current working directory.
  The working directory begins at '/'. You may traverse to a child directory or the parent.

* Get the current working directory. 
Returns the current working directory's path from
the root to console. Example: ‘/school/homework’

* Create a new directory. 
The current working directory is the parent.

* Get the directory contents.
Returns the children of the current working directory.
Example: [‘math’, ‘history’, ‘spanish’]

* Remove a directory. 
The target directory must be among the current working directory’s!
children.

* Create a new file. 
Creates a new empty file in the current working directory.

* Write file contents.
Writes the specified contents to a file in the current working
directory. All file contents will fit into memory.

* Get file contents.
Returns the content of a file in the current working directory.

* Move a file.
Move an existing file in the current working directory to a new location (in the same directory).

* Find a file/directory.
Given a filename, find all the files and directories within the current
working directory that have exactly that name.

## Extra Features Supported

* Move files. 
You can move files to any location and same name files will be replaced.

* Operations on paths
When doing basic operations (changing the current working directory, creating or
moving files or folders, etc), you can use absolute paths instead of only operating
on objects in the current working directory.
You can use relative paths (relative to the current working directory) as well,
including the special “..” path that refers to the parent directory.
When creating or moving items to a new path, you can choose to automatically
create any intermediate directories on the path that don’t exist yet.


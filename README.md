# Duke project template

This is a project template for a greenfield Java project. It's named after the Java mascot _Duke_. Given below are instructions on how to use it.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/Duke.java` file, right-click it, and choose `Run Duke.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:
   ```
    ____        _        
   |  _ \ _   _| | _____ 
   | | | | | | | |/ / _ \
   | |_| | |_| |   <  __/
   |____/ \__,_|_|\_\___|
   ```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## AI assistance

This project was developed with the help of AI, as cited below.

* **Tool:** [Claude Code](https://claude.com/claude-code), using Claude Opus models.
* **Used by:** Ryan Chua.
* **Extent of use:**
  * Used throughout the project as a helper rather than as the main author: explaining concepts and errors,
    suggesting designs and refactorings, reviewing code against the SE-EDU coding standard, and drafting
    tests, Javadoc comments and commit messages. All suggestions were reviewed, and edited where needed,
    before they were kept.
  * Files mostly written with Claude Code say so in a comment at the top of the file (e.g. the JavaFX GUI
    classes in `src/main/java/myriad/ui/`).
  * The user guide, `docs/README.md`, was drafted with Claude Code and checked against the actual output
    of the app.

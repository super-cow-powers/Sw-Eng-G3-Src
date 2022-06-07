[![Lint Code Base](https://github.com/super-cow-powers/Sw-Eng-G3-Src/actions/workflows/super-linter.yml/badge.svg?branch=master)](https://github.com/super-cow-powers/Sw-Eng-G3-Src/actions/workflows/super-linter.yml)

[![Java CI with Maven](https://github.com/super-cow-powers/Sw-Eng-G3-Src/actions/workflows/maven_test.yml/badge.svg?branch=master)](https://github.com/super-cow-powers/Sw-Eng-G3-Src/actions/workflows/maven_test.yml)

# README
This is the Source for Group 3's 2021/2022 University of York Electronic Engineering Software Engineering Project.
It is a graphical Multimedia Application Development suite, programmable in a variety of Languages through bound scripts; it is somewhat simillar to a web browser's ES engine integration, where attached scripts may be executed on user interaction with elements.

## Samples
Samples of what can be built with our application may be found in `demo` and `sales_demo`.

## Building
The project requires Java 11 and OpenJFX 14 or higher, and builds with Maven. To build and run, clone this repository and then run `mvn javafx:run` which will fetch project dependancies, and then build and run the project. Certain platform-specific configurations are provided, and will be triggered automatically. 

## Editing
Any editor may be used to edit the project source. However, for easy integration with Maven, we suggest using Netbeans (with the CheckBeans linter extension) or IntelliJ; any support requests for issues relating to Eclipse will not be given priority.

## Making Changes
To commit any changes first open a new working branch, and when your changes are complete please open a new Pull Request to request the changes to be merged into Master. Your changes will be automatically tested and linted through the CICD pipeline, reviewed, and then either merged or rejected.

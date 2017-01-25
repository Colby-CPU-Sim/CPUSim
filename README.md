# CPUSim

## Introduction
CPU Sim is a Java application that allows users to design simple computer CPUs at the microcode level and to run 
machine-language or assembly-language programs on those CPUs through simulation. It can be used to simulate a variety 
of architectures, including accumulator-based, RISC-like, or stack-based (such as the JVM) architectures. It is a useful 
tool for instructors who want their students to get hands-on exposure to a variety of architectures and to get a chance 
to design and implement their own architectures and write programs in machine language and assembly language for their 
architectures.

The newest version 4 of CPU Sim is written using the JavaFX package (Version 3, which is still available, was written 
using Swing). Version 4 should run on any platform that has the Java runtime environment (JRE) version 1.8.0u40 or later 
installed on it. Version 3 should run on any platform that has JRE version 1.5 or later installed on it. Version 4 is 
backwards compatible with version 3 in that all machines created in version 3 can be loaded and run in version 4.

## Features

The CPU Sim application is a fully-integrated development environment that includes the following features.

* Tools for designing a CPU at the register-transfer level:
    - Dialogs for specifying the number and width of registers, register arrays, and RAMs.
    - Dialogs for specifying the microinstructions (e.g., bit transfers between registers) that are used to implement the
      machine instructions
    - A dialog for specifying the machine instructions, including:
      * the number of bits in each instruction
      * the opcode value and the number of bits the opcode occupies
      * the number of the operands and the properties of each operand
      * the semantics of each instruction (as specified by a sequence of microinstructions)
      
* A text editor with syntax highlighting for writing assembly language programs
* An assembler for converting assembly programs into machine code for the user's CPU.
* A debugger for stepping forward and backward through the execution of such programs, inspecting and optionally 
  changing the machine state after each step.
  
## Articles about CPUSim and its precursor

KERRIDGE, J., AND WILLIS, N. 1980. A Simulator for Teaching Computer Architecture. SIGCSE Bulletin, 12(2), 65-71.

This simulator is the precursor to CPU Sim.

SKRIEN, D., AND HOSACK, J. 1991. A multilevel simulator at the register transfer level for use in an introductory 
machine organization class. SIGCSE Bulletin (Papers of the 22nd ACM/SIGCSE Technical Symposium on Computer Science 
Education), 23(1), 347-351.

This article describes version 1.0.13 of CPU Sim.

SKRIEN, D. 1994. CPU Sim: A Computer Simulator for Use in an Introductory Computer Organization Class. 
Journal of Computing in Higher Education, 6(1), 3-13.

This article describes version 2.2 of CPU Sim.

SKRIEN, D. 2001. CPU Sim 3.1: A tool for simulating computer architectures for computer organization classes. 
(ACM Journal of Educational Resources in Computing (JERIC))[http://www.acm.org/pubs/jeric/] 1(4), 46-59. 
This issue of JERIC is a special one devoted to computer architecture simulators.

I also presented a (faculty poster)[http://www.cs.wpi.edu/%7Ecew/sigcse2002posters/skrien.html] at SIGCSE 2002 about 
CPU Sim 3.1. 

## Development

This project is licensed under the **TODO INSERT LICENSE HERE** located in LICENSE.md. 

### Building CPUSim

CPU Sim requires *Oracle* Java 1.8+ (Oracle is required for JavaFX), and Gradle 2.+ to be built. This guide assumes you 
have Gradle installed and is accessible via `gradle` on the command-line (Window's users, it is likely `gradle.bat`). 

For all development environments, clone the repository: `git clone https://github.com/Colby-CPU-Sim/CPUSimFX2015.git`

#### Within IntelliJ

Most developers will use (IntelliJ Idea)[https://www.jetbrains.com/idea/] by JetBrains. To develop with IntelliJ, 
download and install IntelliJ. 

1. Open IntelliJ
2. Import the Project Directory, choose "Gradle" as the import type
3. Select the JDK to be Oracle 1.8 and set the Gradle Home if you haven't already
    * On Linux, this will likely be: `/usr/lib/gradle/$GRADLE_VERSION`
    * On Mac, if you installed via homebrew, `/usr/local/Cellar/gradle/$GRADLE_VERSION/libexec`
    * On Windows, TODO :(
4. Let the gradle import run

#### From the command-line

This guide involves working directly with the command-line, for *NIX like environments (OS X, Linux) it is the same 
directions. 

##### Linux/Mac OS X

1. Open up a Terminal (xterm, iTerm, Terminal, Konsole, etc.)
2. Change to the clone directory: `cd path/to/cpusim/clone`
3. Build the distribution: `gradle dist`
4. Run the project: `java -jar ./build/libs/CPUSim-*.jar`

## Open Source Usage

### cpusim/util/Gullectors.java

Taken from [maciejmiklas/cyclop#b90df02](https://github.com/maciejmiklas/cyclop/blob/b90df02ab952e1aebccbb4be2595dc49e1464a99/cyclop-webapp/src/main/java/org/cyclop/common/Gullectors.java)
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
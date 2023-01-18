## Java debugger

This is a simple debugger implementation in Java using the Java Platform Debugger Architecture (JPDA). It only supports single-threaded Java applications.

### Usage
```
java Main <debuggee> [<debuggee-args>, ..]
```

#### Example:
```
cd \debugger\src\main\java
javac -g org/ssw/*.java
java org.ssw.Main org.ssw.Debuggee
```

### Commands
| command                      | description                                                            |
|------------------------------|------------------------------------------------------------------------|
| `print`                      | print all variables and their values                                   |
| `print <variable-name>`      | print the detailed value of the `<variable-name>`                      |
| `step over`                  | go to the next line in the current stack frame                         |
| `step into`                  | go to the next line (i.e. go to the first line of the called method)   |
| `step out`                   | go to the next line in the outer stack frame                           |
| `breakpoints`                | print all breakpoints as line numbers                                  |
| `breakpoints <line-number>`  | add a breakpoint at `<line-number>`                                    |
| `breakpoints -<line-number>` | remove the breakpoint at `<line-number>`                               |
| `trace`                      | print all currently active methods of the debuggee (i.e. a stacktrace) |
| `continue`                   | continue the program until the next breakpoint or the end              |
# commandLineShell

> Shell written in Java

This is a basic shell, which supports complex commands but is not a programming language of it's own, unlike shells like sh, bash, and zsh.

## Building and Running

To build the program, simply run:

> `./build.sh`

To then run the program, run:

> `java -jar build/CommandShell.jar`

## Differences from Bash

Besides not being a programming language, there are a few other differences from bash:

1. Pipes will fail after the first failed command (in bash this only happens with the `pipefail` parameter set)
2. `cd` will change the directory of the terminal, but only for the next command. For example, `cd .. && ls` will print the contents of the current directory and move the terminal to the parent directory, so the next command entered will be there.
3. Redirection of groups (except pipes) is treated the same as providing the same redirections to each member. In bash, `(cat in1 && cat in2) > out1` will result in `out1` containg the contents of `in1` and `in2` one after the other, and `(cat in1 > out1 && cat in2 > out2) > out3` will result in `out1` containing the contents of `in1`, `out2` containing the contents of `in2`, and `out3` being blank. In this shell, the same commands will result in `out1` and `out3` containing the contents of `in2`.
4. The shell will error if a command is incomplete (for example, it ends with a `|` or a `&&`)
5. `exit` will not exit immediately, but rather once the current command is finished. For example, `exit && ls` will still list the contents of the current directory before exiting.

## Built-in Commands

The shell contains some built-in commands that provide basic functionality. They should be familiar to those familiar with `bash` and friends. These are:

- `cd`: Change the working directory to the given directory. `cd -` is a special case and will go back to the previous working directory.
- `echo`: Output any arguments given
- `pwd`: Output the current working directory
- `history`: Print all the commands that have been entered in the current session
- `source`: Run each line of the given file(s) as if they are commands. Note: Will create a new session, so any `cd`s or `exit`s will not affect the current session, plus any commands in the file will not be added to the current sessions history.
- `exit`: Exit the shell session once the current command is completed. Warning: Running `exit` in the background will cause a race condition and may or may not exit.

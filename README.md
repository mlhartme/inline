# Inline

Library for command line parsing.

## Usage

Start with the following example:

    public class Hello {
      public static int main(String[] args) {
        Cli cli = Cli.create("Hello, World")
        System.exit(cli.run(args));
      }
    }

Running this code with not argument prints the usage message passed to the create method: Hello, World.
Passing a single "help" argument has the same effect; the create method returns a command line interface
with two commands: help and version. Help is the default command, thus, you get it if you don't pass any
arguments.

Now add you a "greetings" command - change the code to

    public class Hello {
      public static int main(String[] args) {
        Cli cli = Cli.create("Hello, World");
        cli.add(Greetings.class, "greetings -count=1 name");
        System.exit(cli.run(args));
      }

      public static class Greetings {
        private final String count;
        private final String name;

        public Hello(int count, String name) {
          this.count = count;
          this.name = name;
        }

        public void run() {
          for (int i = 0; i < count; i++) {
            System.out.println("greetings, " + name);
          }
        }
      }
    }

A command is defined by an arbitrary Class with a "run" method, and you use

    Cli.add(Greetings.class, "greetings -count=1 name")

to add it to the command line parser. It takes a class and a syntax argument. The syntax that Cli will
the class if the first argument is "greetings" and a count option and a name value are passed as arguments
to the constructor.



## Examples

See https://github.com/mlhartme/inline/blob/master/src/test/java/net/oneandone/inline/samples/



## Concepts

A command line consists of options (anything starting with '-') and values (anything else).
Use Inline to map command lines to Command objects and invoke a method on them.

The preferred way to populate commands is via Constructor injection. Because this is the best documentation and it
yields command objects that can be re-used in normal code (e.g. if one command aggregares other commands).

Rationale: Inline uses systax specification instead of annotations. Because:
* easier to read and write, more expressive
* existing code (where you cannot add annotations) can be used for commands

### Command

A command is an arbitrary object with a method ()->int or ()->void.

### Primitives

Cli objects maintain a configurable set of primitives. A primitive defines how to convert a command line argument (i.e. String) 
into an Object (e.g. an int or a File). They are use to convert arguments passed to commands.

### Contexts

A Context is an object used by Commands that originates not from the command line. 
Typical example is an object for global configuration used by all commands.

## Exception handling

The default exception handling is to catch checked exceptions and ArgumentExceptions, print the message
to the user, and return with exit code -1. Other exceptions are not caught, the jvm will print a stacktrace 
for them.

You can change the exception handling by creating Cli with a different exceptionHandler.

## Unused code elimination

If you use tools like ProGuard http://proguard.sourceforge.net/manual/usage.html you have to add extra declarations
to indicate what code is accessed by Inline. One way to do so:
* place all Command Objects in a package "cli"
* make all fields, constructors and methods used in your cli public
* add a declaration to keep all public stuff in the cli package.

## Migration from Sushi 2.8.x

Inline is a spin of from https://github.com/mlhartme/sushi/tree/sushi-2.8.19. 

Benefits:
* simplified main class
* cardinality for values - you can defines optional values now
* encourage real command objects, where all arguments are passed to the constructor
* return values for commands
* configurable exception handling

Changes:
* options can be placed anywhere one the command line now

Restrictions:
* the built-in option -pretend is gone

Steps

* Globals
  * create a new class Globals as a copy of the Main class
  * dump the static methods, just keep the main object with it's state
  * remove annotations from fields, but remember the syntax
  * create getter methods for the arguments passed to the command 
    objects when calling @child methods
  * remove all @child methods and the invoke method

* Main
	* dump "extends Cli" and "implements Command"
	* replace new Main().run(args) with
	    Cli cli = Cli.create(""); 
	    cli.begin(Globals.class, (syntax from globals))
        ( - commands will be inserted here - )
	    cli.run(args);
	* add
  * move the help text from Main.printHelp to the Cli.create argument and dump Main.printHelp
  * dump Main.invoke() { printHelp(); }
  * for each @Child method: remove the method and add a line cli.add(MyCommand.class, "commandName") instead

* For all classes that directly or indirectly implement Command
  * dump Command
  * change the constructor to take one Globals argument instead of the current arguments; 
    fill the respective fields of the command by using getters from Globals
  * rename the invoke() method to run()
  * remove all @Value and @Option annotations and append constructor arguments instead; 
    add the respective syntax to Main. 
  * remove @Remaining annotation and add a Mapping instead

* For command base classes
  * adjust the constructor to pass around globals
  * add a world field to your Base command class and use it instead of console.world


## Alternatives

Rationale: I know there's https://github.com/airlift, but I need context objects. 


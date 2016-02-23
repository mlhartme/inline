# inline
Library for command line parsing.

A command line consists of options and values Cli is the main class in this package.

The preferred way to populate commands is via Constructor injection. Because this is the best documentation and it
yields command objects that can be re-used in normal code (e.g. if one command aggregares other commands).

Rationale: I know there's https://github.com/airlift, but I need context objects and my console with default options
and exception handling. (And the same for JCommander)

## Migration from Sushi 2.8.x

Inline is a spin of from https://github.com/mlhartme/sushi/tree/sushi-2.8.19. 

Steps
* Main class
	* dump "extends Cli" and "implements Command"
	* invoke
	    Cli.create 
  * move the help text from Main.printHelp to Cli.create argument
  * for each @Child command invoke Cli.add()
* For all classes that directly or indirectly implement Command
  *	dump Command
  * remove all @Value and @Option annotations and introducate constructor arguments instead; add the respective syntax to Main. 
  * remove @Remaining annotation and add a Mapping instead

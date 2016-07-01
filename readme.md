## How to compile your configuration in Java: from config file to java source code

[![Build Status](https://travis-ci.org/todvora/cfg-compile.svg?branch=master)](https://travis-ci.org/todvora/cfg-compile)

Lets suppose you have a [configuration file](https://github.com/todvora/cfg-compile/blob/master/myapp/src/main/resources/settings.cfg) in the following form:

```ini
# System wide configuration
[SystemConstants]
MAX_MEMORY = 120
DISK_QUOTA = 50
HOME_ROOT = "/home"

# Users module
[UserConstants]
BOOST = 3.5
BOOST_ENABLED = true
```

Maybe because it's shared between your java app and some legacy app. Maybe because
your technical support / ops like this format and can adapt it easily, before building a new
release. Or maybe because you don't want to change something everyone i familiar with.

**Your task is to read the config file in your application and use those constants on proper places.**

So how would you handle it?

### The usual solution

If you are lucky, there already exists a library to parse this config in java. Maybe
it's [ini4j](http://ini4j.sourceforge.net/), maybe standard [java properties](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html) file.
If not, you are on your own. So hack some regex and iterate over all lines, do some find-and-replace, prepare a singleton and utility classes to access properties easily? And then usage like:

```java
int maxMemory = Config.getInstance().getIntValue("SystemConstants", "MAX_MEMORY");
```

You write tests, right? So cover it for some obscure cases and hope for the best?

### No, compile it!

But there must be a better approach. This isn't how one builds a reliable solution.
We have compilers, data types checking, code generators, maven goals and plugins, tons of useful libraries and tools. Let's get the best of it.

What would I like to see? A Java class, called ```SystemConstants.java``` with following content, automatically generated from the config above:

```java
public final class SystemConstants {

	public static final int MAX_MEMORY = 120;
	public static final int DISK_QUOTA = 50;
	public static final String HOME_ROOT = "/home";
}
```

Then in my real code, access to this config value would be only

```java
int maxMemory = SystemConstants.MAX_MEMORY;
```

Clean, easy to read and actually use, compile-time verifiable, type secure, code completion friendly.

All we need to figure out is, how to transform our config file and generate some java source code of it. In the same way we do it with WSDL ([wsdl2java](https://cxf.apache.org/docs/wsdl-to-java.html)).

### Fun with Parboiled and Roaster

#### Step one - tree structure
No matter how simple or complicated your config is, regex is probably not the right tool. It will be  either too naive and fragile or extremely complicated and unreadable. You have probably already heard of [abstract syntax trees](https://en.wikipedia.org/wiki/Abstract_syntax_tree). This is how source code is translated to a tree representation before compilation.

In our case it should translate the config into something like:

```
Config
├── SystemConstants
│   ├── MAX_MEMORY = 120 (int)
│   ├── DISK_QUOTA = 50 (int)
│   └── HOME_ROOT = "/home" (String)
└── UserConstants
    ├── BOOST = 3.5 (double)
    └── BOOST_ENABLED = true (boolean)
```

Two different config enums, each with several key-value pairs. Values can be of different type - ```integer```, ```double```, ```boolean```, ```String```. All we need to do is to parse a ```*.cfg``` file and construct similar tree of it.

#### Parboiled enters
To avoid regexes and line iterating, let's check [Parboiled](https://github.com/sirthias/parboiled/wiki). Parboiled is a parsing library based on [parsing expression grammars](https://en.wikipedia.org/wiki/Parsing_expression_grammar). We have to describe supported elements of our config language an subsequent actions on the tree nodes. To get a better idea, what is this all about, check the [Calculator example](https://github.com/sirthias/parboiled/wiki/Calculators).

In our case, we have to describe several structures:
- Section (*SystemConstants*, *UserConstants*), each holding one or more key-value pairs
- Key-Value pair, compound of
  - Key identificator (variable name)
  - Value - ```integer```, ```double```, ```boolean```, ```String```
- Comments and whitespaces, to cover rest the of the file

Parboiled gives us the power to define each structure as a java method. Take for example the key-value pair:

```java
    Rule Assignment() {
        return Sequence(
                Identifier(),
                EQUAL,
                Value(),
                Spacing()
        );
    }
```

And a Section consists of one or more Assignments:

```java
  Rule Section() {
        return Sequence(
                LEFT_BRACKET,
                Identifier(),
                RIGHT_BRACKET,
                OneOrMore(Assignment())
        );
    }
```
This is the [actual code](https://github.com/todvora/cfg-compile/blob/master/codegen/src/main/java/cz/tomasdvorak/codegen/parser/ConfigurationGrammar.java), used to parse our configuration file - not a pseudo code. Pretty cool, right? Readable, easy to write and actually understand, unlike typical regex for such a complicated structure.

Parboiled provides a [value stack](https://github.com/sirthias/parboiled/wiki/The-Value-Stack), convenient for storing our node values. Consider following ```Integer``` value rule:

```java
Rule Integer() {
        return Sequence(
                OneOrMore(Digit()),
                push(Integer.parseInt(match()))
        );
    }
```
Integer consists of one or more digits. At the end of *digits* matching, we *push* the *match* to the value stack. From the stack can be the value obtained later, when the whole *Assignment* is matched and a key-value pair build. The same methods can be applied to an *Assignment* and later to a *Section*. *Assignment* will *pop* the last and last minus one values. Last is the actual value, last minus one is the key. And so one. You get the idea.

Now we are able to build the whole configuration tree described above. Sections->Assignments->Key-value pairs. What's next?

#### Step two - Java source code generator
We have a config tree. Converting to the java classes should be more or less:

- Iterate over all *Section* nodes
- Convert *Section* to a class
- Attach every *Assignment* to this class as a *field*
- Persist class to a file

Since java source codes are usual text files, we could do it on our own. But String joining of all the pieces together could quickly become same nightmare as those regexes before. Luckily we don't have to do it. [Roaster](https://github.com/forge/roaster) is a parser / formatter of java source codes. It provides fluent API to generate java classes:

```java
final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
javaClass.setPackage("cz.tomasdvorak").setName("SystemConstants");

javaClass.addField()
  .setName("MAX_MEMORY")
  .setType("int")
  .setLiteralInitializer("120")
  .setPublic()
  .setStatic(true)
  .setFinal(true);
```

Will produce following source code:

```java
package cz.tomasdvorak;
public class SystemConstants {
	public static final int MAX_MEMORY = 120;
}

```
All we need to do is to iterate over *Sections*, iterate over *Assignments* and generate bunch of the source codes.

### Maven magic
Now we are able to parse the configuration and generate java source codes of it. Those *generated-sources* have to be available before the compiler starts to compile our actual application sources. How to glue everything together?

We will need following project layout:

```
Root project
├── Codegen
│   ├── Parser
│   ├── Generator
│   └── Main
└── MyApp
    ├── Generated-sources
    │   ├── SystemConstants.java
    │   └── UserConstants.java
    └── Sources
        └── *.java    
```

With the right Maven setup, following steps take places during ```mvn package``` of the root project:

- ```compile``` phase of *Codegen* source codes - prepaires [Parser](https://github.com/todvora/cfg-compile/blob/master/codegen/src/main/java/cz/tomasdvorak/codegen/parser/ConfigurationParser.java), [Grammar](https://github.com/todvora/cfg-compile/blob/master/codegen/src/main/java/cz/tomasdvorak/codegen/parser/ConfigurationGrammar.java), [Generator](https://github.com/todvora/cfg-compile/blob/master/codegen/src/main/java/cz/tomasdvorak/codegen/generator/ClassesGenerator.java) and [Main](https://github.com/todvora/cfg-compile/blob/master/codegen/src/main/java/cz/tomasdvorak/codegen/Codegen.java)
- ```generate-sources``` phase of *MyApp*, where Codegen.Main is executed and all configuration classes are generated from provided config file, then everything persisted
- ```compile``` phase of *MyApp* is executed, with already available Configuration sources
- ```test``` phase of *MyApp* is executed, when compilation succeeds
- ...

The root project doesn't have any sources and serves only as an [aggregator project](https://maven.apache.org/pom.html#Aggregation). Two subprojects are needed to separate compile phases of Parser/Generator and actual application sources. [Exec Maven Plugin](http://www.mojohaus.org/exec-maven-plugin/) executes the Generator.Main during ```generate-sources``` phase of *MyApp* project, triggering the whole parse-generate-persist chain.

This project structure also ensures the [Separation of concerns](https://en.wikipedia.org/wiki/Separation_of_concerns). Your application only provides a ```*.cfg``` file and receives ready-to-use java classes from them. All the parsing and generating is separated in its own project. You can have many apps using the similar configuration files, every app depending on the same *Codegen* project. So next time you don't have to [repeat yourself](https://en.wikipedia.org/wiki/Don't_repeat_yourself).

### Results
We are ready, all the infrastructure is prepaired. It's time to write our app, that depends on generated *SystemConstants* and *UserConstants*. It could be something like:

```java
package cz.tomasdvorak.users;

import cz.tomasdvorak.myapp.settings.SystemConstants;
import cz.tomasdvorak.myapp.settings.UserConstants;

public class UserUtils {

      public static double getMaxMemory() {
        if(UserConstants.BOOST_ENABLED) {
            return SystemConstants.MAX_MEMORY * UserConstants.BOOST;
        } else {
            return SystemConstants.MAX_MEMORY;
        }
    }
}
```

If we remove any of ```BOOST_ENABLED```, ```BOOST``` or ```MAX_MEMORY``` assignments in the ```*.cfg``` file, the compilation fails. If we change any of them to different type - like String, the compilation fails. If we rename them, the compilation fails. Everything must be pretty much aligned to pass the compile phase. **The majority of errors can and will be detected before our tests are started**. There exists no simple workaround / hack like disabling or ignoring failing test results. Even without a single test we are still able to catch most of the typical errors and mistakes. Our app code is clean, reliable, typed, available for code completion. What more to ask for?!

Don't forget to check the [travis-ci build](https://travis-ci.org/todvora/cfg-compile) of this repository. It proves, that everything plays nicely together. Have you find a better solution or a bug in my code? Let me know on [twitter](https://twitter.com/tdvorak) or send a [pull request](https://github.com/todvora/cfg-compile/pulls). Thanks! 

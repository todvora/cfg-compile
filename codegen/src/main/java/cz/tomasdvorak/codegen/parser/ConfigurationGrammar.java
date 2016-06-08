package cz.tomasdvorak.codegen.parser;

import cz.tomasdvorak.codegen.dto.Pair;
import cz.tomasdvorak.codegen.dto.Config;
import cz.tomasdvorak.codegen.dto.Section;
import cz.tomasdvorak.codegen.parser.utils.ListBuilder;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.Label;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;

import java.util.List;

/**
 * Grammar for parsing our own conf structure. The structure is notably similar to ini files structure known from
 * Windows. That's because almost everyone is used to it, it's easy to read and write, doesn't have any problems
 * with pair braces, commas and so one.
 *
 * For your case you can implement any grammar you like or even skip the grammar part and reuse any of widely used parsing
 * libraries for yml, json, properties or ini files.
 *
 * @see <a href="https://github.com/sirthias/parboiled/wiki">Parboiled wiki</a>
 * @see <a href="https://en.wikipedia.org/wiki/Parsing_expression_grammar">Parsing expression grammar</a>
 * @see <a href="http://ini4j.sourceforge.net/index.html">ini4j</a>
 */
@SuppressWarnings("WeakerAccess")
@BuildParseTree
class ConfigurationGrammar extends BaseParser<Object> {

    Rule Configuration() {
        return Sequence(
                Sections(),
                push(new Config((List<Section>) pop())),
                END_OF_INPUT
        );
    }

    Rule Sections() {
        final ListBuilder<Section> list = new ListBuilder<>();
        return Sequence(
                OneOrMore(
                        Section(),
                        list.add((Section) pop()) // save one section to the list
                ),
                push(list.getAndReset()) // push all sections to the stack
        );
    }

    Rule Section() {
        return Sequence(
                LBRK,
                Identifier().label("SectionName"),
                RBRK,
                Assignments(),
                push(new Section((String) pop(1), (List<Pair>) pop()))
        );
    }

    Rule Assignments() {
        final ListBuilder<Pair> list = new ListBuilder<>();
        return Sequence(
                OneOrMore(
                        Assignment(),
                        list.add((Pair) pop())
                ),
                push(list.getAndReset()));
    }

    Rule Assignment() {
        return Sequence(
                Identifier().label("Key"),
                EQUAL,
                Value(),
                Spacing(),
                push(new Pair((String) pop(1), pop()))
        );
    }

    @SuppressSubnodes
    @Label("Value")
    Rule Value() {
        return FirstOf(
                Double(),
                Integer(),
                Boolean(),
                StringLiteral()
        );
    }

    @SuppressSubnodes
    Rule StringLiteral() {
        return Sequence(
                '"',
                ZeroOrMore(
                        Sequence(TestNot(AnyOf("\r\n\"")), ANY)
                ),
                push(String.valueOf(match())),
                '"'
        );
    }

    @SuppressSubnodes
    Rule Boolean() {
        return Sequence(
                FirstOf("true", "false"),
                push(Boolean.valueOf(match()))
        );
    }

    @SuppressSubnodes
    Rule Identifier() {
        return Sequence(
                Spacing(),
                TestNot(OneOrMore(Integer())),
                OneOrMore(FirstOf(CharRange('A', 'Z'), CharRange('a', 'z'), OneOrMore("_"), Digit())),
                push(match()),
                Spacing()
        );
    }

    Rule Digit() {
        return CharRange('0', '9');
    }

    @SuppressSubnodes
    Rule Integer() {
        return Sequence(
                OneOrMore(Digit()),
                push(Integer.parseInt(match()))
        );
    }

    @SuppressSubnodes
    Rule Double() {
        return Sequence(
                Sequence(
                        Optional(Ch('-')),
                        Digit(),
                        Ch('.'),
                        OneOrMore(Digit())
                ),
                push(Double.parseDouble(match()))
        );
    }

    @SuppressNode
    Rule Spacing() {
        return ZeroOrMore(FirstOf(
                // whitespace
                OneOrMore(AnyOf(" \t\r\n\f")),
                // comments
                Sequence(
                        "#",
                        ZeroOrMore(TestNot(AnyOf("\r\n")), ANY),
                        FirstOf("\r\n", '\r', '\n')
                )
        ));
    }

    final Rule EQUAL = Terminal("=");
    final Rule LBRK = Terminal("[");
    final Rule RBRK = Terminal("]");
    final Rule END_OF_INPUT = EOI.suppressNode();

    @SuppressNode
    Rule Terminal(final String string) {
        return Sequence(Spacing(), IgnoreCase(string), Spacing());
    }
}
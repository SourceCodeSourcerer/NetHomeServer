package nu.nethome.home.impl;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class CommandLineParserTest {

    @Test
    public void parseEmptyLine() throws Exception {
        List<String> tokens = CommandLineParser.parseLine("");
        assertThat(tokens.size(), is(0));
    }

    @Test
    public void parseEmptyTokenInMiddle() throws Exception {
        List<String> tokens = CommandLineParser.parseLine("One,,Two");
        assertThat(tokens.size(), is(3));
        assertThat(tokens, hasItems("One", "", "Two"));
    }

    @Test
    public void parseEndingWithComma() throws Exception {
        List<String> tokens = CommandLineParser.parseLine("One,");
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("One", ""));
    }

    @Test
    public void parseStartingWithComma() throws Exception {
        List<String> tokens = CommandLineParser.parseLine(",One");
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("", "One"));
    }

    @Test
    public void parseSimpleLineWithOneToken() throws Exception {
        List<String> tokens = CommandLineParser.parseLine("One");
        assertThat(tokens.size(), is(1));
        assertThat(tokens, hasItems("One"));
    }

    @Test
    public void parseSimpleLineWithTwoTokens() throws Exception {
        List<String> tokens = CommandLineParser.parseLine("One,Two");
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("One", "Two"));
    }

    @Test
    public void parseSimpleLineWithTwoTokensAndQuotedChars() throws Exception {
        List<String> tokens = CommandLineParser.parseLine("One%2C,Two%25");
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("One,", "Two%"));
    }

    @Test
    public void keepsSpaces() throws Exception {
        List<String> tokens = CommandLineParser.parseLine("One , Two");
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("One ", " Two"));
    }
}

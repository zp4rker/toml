package com.github.jezza.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.github.jezza.TomlTable;

/**
 * @author Jezza
 */
abstract class AbstractTest {
	InputStream locate(String file) {
		InputStream in = getClass().getResourceAsStream(file);
		if (in == null) {
			throw new IllegalStateException("Unable to locate \"" + file + '"');
		}
		return in;
	}

	Reader reader(String file) {
		return new InputStreamReader(locate(file), StandardCharsets.UTF_8);
	}

	_TomlLexer lexer(String file) {
		return new _TomlLexer(reader(file));
	}

	static Token slurp(String input) {
		_TomlLexer lexer = new _TomlLexer(input);
		try {
			Token token = lexer.next();
			assertEquals(Tokens.EOS, lexer.next().type, "Input wasn't completely consumed");
			return token;
		} catch (IOException e) {
			throw new IllegalStateException("Should never happen", e);
		}
	}

	static String nom(String input) {
		Token string = slurp(input);
		assertTrue(string.type == Tokens.STRING || string.type == Tokens.ML_STRING, "Failed to parse string");
		return string.value;
	}

	static void test(String expected, String input) {
		assertEquals(expected, nom(input), "Failed to parse string correctly...");
	}

	Token slurpFile(String file) throws IOException {
		_TomlLexer lexer = new _TomlLexer(reader(file));
		Token token = lexer.next();
		assertEquals(Tokens.EOS, lexer.next().type, "Input wasn't completely consumed");
		return token;
	}

	String nomFile(String file) throws IOException {
		Token string = slurpFile(file);
		assertTrue(string.type == Tokens.STRING || string.type == Tokens.ML_STRING, "Failed to parse string");
		assertTrue(string.value instanceof String, "Output is a string type, but doesn't hold a String value");
		return string.value;
	}

	void testFile(String expected, String file) throws IOException {
		assertEquals(expected, nomFile(file), "Failed to parse string correctly...");
	}

	TomlTable parse(String file) throws IOException {
		return new TomlParser(reader(file)).parse();
	}

	String parseToString(String file) throws IOException {
		TomlTable table = parse(file);
		StringBuilder b = new StringBuilder();
		table.write(b, 2, 2);
		return b.toString();
	}

	void compareOutput(String expected, String input) throws IOException {
		String output = parseToString(input);
		String content = new String(locate(expected).readAllBytes(), StandardCharsets.UTF_8);
		assertEquals(content, output);
	}
}

import java.util.ArrayList;

/**
 * The Parser class handles converting the user input into an expression.
 * This could be a single command, or multiple commands combined using different
 * operators.
 * It also finds input and output redirections.
 * 
 * Implementation Note: In order to increase efficiency, this Parser does not
 * use a seperate tokenizer, instead tokenizing the input on demand
 */
public class Parser {
  String input;
  int pos;

  public Parser(String input) {
    this.input = input;
    this.pos = 0;
  }

  Token next() {
    char chr = input.charAt(pos);
    while (chr == ' ') {
      chr = input.charAt(++pos);
    }
    switch (chr) {
      case '|':
        pos++;
        return new Token(Token.Type.Pipe, chr);
      case '<':
        pos++;
        return new Token(Token.Type.RedirectInput, chr);
      case '&':
        if (input.charAt(pos + 1) == '&') {
          pos += 2;
          return new Token(Token.Type.ExecuteSequential, "&&");
        }
        pos++;
        return new Token(Token.Type.ExecuteParallel, chr);
      case '>':
        if (input.charAt(pos + 1) == '>') {
          pos += 2;
          return new Token(Token.Type.RedirectOutputAppend, ">>");
        }
        pos++;
        return new Token(Token.Type.RedirectOutput, chr);
      case '"':
      case '\'': {
        // TODO: Allow escaping quotes with backslashes
        pos++;
        int startPos = pos;
        char c = input.charAt(pos);
        while (c != chr) {
          pos++;
          if (pos >= input.length())
            break;
          c = input.charAt(pos);
        }
        pos++;
        return new Token(Token.Type.String, input.substring(startPos, pos - 1));
      }
      default: {
        int startPos = pos;
        while (input.charAt(pos) != ' ') {
          pos++;
          if (pos >= input.length())
            break;
        }
        pos++;
        return new Token(Token.Type.String, input.substring(startPos, pos - 1));
      }
    }
  }

  ArrayList<Token> parse() {
    ArrayList<Token> tokens = new ArrayList<>();
    while (pos < input.length()) {
      tokens.add(this.next());
    }
    return tokens;
  }

  static class Token {
    enum Type {
      String, // Sequence of characters, part of a command
      Pipe, // '|' Directs one process's output into another's input
      RedirectInput, // '<' Passes a file as input to a process
      RedirectOutput, // '>' Passes a process's output into a file
      RedirectOutputAppend, // '>>' Appends a process's output onto a file
      ExecuteParallel, // '&' Executes two processes in parallel and returns if they both succeeded
      ExecuteSequential, // '&&' Executes one process then the next, stopping on the first one that fails
    }

    Type type;
    String value;

    Token(Type type, String value) {
      this.type = type;
      this.value = value;
    }

    Token(Type type, char value) {
      this.type = type;
      this.value = new String(new char[] { value });
    }

    @Override
    public String toString() {
      return type.toString() + "(" + value + ")";
    }
  }
}

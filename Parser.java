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
  // Used to tell ArrayList method which array type to return
  final String[] STRING_ARR = new String[0];

  /**
   * Construct a parser
   * 
   * @param input The string to parse
   */
  public Parser(String input) {
    this.input = input;
    this.pos = 0;
  }

  /**
   * Parse the input into a process tree
   */
  public ProcessNode parse() throws SyntaxException {
    return this.parseExpression();
  }

  /**
   * Parse an expression, which is a command with optional special characters
   * after it, optionally enclosed in parentheses.s
   */
  ProcessNode parseExpression() throws SyntaxException {
    ArrayList<String> args = new ArrayList<>();
    Token tok = this.next();
    ProcessNode node;
    // If we find an open paren, recuse into ourselves to find the subexpression
    // Closing the paren is handled in parseSpecialCharacter
    if (tok.type == Token.Type.OpenParen) {
      node = this.parseExpression();
      tok = this.next();
    } else {
      while (tok != null && tok.type == Token.Type.String) {
        args.add(tok.value);
        tok = this.next();
      }
      node = new ShellProcess(args.toArray(STRING_ARR));
    }
    return this.parseSpecialCharacter(node, tok);
  }

  /**
   * Parse a special character. This method's basic job is to determine what to do
   * based on token type.
   * 
   * @param node The node that came before this special character.
   * @param chr  The special character in question
   */
  ProcessNode parseSpecialCharacter(ProcessNode node, Token chr) throws SyntaxException {
    if (chr == null)
      return node;
    switch (chr.type) {
      case CloseParen:
        return node;
      case Pipe:
        ProcessNode child = this.parseExpression();
        node.setPipe(child);
        break;
      case RedirectInput:
      case RedirectOutput:
      case RedirectOutputAppend:
        Token file = this.next();
        if (file.type != Token.Type.String) {
          throw new SyntaxException("Unexpected token '" + file.value + "'.", pos - file.value.length(), input);
        }
        if (chr.type == Token.Type.RedirectInput) {
          node.setInput(file.value);
        } else {
          node.setOutput(file.value, chr.type == Token.Type.RedirectOutputAppend);
        }
        break;
      case ExecuteParallel:
      case ExecuteSequential:
        node = new ProcessGroup(new ProcessNode[] { node, this.parseExpression() },
            chr.type == Token.Type.ExecuteSequential);
        break;
      default:
        throw new SyntaxException("Unexpected token '" + chr.value + "'.", pos - chr.value.length(), input);
    }
    return this.parseSpecialCharacter(node, this.next());
  }

  /**
   * Get the next token in the input stream
   */
  Token next() {
    if (pos >= input.length())
      return null;
    char chr = input.charAt(pos);
    while (chr == ' ') {
      pos++;
      if (pos >= input.length())
        return null;
      chr = input.charAt(pos);
    }
    switch (chr) {
      case '|':
        pos++;
        return new Token(Token.Type.Pipe, chr);
      case '<':
        pos++;
        return new Token(Token.Type.RedirectInput, chr);
      case '(':
        pos++;
        return new Token(Token.Type.OpenParen, chr);
      case ')':
        pos++;
        return new Token(Token.Type.CloseParen, chr);
      case '&':
        if (pos + 1 < input.length() && input.charAt(pos + 1) == '&') {
          pos += 2;
          return new Token(Token.Type.ExecuteSequential, "&&");
        }
        pos++;
        return new Token(Token.Type.ExecuteParallel, chr);
      case '>':
        if (pos + 1 < input.length() && input.charAt(pos + 1) == '>') {
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
        while (input.charAt(pos) != chr) {
          pos++;
          if (pos >= input.length())
            break;
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

  static class Token {
    enum Type {
      String, // Sequence of characters, part of a command
      Pipe, // '|' Directs one process's output into another's input
      RedirectInput, // '<' Passes a file as input to a process
      RedirectOutput, // '>' Passes a process's output into a file
      RedirectOutputAppend, // '>>' Appends a process's output onto a file
      ExecuteParallel, // '&' Executes two processes in parallel and returns if they both succeeded
      ExecuteSequential, // '&&' Executes one process then the next, stopping on the first one that fails
      OpenParen, // '(' Open a parenthesized group
      CloseParen, // ')' Close a parenthesized group
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

  /**
   * Represents an issue with the syntax of the passed input
   */
  public static class SyntaxException extends Exception {
    String input;
    int pos;

    SyntaxException(String message, int pos, String input) {
      super(message);
      this.pos = pos;
      this.input = input;
    }

    @Override
    public String toString() {
      char[] chars = new char[pos];
      for (int i = 0; i < pos; i++)
        chars[i] = ' ';
      String indent = new String(chars);
      return "> " + input + "\n> " + indent + "^" + "\n> " + indent + this.getLocalizedMessage();
    }
  }
}

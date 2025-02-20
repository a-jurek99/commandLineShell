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
  int parenLevel;
  boolean background;
  Token curToken;
  // Used to tell ArrayList method which array type to return
  final String[] STRING_ARR = new String[0];
  final ProcessNode[] NODE_ARR = new ProcessNode[0];

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
    this.next();
    ProcessNode root = this.parseMaybeGroup();
    if (parenLevel != 0) {
      throw new SyntaxException("Mismatched parentheses.", pos, input);
    }
    root.setBackground(background);
    return root;
  }

  /**
   * Parse something that could either be a group or an expression
   */
  ProcessNode parseMaybeGroup() throws SyntaxException {
    ProcessNode left = this.parseExpression();
    if (curToken == null) {
      return left;
    } else if (curToken.isGrouping()) {
      return this.parseGroup(left);
    } else if (curToken.type == Token.Type.CloseParen) {
      parenLevel--;
      this.next();
    }
    return left;
  }

  /**
   * Parse an expression, which is either a group enclosed in parentheses or a
   * command with optional redirects
   */
  ProcessNode parseExpression() throws SyntaxException {
    ProcessNode node;
    // If we find an open paren, go back to the top level (parseMaybeGroup), which
    // handles the closing
    if (curToken.type == Token.Type.OpenParen) {
      parenLevel++;
      this.next();
      node = this.parseMaybeGroup();
    } else if (curToken.type == Token.Type.String) {
      node = this.parseCommand();
    } else {
      throw this.makeUnexpectedToken();
    }
    this.maybeParseRedirects(node);
    return node;
  }

  /**
   * Parse a command, which is just a series of one or more strings
   */
  ProcessNode parseCommand() throws SyntaxException {
    ArrayList<String> args = new ArrayList<>();
    while (curToken != null && curToken.type == Token.Type.String) {
      args.add(curToken.value);
      this.next();
    }
    return new ShellProcess(args.toArray(STRING_ARR));
  }

  /**
   * Parse the redirects for another node, which is a series of '<', '>', or '>>'
   * with files after each
   */
  void maybeParseRedirects(ProcessNode node) throws SyntaxException {
    while (curToken != null && curToken.isRedirect()) {
      Token.Type type = curToken.type;
      this.next();
      if (curToken.type != Token.Type.String) {
        throw this.makeUnexpectedToken();
      }
      if (type == Token.Type.RedirectInput) {
        node.setInput(curToken.value);
      } else {
        node.setOutput(curToken.value, type == Token.Type.RedirectOutputAppend);
      }
      this.next();
    }
  }

  /**
   * Parse a group, which is a series of expressions seperated by '&', '&&', or
   * '|'
   */
  ProcessNode parseGroup(ProcessNode left) throws SyntaxException {
    ArrayList<ProcessNode> members = new ArrayList<>();
    members.add(left);
    ProcessGroup.Type groupType = getGroupType(curToken.type);
    this.next();
    while (true) {
      if (curToken == null) {
        if (groupType == ProcessGroup.Type.Parallel) {
          background = true;
          break;
        } else {
          throw new SyntaxException("Unexpected end of input.", pos, input);
        }
      }
      ProcessNode node = this.parseExpression();
      if (curToken == null || curToken.type == Token.Type.CloseParen) {
        if (curToken != null) {
          parenLevel--;
          this.next();
        }
        members.add(node);
        break;
      } else if (curToken.isGrouping()) {
        ProcessGroup.Type curType = getGroupType(curToken.type);
        if (curType == groupType) {
          members.add(node);
        } else {
          members.add(this.parseGroup(node));
          break;
        }
      } else {
        throw this.makeUnexpectedToken();
      }
      this.next();
    }
    if (members.size() == 1) {
      return members.get(0);
    }
    return new ProcessGroup(members.toArray(NODE_ARR), groupType);
  }

  /**
   * Get the type of the group formed by a given token type
   * 
   * @param type The type of the token forming the group
   */
  ProcessGroup.Type getGroupType(Token.Type type) throws SyntaxException {
    switch (type) {
      case Pipe:
        return ProcessGroup.Type.Pipe;
      case ExecuteParallel:
        return ProcessGroup.Type.Parallel;
      case ExecuteSequential:
        return ProcessGroup.Type.Sequential;
      default:
        throw new SyntaxException("This should be impossible.", pos, input);
    }
  }

  /**
   * Make an unexpected token syntax exception based on the current token
   */
  SyntaxException makeUnexpectedToken() {
    return new SyntaxException("Unexpected token '" + curToken.value + "'.", pos - curToken.value.length(), input);
  }

  /**
   * Move to the next token in the input stream
   */
  Token next() {
    if (pos >= input.length())
      return curToken = null;
    char chr = input.charAt(pos);
    while (chr == ' ') {
      pos++;
      if (pos >= input.length())
        return curToken = null;
      chr = input.charAt(pos);
    }
    switch (chr) {
      case '|':
        pos++;
        return curToken = new Token(Token.Type.Pipe, chr);
      case '<':
        pos++;
        return curToken = new Token(Token.Type.RedirectInput, chr);
      case '(':
        pos++;
        return curToken = new Token(Token.Type.OpenParen, chr);
      case ')':
        pos++;
        return curToken = new Token(Token.Type.CloseParen, chr);
      case '&':
        if (pos + 1 < input.length() && input.charAt(pos + 1) == '&') {
          pos += 2;
          return curToken = new Token(Token.Type.ExecuteSequential, "&&");
        }
        pos++;
        return curToken = new Token(Token.Type.ExecuteParallel, chr);
      case '>':
        if (pos + 1 < input.length() && input.charAt(pos + 1) == '>') {
          pos += 2;
          return curToken = new Token(Token.Type.RedirectOutputAppend, ">>");
        }
        pos++;
        return curToken = new Token(Token.Type.RedirectOutput, chr);
      case '"':
      case '\'': {
        pos++;
        int startPos = pos;
        char c = input.charAt(pos);
        char prevC = c;
        while (prevC == '\\' || c != chr) {
          pos++;
          if (pos >= input.length())
            break;
          prevC = c;
          c = input.charAt(pos);
        }
        pos++;
        String value = input.substring(startPos, pos - 1).replace("\\" + chr, new String(new char[] { chr }));
        return curToken = new Token(Token.Type.String, value);
      }
      default: {
        int startPos = pos;
        char c = input.charAt(pos);
        while (c != ' ' && c != '(' && c != ')' && c != '&' && c != '<' && c != '>') {
          pos++;
          if (pos >= input.length())
            break;
          c = input.charAt(pos);
        }
        return curToken = new Token(Token.Type.String, input.substring(startPos, pos));
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

    /**
     * Does this token redirect input or output?
     */
    boolean isRedirect() {
      return type == Type.RedirectInput || type == Type.RedirectOutput || type == Type.RedirectOutputAppend;
    }

    /**
     * Does this token form groups?
     */
    boolean isGrouping() {
      return type == Type.Pipe || type == Type.ExecuteParallel || type == Type.ExecuteSequential;
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

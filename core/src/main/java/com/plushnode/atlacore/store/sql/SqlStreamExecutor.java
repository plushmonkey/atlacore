package com.plushnode.atlacore.store.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Stack;

// Adds delimiter support for executing an SQL file
public class SqlStreamExecutor implements AutoCloseable {
    private enum State { QUERY_BUILD, DELIMITER_CHANGE, DELIMITER_CHANGING, DELIMITER_CHECK }
    private final Connection connection;
    private final InputStreamReader in;
    private String delimiter = ";";
    private static final String DELIMITER_KEYWORD = "DELIMITER";
    private Stack<StringBuilder> parseStack = new Stack<>();
    private State state = State.QUERY_BUILD;
    private int delimCheckIndex = 0;
    private int delimChangeIndex = 0;

    public SqlStreamExecutor(Connection connection, InputStream stream) {
        this.in = new InputStreamReader(stream);
        this.connection = connection;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

    private void executeStatement(String statement) {
        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(statement);
        } catch (SQLException e) {}
    }

    private void doParse(char c) {
        switch (this.state) {
            case QUERY_BUILD:
                if (c != this.delimiter.charAt(0) && c != 'D') {
                    this.parseStack.peek().append(c);
                    return;
                }
                this.parseStack.push(new StringBuilder());
                this.parseStack.peek().append(c);
                if (c == this.delimiter.charAt(0)) {
                    this.state = State.DELIMITER_CHECK;
                    ++this.delimChangeIndex;
                } else {
                    this.state = State.DELIMITER_CHANGE;
                    ++this.delimCheckIndex;
                }
                break;
            case DELIMITER_CHANGE:
                if (this.delimCheckIndex >= DELIMITER_KEYWORD.length()) {
                    this.state = State.DELIMITER_CHANGING;
                    this.delimCheckIndex = 0;
                    this.parseStack.peek().setLength(0);
                } else if (DELIMITER_KEYWORD.charAt(this.delimCheckIndex) == c) {
                    ++this.delimCheckIndex;
                    this.parseStack.peek().append(c);
                } else {
                    String query = this.parseStack.pop().toString();
                    this.parseStack.peek().append(query);
                    this.state = State.QUERY_BUILD;
                    this.delimCheckIndex = 0;
                    this.doParse(c);
                }
                break;
            case DELIMITER_CHECK:
                if (this.delimChangeIndex >= this.delimiter.length()) {
                    this.state = State.QUERY_BUILD;
                    this.delimChangeIndex = 0;
                    this.parseStack.pop();
                    this.executeStatement(this.parseStack.pop().toString());
                    this.parseStack.push(new StringBuilder());
                    this.doParse(c);
                } else if (this.delimiter.charAt(this.delimChangeIndex) == c) {
                    ++this.delimChangeIndex;
                    this.parseStack.peek().append(c);
                } else {
                    String query = this.parseStack.pop().toString();
                    this.parseStack.peek().append(query);
                    this.state = State.QUERY_BUILD;
                    this.delimChangeIndex = 0;
                    this.doParse(c);
                }
                break;
            case DELIMITER_CHANGING:
                if (!Character.isWhitespace(c))
                    this.parseStack.peek().append(c);
                else {
                    this.delimiter = this.parseStack.pop().toString();
                    this.state = State.QUERY_BUILD;
                }
                break;
        }
    }

    public void execute() {
        StringBuilder queryBuilder = new StringBuilder();
        this.parseStack.push(queryBuilder);
        CharBuffer buffer = CharBuffer.allocate(256);
        try {
            while (this.in.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining())
                    this.doParse(buffer.get());
                buffer.clear();
            }
        } catch (IOException e) {}
    }
}

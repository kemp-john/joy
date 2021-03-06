package org.joy.core.persistence.jdbc.support.db;

import org.joy.commons.lang.string.StringTool;
import org.joy.commons.log.Log;
import org.joy.commons.log.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Sql script containing a series of statements terminated by semi-columns (;). Single-line (--) and multi-line (/* * /)
 * comments are stripped and ignored.
 */
public class SqlScript {
    private static final Log LOG = LogFactory.getLog(SqlScript.class);

    /**
     * The database-specific support.
     */
    private final DbSupport dbSupport;

    /**
     * The sql statements contained in this script.
     */
    private final List<SqlStatement> sqlStatements;

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource The sql script as a text block with all placeholders already replaced.
     * @param dbSupport       The database-specific support.
     */
    public SqlScript(String sqlScriptSource, DbSupport dbSupport) {
        this.dbSupport = dbSupport;
        this.sqlStatements = parse(sqlScriptSource);
    }

    /**
     * Dummy constructor to increase testability.
     *
     * @param dbSupport The database-specific support.
     */
    SqlScript(DbSupport dbSupport) {
        this.dbSupport = dbSupport;
        this.sqlStatements = null;
    }

    /**
     * For increased testability.
     *
     * @return The sql statements contained in this script.
     */
    public List<SqlStatement> getSqlStatements() {
        return sqlStatements;
    }

    /**
     * Parses this script source into statements.
     *
     * @param sqlScriptSource The script source to parse.
     * @return The parsed statements.
     */
    /* private -> for testing */
    List<SqlStatement> parse(String sqlScriptSource) {
        return linesToStatements(readLines(new StringReader(sqlScriptSource)));
    }

    /**
     * Turns these lines in a series of statements.
     *
     * @param lines The lines to analyse.
     * @return The statements contained in these lines (in order).
     */
    /* private -> for testing */
    List<SqlStatement> linesToStatements(List<String> lines) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        boolean inMultilineComment = false;
        Delimiter nonStandardDelimiter = null;
        SqlStatementBuilder sqlStatementBuilder = dbSupport.createSqlStatementBuilder();

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);

            if (sqlStatementBuilder.isEmpty()) {
                if (StringTool.isEmpty(line)) {
                    // Skip empty line between statements.
                    continue;
                }

                String trimmedLine = line.trim();

                if (!sqlStatementBuilder.isCommentDirective(trimmedLine)) {
                    if (trimmedLine.startsWith("/*")) {
                        inMultilineComment = true;
                    }

                    if (inMultilineComment) {
                        if (trimmedLine.endsWith("*/")) {
                            inMultilineComment = false;
                        }
                        // Skip line part of a multi-line comment
                        continue;
                    }

                    if (sqlStatementBuilder.isSingleLineComment(trimmedLine)) {
                        // Skip single-line comment
                        continue;
                    }
                }

                Delimiter newDelimiter = sqlStatementBuilder.extractNewDelimiterFromLine(line);
                if (newDelimiter != null) {
                    nonStandardDelimiter = newDelimiter;
                    // Skip this line as it was an explicit delimiter change directive outside of any statements.
                    continue;
                }

                sqlStatementBuilder.setLineNumber(lineNumber);

                // Start a new statement, marking it with this line number.
                if (nonStandardDelimiter != null) {
                    sqlStatementBuilder.setDelimiter(nonStandardDelimiter);
                }
            }

            sqlStatementBuilder.addLine(line);

            if (sqlStatementBuilder.isTerminated()) {
                SqlStatement sqlStatement = sqlStatementBuilder.getSqlStatement();
                statements.add(sqlStatement);
                LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql());

                sqlStatementBuilder = dbSupport.createSqlStatementBuilder();
            }
        }

        // Catch any statements not followed by delimiter.
        if (!sqlStatementBuilder.isEmpty()) {
            statements.add(sqlStatementBuilder.getSqlStatement());
        }

        return statements;
    }

    /**
     * Parses the textual data provided by this reader into a list of lines.
     *
     * @param reader The reader for the textual data.
     * @return The list of lines (in order).
     * @throws IllegalStateException Thrown when the textual data parsing failed.
     */
    private List<String> readLines(Reader reader) {
        List<String> lines = new ArrayList<String>();

        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse lines", e);
        }

        return lines;
    }
}

package org.str.backend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.str.backend.lifeparserParser.Content_lineContext;
import org.str.backend.lifeparserParser.FileContext;
import org.str.backend.lifeparserParser.Pattern_statementsContext;

public class LifParser {

    private static final Logger logger = LoggerFactory.getLogger(LifParser.class);

    private static InputStream skipUntilPatterns(InputStream input) {
        try {
            List<String> lines = IOUtils.readLines(input, StandardCharsets.UTF_8);
            if (!lines.get(0).equals("#Life 1.05")) {
                throw new ParseCancellationException("Invalid header");
            }
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).startsWith("#D") || lines.get(i).startsWith("#N")) {
                    // OK
                } else if (lines.get(i).startsWith("#P")) {
                    // Found first pattern line.
                    lines = lines.stream().skip(i).collect(Collectors.toList());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.writeLines(lines, "\n", baos, StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(baos.toByteArray());
                }
            }
            // No patterns
            throw new ParseCancellationException("No patterns in file");
        } catch (IOException e) {
            throw new ParseCancellationException(e);
        }
    }

    public GameOfLifeState parse(InputStream input) {
        // temporary hack to allow correct parsing.
        input = skipUntilPatterns(input);
        ErrorListener errorListener = new ErrorListener();
        try {
            lifeparserLexer lexer = new ErrorReportingLexer(new ANTLRInputStream(input));
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            lifeparserParser parser = new lifeparserParser(tokens);
            parser.setErrorHandler(new BailErrorStrategy());
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            ParserRuleContext tree;
            // tokens.fill();
            // for (int i = 0; i < tokens.size(); i++) {
            // System.out.println("Token " + i + " is " + tokens.get(i));
            // }
            tree = parser.file();
            // walk the parse tree
            GameOfLifeState r = new GameBuilderVisitor().visit(tree);
            return r;
        } catch (RecognitionException e) {
            logger.error("Parse error: {}", e.getMessage() + errorListener.getMessage());
        } catch (ParseCancellationException e) {
            logger.error("Parse cancelled: {}", e.getMessage() + errorListener.getMessage());
        } catch (Exception e) {
            logger.error("Failed to parse input: {}", e.getMessage());
        }
        return new GameOfLifeState(1, 1);
    }

    private static class ErrorListener extends BaseErrorListener {
        private final List<String> errors = new ArrayList<String>();

        public String getMessage() {
            return StringUtils.join(errors, "\n");
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                String msg, RecognitionException e) {
            errors.add("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    private static class ErrorReportingLexer extends lifeparserLexer {
        public ErrorReportingLexer(CharStream input) {
            super(input);
        }

        @Override
        public void recover(LexerNoViableAltException e) {
            // Stop lexing.
            logger.error("Got LNVA: {}", e.getMessage());
            // Apparently there is a null pointer exception bug in ANTLR runtime
            // and you can't throw out a LNVA exception
            throw new ParseCancellationException(e.getMessage());
        }
    }

    private static class Rectangle {
        public final int x, y, w, h;

        @Override
        public String toString() {
            return String.format("[%d,%d - %d,%d]", x, y, w, h);
        }

        public Rectangle() {
            x = y = w = h = 0;
        }

        public Rectangle(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private static class GameBuilderVisitor extends lifeparserBaseVisitor<GameOfLifeState> {

        @Override
        public GameOfLifeState visitFile(FileContext ctx) {
            return ctx.pattern_statements().accept(this);
        }

        @Override
        public GameOfLifeState visitPattern_statements(Pattern_statementsContext ctx) {
            // determine bounds by parsing each pattern definition
            Rectangle bounds = ctx.pattern_statement().stream().map(ps -> getBounds(ps)).reduce(new Rectangle(),
                    (a, b) -> union(a, b));
            GameOfLifeState r = new GameOfLifeState(bounds.w, bounds.h);
            logger.info("Bounds {},{},{},{}", bounds.x, bounds.y, bounds.w, bounds.h);
            // parse each pattern again to copy the cells into the array
            // representation.
            ctx.pattern_statement().stream().forEach(ps -> {
                int x0 = Integer.valueOf(ps.x.getText());
                int y0 = Integer.valueOf(ps.y.getText());
                logger.info("Read coord {},{} normalized {},{}", x0, y0, x0 - bounds.x, y0 - bounds.y);
                for (int y = 0; y < ps.content.size(); y++) {
                    Content_lineContext c = ps.content.get(y);
                    for (int x = 0; x < c.chars.size(); x++) {
                        Token t = c.chars.get(x);
                        if (t.getType() == lifeparserLexer.STAR) {
                            // logger.info("Setting at {},{}", x + x0 -
                            // bounds.x, y + y0 - bounds.y);
                            try {
                                r.set(x + x0 - bounds.x, y + y0 - bounds.y);
                            } catch (ArrayIndexOutOfBoundsException e) {
                                logger.error("out of bounds: {},{} on {},{}", x + x0 - bounds.x, y + y0 - bounds.y,
                                        bounds.w, bounds.h);
                                throw e;
                            }
                        }
                    }
                }
            });
            return r;
        }

        private static Rectangle union(Rectangle a, Rectangle b) {
            if (a.w == 0 || a.h == 0)
                return b;
            int x0 = Math.min(a.x, b.x);
            int y0 = Math.min(a.y, b.y);
            int x1 = Math.max(a.x + a.w, b.x + b.w);
            int y1 = Math.max(a.y + a.h, b.y + b.h);
            Rectangle r = new Rectangle(x0, y0, x1 - x0, y1 - y0);
            // logger.info("Union of {} and {} is {}", a, b, r);
            return r;
        }

        private Rectangle getBounds(lifeparserParser.Pattern_statementContext ctx) {
            try {
                // upper left-hand coordinates are stated explicitly
                int x = Integer.valueOf(ctx.x.getText());
                int y = Integer.valueOf(ctx.y.getText());
                // count of lines is vertical dimension.
                int h = ctx.content.size();
                // maximum of line lengths is horizontal dimension
                int w = ctx.content.stream().map((Content_lineContext c) -> c.chars.size()).max(Integer::max).orElse(0);
                return new Rectangle(x, y, w + 1, h + 1);
            } catch (NumberFormatException e) {
                throw new ParseCancellationException(e);
            }
        }
    }
}

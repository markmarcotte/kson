package org.kson.parser.behavior.embedblock


/**
 * This class represents the behavior for handling embedded block content
 * by parsing and trimming its minimum indentation.
 *
 * @property rawEmbedContent The raw embedded block content as a string
 *                        to be analyzed or transformed.
 */
class EmbedBlockIndent(embedContent: String) {
    private val rawEmbedContent: String = embedContent

    /**
     * Computes the minimum indent of all lines in [rawEmbedContent], then returns
     * the text with that indent trimmed from each line.
     *
     * NOTE: blank lines are considered pure indent and used in this calculation, so for instance:
     *
     * "   this string
     *         has a minimum indent defined
     *       by its last line
     *    "
     *
     * becomes:
     *
     * "  this string
     *      has a minimum indent defined
     *    by its blank last line
     * "
     */
    fun trimMinimumIndent(): String {
        val minCommonIndent = computeMinimumIndent()

        return rawEmbedContent
            .split("\n")
            .joinToString("\n") { it.drop(minCommonIndent) }
    }

    /**
     * Computes the minimum indent in a [rawEmbedContent].
     *
     * NOTE: blank lines are considered pure indent and used in this calculation, so for instance:
     *
     * "   this string
     *         has a minimum indent defined
     *       by its last line
     *    "
     *
     * returns: 2
     */
    fun computeMinimumIndent(): Int {
        /**
         * A genuinely empty line carries no indent information, so it takes no part in the
         * minimum. Whitespace-only lines still do: those spaces are typed characters, and the
         * behavior documented above depends on them. Without this, a single empty line — a
         * markdown paragraph break, say — reports an indent of 0 and flattens the minimum for
         * the whole block.
         *
         * The final line is the exception, and is always counted: it is not content at all but
         * the whitespace preceding the closing delimiter, which defines the minimum indent when
         * it is the least indented (see `testEmbedBlockCloseDelimDefinesMinIndent`). A closer at
         * column 0 leaves that line empty, and its zero must still count.
         */
        val lines = rawEmbedContent.split("\n")
        val linesWithNewlines = lines
            .filterIndexed { index, line -> index == lines.lastIndex || line.isNotEmpty() }
            .map { it + "\n" }

        val minCommonIndent =
            linesWithNewlines.minOfOrNull { it.indexOfFirst { char -> !isInlineWhitespace(char) } } ?: 0

        return minCommonIndent
    }

    /**
     * Returns true if the given [char] is a non-newline whitespace
     */
    private fun isInlineWhitespace(char: Char?): Boolean {
        return char == ' ' || char == '\r' || char == '\t'
    }
}

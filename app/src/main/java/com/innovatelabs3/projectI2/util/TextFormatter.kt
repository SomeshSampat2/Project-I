package com.innovatelabs3.projectI2.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.innovatelabs3.projectI2.ui.theme.OpenSansFont

data class TextSegment(
    val text: AnnotatedString,
    val isCodeBlock: Boolean,
    val language: String? = null
)

object TextFormatter {
    // IntelliJ-inspired color scheme
    private val keywordColor = Color(0xFFCC7832)      // orange for keywords
    private val stringColor = Color(0xFF6A8759)       // green for strings
    private val commentColor = Color(0xFF808080)      // gray for comments
    private val numberColor = Color(0xFF6897BB)       // blue for numbers
    private val functionColor = Color(0xFFFFC66D)     // yellow for function names
    private val typeColor = Color(0xFFCC7832)         // orange for types
    private val propertyColor = Color(0xFF9876AA)     // purple for properties
    private val parenthesisColor = Color(0xFFA9B7C6)  // light gray for parentheses
    private val defaultTextColor = Color(0xFFA9B7C6)  // light gray for default text

    fun formatText(text: String): List<TextSegment> {
        val segments = mutableListOf<TextSegment>()
        var remainingText = text

        while (remainingText.contains("```")) {
            val startIndex = remainingText.indexOf("```")
            if (startIndex > 0) {
                segments.add(TextSegment(
                    formatNonCodeText(remainingText.substring(0, startIndex)),
                    false
                ))
            }
            
            val endIndex = remainingText.indexOf("```", startIndex + 3)
            if (endIndex != -1) {
                val codeStart = startIndex + 3
                val languageEndIndex = remainingText.indexOf('\n', codeStart)
                val (language, codeContentStart) = if (languageEndIndex in codeStart until endIndex) {
                    Pair(
                        remainingText.substring(codeStart, languageEndIndex).trim(),
                        languageEndIndex + 1
                    )
                } else {
                    Pair(null, codeStart)
                }
                
                val codeText = remainingText.substring(codeContentStart, endIndex).trim()
                if (codeText.isNotBlank()) {
                    segments.add(TextSegment(
                        formatCodeText(codeText),
                        true,
                        language
                    ))
                }
                remainingText = remainingText.substring(endIndex + 3)
            } else {
                segments.add(TextSegment(
                    formatNonCodeText(remainingText.substring(startIndex)),
                    false
                ))
                remainingText = ""
                break
            }
        }

        if (remainingText.isNotEmpty()) {
            segments.add(TextSegment(
                formatNonCodeText(remainingText),
                false
            ))
        }

        return segments.filter { it.text.text.isNotBlank() }
    }

    private fun formatCodeText(code: String): AnnotatedString = buildAnnotatedString {
        // Base style for code
        addStyle(
            SpanStyle(
                fontFamily = OpenSansFont,
                fontSize = 14.sp,
                color = defaultTextColor,
                background = Color(0xFF2B2B2B)  // IntelliJ dark theme background
            ),
            0,
            code.length
        )

        // Keywords
        val keywords = listOf(
            "fun", "val", "var", "if", "else", "when", "class", "object",
            "interface", "private", "public", "internal", "protected", "return",
            "true", "false", "null", "this", "super", "package", "import"
        )
        val keywordPattern = "\\b(${keywords.joinToString("|")})\\b".toRegex()
        keywordPattern.findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = keywordColor),
                match.range.first,
                match.range.last + 1
            )
        }

        // Strings (including multiline)
        Regex("\"\"\"[^\"]*\"\"\"|\"[^\"\\n]*\"").findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = stringColor),
                match.range.first,
                match.range.last + 1
            )
        }

        // Numbers
        Regex("\\b\\d+[.\\d]*[LFf]?\\b").findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = numberColor),
                match.range.first,
                match.range.last + 1
            )
        }

        // Function declarations
        Regex("fun\\s+(\\w+)").findAll(code).forEach { match ->
            val functionName = match.groupValues[1]
            val functionStart = match.range.first + 4 // Skip "fun "
            addStyle(
                SpanStyle(color = functionColor),
                functionStart,
                functionStart + functionName.length
            )
        }

        // Function calls
        Regex("\\b\\w+(?=\\s*\\()").findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = functionColor),
                match.range.first,
                match.range.last + 1
            )
        }

        // Types
        Regex("(?<=[:\\s])([A-Z]\\w*)").findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = typeColor),
                match.range.first,
                match.range.last + 1
            )
        }

        // Properties
        Regex("\\.(\\w+)").findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = propertyColor),
                match.range.first + 1,  // Skip the dot
                match.range.last + 1
            )
        }

        // Comments
        Regex("//.*|/\\*[\\s\\S]*?\\*/").findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = commentColor, fontStyle = FontStyle.Italic),
                match.range.first,
                match.range.last + 1
            )
        }

        // Parentheses and brackets
        Regex("[(){}\\[\\]]").findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = parenthesisColor),
                match.range.first,
                match.range.last + 1
            )
        }

        append(code)
    }

    private fun formatNonCodeText(text: String): AnnotatedString = buildAnnotatedString {
        var currentPosition = 0
        var remainingText = text
        val fontFamily = OpenSansFont

        // Enhanced bullet points with indentation
        remainingText = remainingText.replace(Regex("^(\\s*)[-*+]\\s", RegexOption.MULTILINE)) { match ->
            val indent = match.groupValues[1]
            "$indent• "
        }

        // Process HTML tags first
        val htmlPattern = Regex("<([a-z1-6]+)>(.*?)</\\1>", RegexOption.DOT_MATCHES_ALL)
        htmlPattern.findAll(remainingText).forEach { match ->
            if (match.range.first > currentPosition) {
                append(remainingText.substring(currentPosition, match.range.first))
            }

            val tag = match.groupValues[1]
            val content = match.groupValues[2]

            when (tag) {
                "h1" -> withStyle(SpanStyle(fontFamily = fontFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                    append(content)
                    append("\n")
                }
                "h2" -> withStyle(SpanStyle(fontFamily = fontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                    append(content)
                    append("\n")
                }
                "h3" -> withStyle(SpanStyle(fontFamily = fontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                    append(content)
                    append("\n")
                }
                "b", "strong" -> withStyle(SpanStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold)) {
                    append(content)
                }
                "i", "em" -> withStyle(SpanStyle(fontFamily = fontFamily, fontStyle = FontStyle.Italic)) {
                    append(content)
                }
                "u" -> withStyle(SpanStyle(fontFamily = fontFamily, textDecoration = TextDecoration.Underline)) {
                    append(content)
                }
                "code" -> withStyle(SpanStyle(
                    fontFamily = OpenSansFont,
                    background = Color(0xFF2B2B2B),
                    color = Color(0xFFA9B7C6)
                )) {
                    append(content)
                }
                "pre" -> withStyle(SpanStyle(
                    fontFamily = OpenSansFont,
                    letterSpacing = 0.sp
                )) {
                    append(content)
                    append("\n")
                }
                else -> append(content)
            }
            
            currentPosition = match.range.last + 1
        }

        // Process bold text
        val boldPattern = Regex("""\*\*(.*?)\*\*""")
        boldPattern.findAll(remainingText).forEach { match ->
            if (match.range.first > currentPosition) {
                append(remainingText.substring(currentPosition, match.range.first))
            }
            
            val boldText = match.groupValues[1]
            withStyle(
                SpanStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            ) {
                append(boldText)
            }
            
            currentPosition = match.range.last + 1
        }

        if (currentPosition < remainingText.length) {
            append(remainingText.substring(currentPosition))
        }
    }
} 
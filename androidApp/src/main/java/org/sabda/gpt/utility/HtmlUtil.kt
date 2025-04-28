package org.sabda.gpt.utility

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan

class HtmlUtil {
    companion object{
        fun replaceSpecificTags(input: String): Spanned {
            val output = SpannableStringBuilder()

            val preprocessedInput = input
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
                .replace(Regex("(?m)^\\s+"), "")

            val tagRegex = Regex(
                "<(/?)(h[1-3]|p|br|ul|li|ol|strong|em|blockquote)>|([^<>]+)",
                RegexOption.IGNORE_CASE
            )

            val matches = tagRegex.findAll(preprocessedInput)

            var currentSizeFactor = 1.0f

            val boldStack = mutableListOf<Int>()
            val italicStack = mutableListOf<Int>()
            val sizeStack = mutableListOf<Int>()
            val blockquoteStack = mutableListOf<Int>()

            for (match in matches) {
                val value = match.value.lowercase()

                when (value) {
                    "<h1>", "<h2>", "<h3>" -> {
                        currentSizeFactor = when (value) {
                            "<h1>" -> 1.5f
                            "<h2>" -> 1.3f
                            else -> 1.15f
                        }
                        sizeStack.add(output.length)
                        if (output.isNotEmpty()) output.append("\n")
                    }

                    "</h1>", "</h2>", "</h3>" -> {
                        sizeStack.removeLastOrNull()?.let { start ->
                            val end = output.length
                            output.setSpan(RelativeSizeSpan(currentSizeFactor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            output.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        currentSizeFactor = 1.0f
                    }

                    "<blockquote>" -> {
                        output.append("—————————————————")
                        blockquoteStack.add(output.length)
                    }

                    "</blockquote>" -> {
                        blockquoteStack.removeLastOrNull()?.let { start ->
                            val end = output.length
                            output.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            output.setSpan(RelativeSizeSpan(0.9f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        output.append("\n—————————————————\n")
                    }

                    "<strong>" -> boldStack.add(output.length)
                    "</strong>" -> applySpan(boldStack, output, StyleSpan(Typeface.BOLD))

                    "<em>" -> italicStack.add(output.length)
                    "</em>" -> applySpan(italicStack, output, StyleSpan(Typeface.ITALIC))


                    "<li>" -> output.append("° ")
                    "</li>" -> {}

                    "<br>" -> output.append("\n")

                    "<p>", "</p>", "<ul>", "</ul>", "<ol>", "</ol>" -> {}

                    else -> match.value.takeIf { it.isNotBlank() }?.let { output.append(it) }
                }
            }

            return output
        }

        private fun applySpan(stack: MutableList<Int>, output: SpannableStringBuilder, span: Any) {
            stack.removeLastOrNull()?.let { start ->
                output.setSpan(span, start, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}
package rules

import extensions.TechDebtExtension

// 1. Clean Code Standards
val MAX_FUNCTION_LINES = 20
val ARG_COUNT_WARNING_THRESHOLD = 3 // Triadic (Warn)
val ARG_COUNT_ERROR_THRESHOLD = 4   // Polyadic (Fail)

val techDebt = project.extensions.findByType<TechDebtExtension>()
    ?: project.extensions.create<TechDebtExtension>("techDebt")

val enforceFunctionRules by tasks.registering {
    group = "verification"
    description = "Enforces Clean Code: Small functions (< $MAX_FUNCTION_LINES lines) and limited arguments."

    val sourceFiles = fileTree("src") {
        include("**/*.kt")
        exclude("**/ui/theme/**")
        exclude("**/build/**")
        exclude("**/*Test.kt")
    }

    doLast {
        val lengthErrors = mutableListOf<String>()
        val argErrors = mutableListOf<String>()
        val argWarnings = mutableListOf<String>()

        sourceFiles.forEach { file ->
            val lines = file.readLines()
            var currentFunctionName = ""
            var currentFunctionStartLine = 0
            var braceCount = 0
            var isInsideFunction = false
            var meaningfulLinesCount = 0

            // Signature Capture Variables
            var isCollectingSignature = false
            var signatureAccumulator = StringBuilder()

            lines.forEachIndexed { index, rawLine ->
                val line = rawLine.trim()

                // Skip comments/imports
                if (line.startsWith("//") || line.startsWith("import ") || line.startsWith("package ")) return@forEachIndexed

                // --- 1. Detect Function Start ---
                val isFunctionDefinition = line.matches(Regex("^(.*\\s)?fun\\s+.*"))

                if (isFunctionDefinition && !isInsideFunction) {
                    isInsideFunction = true
                    isCollectingSignature = true
                    currentFunctionStartLine = index + 1

                    // Extract name safely considering modifiers
                    currentFunctionName = line.substringAfter("fun ").substringBefore("(")

                    braceCount = 0
                    meaningfulLinesCount = 0
                    signatureAccumulator.clear()
                }

                // --- 2. Collect Signature for Arg Counting ---
                if (isCollectingSignature) {
                    signatureAccumulator.append(" ").append(line)
                    // Stop collecting when we hit the body start ('{') or expression body ('=')
                    if (line.contains("{") || line.contains("=")) {
                        isCollectingSignature = false

                        val argsCount = countArguments(signatureAccumulator.toString())

                        if (argsCount >= ARG_COUNT_ERROR_THRESHOLD) {
                            argErrors.add(
                                "   • [Too Many Arguments] ($argsCount args)\n" +
                                        "     Name:      $currentFunctionName\n" +
                                        "     Location:  (${file.name}:$currentFunctionStartLine)"
                            )
                        } else if (argsCount == ARG_COUNT_WARNING_THRESHOLD) {
                            argWarnings.add(
                                "   • [Triadic Function] ($argsCount args)\n" +
                                        "     Name:      $currentFunctionName\n" +
                                        "     Location:  (${file.name}:$currentFunctionStartLine)"
                            )
                        }
                    }
                }

                // --- 3. Track Function Body Length ---
                if (isInsideFunction) {
                    braceCount += line.count { it == '{' }
                    braceCount -= line.count { it == '}' }

                    if (line.isNotEmpty()) {
                        meaningfulLinesCount++
                    }

                    // End of function
                    if (braceCount == 0 && (line.contains("}") || line.endsWith("="))) {
                        if (meaningfulLinesCount > MAX_FUNCTION_LINES) {
                            lengthErrors.add(
                                "   • [Complex Function] ($meaningfulLinesCount lines)\n" +
                                        "     Name:      $currentFunctionName\n" +
                                        "     Location:  (${file.name}:$currentFunctionStartLine)"
                            )
                        }
                        isInsideFunction = false
                    }
                }
            }
        }

        // --- 4. Reporting ---

        // Print Warnings (Does not fail build)
        if (argWarnings.isNotEmpty()) {
            println("\n⚠️  CLEAN CODE WARNINGS (Triadic Functions Detected):")
            argWarnings.forEach { println(it) }
            println("Consider refactoring these to use a data class or helper object.\n")
        }

        // Collect Failures
        val allFailures = lengthErrors + argErrors

        if (allFailures.isNotEmpty()) {
            val report = StringBuilder().apply {
                append("\n")
                append("╔════════════════════════════════════════════════════════════════════╗\n")
                append("║             🚫 CLEAN CODE VIOLATIONS DETECTED 🚫                   ║\n")
                append("╠════════════════════════════════════════════════════════════════════╣\n")
                append("║ MODULE:  ${project.name}\n")
                append("║\n")
                append("║ RULE 1:  Functions must be < $MAX_FUNCTION_LINES lines.\n")
                append("║ RULE 2:  Arguments: 0-2 (Good), 3 (Warn), 4+ (Fail).\n")
                append("║\n")
                append("║ \"The ideal number of arguments for a function is zero (niladic).\n")
                append("║  Next comes one (monadic), followed closely by two (dyadic).\n")
                append("║  Three arguments (triadic) should be avoided where possible.\n")
                append("║  More than three (polyadic) requires special justification\n")
                append("║  and shouldn't be used anyway.\" -- Robert C. Martin\n")
                append("╚════════════════════════════════════════════════════════════════════╝\n")
                append("\n👇 ERRORS FOUND (${allFailures.size}):\n")
                append("──────────────────────────────────────────────────────────────────────\n")
                allFailures.forEach { append(it + "\n──────────────────────────────────────────────────────────────────────\n") }
                append("\n❌ Build failed. Please refactor.\n")
            }
            throw GradleException(report.toString())
        }
    }
}

// Helper to parse argument count safely (handles generics <T, U> and parentheses nesting)
fun countArguments(signature: String): Int {
    val start = signature.indexOf('(')
    if (start == -1) return 0

    // Find the matching closing parenthesis
    var depth = 0
    var end = -1
    for (i in start until signature.length) {
        if (signature[i] == '(') depth++
        if (signature[i] == ')') depth--
        if (depth == 0) {
            end = i
            break
        }
    }

    if (end <= start + 1) return 0 // Empty parens "()"

    val argsContent = signature.substring(start + 1, end)

    // Count commas at the "top level" (ignoring commas inside <...> or (...))
    var commaCount = 0
    var genericDepth = 0
    var parenDepth = 0

    for (char in argsContent) {
        when (char) {
            '<' -> genericDepth++
            '>' -> if (genericDepth > 0) genericDepth--
            '(' -> parenDepth++
            ')' -> if (parenDepth > 0) parenDepth--
            ',' -> {
                if (genericDepth == 0 && parenDepth == 0) {
                    commaCount++
                }
            }
        }
    }

    return commaCount + 1
}

tasks.configureEach {
    val taskName = name.lowercase()
    val shouldSkip = techDebt.flags.get().contains("skipFunctionLengthCheck")

    if (!shouldSkip) {
        if ((taskName.contains("compile") && taskName.contains("kotlin")) || taskName == "prebuild") {
            dependsOn(enforceFunctionRules)
        }
    }
}
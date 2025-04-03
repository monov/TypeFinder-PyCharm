package org.jetbrains.plugins.TypeFinder.utils

fun determineVariableType(value: String): String {
    return when {
        value.startsWith("\"") && value.endsWith("\"") -> "str"
        value.startsWith("'") && value.endsWith("'") -> "str"
        value.matches(Regex("^-?\\d+[+-]\\d+j$")) -> "complex"
        value.matches(Regex("^-?\\d*j$")) -> "complex"
        value.matches(Regex("^-?\\d+$")) -> "int"
        value.matches(Regex("^-?\\d+\\.\\d+$")) -> "float"
        value == "True" || value == "False" -> "bool"
        value == "None" -> "NoneType"
        value.startsWith("[") && value.endsWith("]") -> "list"
        value.startsWith("(") && value.endsWith(")") -> "tuple"
        value.startsWith("{") && value.endsWith("}") -> {
            if (value == "{}") "dict"  // Empty dict by default
            else if (":" in value) "dict" else "set"
        }
        value.startsWith("range(") && value.endsWith(")") -> "range"
        value.startsWith("set(") && value.endsWith(")") -> "set"  // Special case for set()
        value.startsWith("sum(") && value.endsWith(")") -> "int"  // Special case for sum()
        value.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*\\(.*\\)$")) -> {
            // For other function calls, try to determine a reasonable default type
            when {
                value.startsWith("list(") -> "list"
                value.startsWith("tuple(") -> "tuple"
                value.startsWith("dict(") -> "dict"
                value.startsWith("int(") -> "int"
                value.startsWith("float(") -> "float"
                value.startsWith("str(") -> "str"
                value.startsWith("bool(") -> "bool"
                else -> "Unknown"  // For other functions we can't determine
            }
        }
        else -> "Unknown"
    }
}
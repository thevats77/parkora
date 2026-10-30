package com.example.parkora.utils


/*
 * Form validation helpers.
 * Each function returns an ERROR MESSAGE (String) when the input is wrong,
 * or null when the input is OK. The screens show the message under the field.
 */
object Validation {

    private val EMAIL_PATTERN = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")

    fun validateName(name: String): String? = when {
        name.isBlank() -> "Please enter your name"
        name.trim().length < 3 -> "Name must have at least 3 characters"
        else -> null
    }

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Please enter your email"
        !EMAIL_PATTERN.matches(email.trim()) -> "Please enter a valid email address"
        else -> null
    }

    fun validatePhone(phone: String): String? = when {
        phone.isBlank() -> "Please enter your phone number"
        phone.length != 10 || !phone.all { it.isDigit() } -> "Phone number must be exactly 10 digits"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Please enter a password"
        password.length < 6 -> "Password must have at least 6 characters"
        else -> null
    }

    /** For simple text boxes that only need "not empty". */
    fun required(value: String, fieldName: String): String? =
        if (value.isBlank()) "Please enter $fieldName" else null

    /** For number boxes such as price, length, width. */
    fun positiveNumber(value: String, fieldName: String): String? {
        val number = value.toIntOrNull()
        return when {
            value.isBlank() -> "Please enter $fieldName"
            number == null -> "$fieldName must be a whole number"
            number <= 0 -> "$fieldName must be greater than 0"
            else -> null
        }
    }
}

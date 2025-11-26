package uk.ac.tees.mad.savesmart.data.model


import kotlinx.serialization.Serializable

@Serializable
data class CurrencyConversionResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

data class ConversionState(
    val isLoading: Boolean = false,
    val convertedAmount: Double? = null,
    val targetCurrency: String = "USD",
    val error: String? = null
)
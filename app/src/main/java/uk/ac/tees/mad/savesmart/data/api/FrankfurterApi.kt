package uk.ac.tees.mad.savesmart.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import uk.ac.tees.mad.savesmart.data.model.CurrencyConversionResponse

interface FrankfurterApi {

    @GET("latest")
    suspend fun convertCurrency(
        @Query("amount") amount: Double,
        @Query("from") fromCurrency: String,
        @Query("to") toCurrency: String
    ): CurrencyConversionResponse

    companion object {
        const val BASE_URL = "https://api.frankfurter.dev/v1/"
    }
}
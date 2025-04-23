package com.example.koktajlista

data class DrinkStruct(
    val drinkName: String,
    val drinkImage: ByteArray,
    val drinkId: Int,
    val strImageSource: String?,
    val strImageAttribution: String?,
    val strCreativeCommonsConfirmed: String,
    val dateModified: String,
    val ingredients: List<String>,
    val measure: List<String>,
    val instructions: Map<String, String?>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DrinkStruct

        if (drinkId != other.drinkId) return false
        if (drinkName != other.drinkName) return false
        if (!drinkImage.contentEquals(other.drinkImage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = drinkId
        result = 31 * result + drinkName.hashCode()
        result = 31 * result + drinkImage.contentHashCode()
        return result
    }
}

data class Category(
    val strCategory: String
)

data class CategoryResponse(
    val drinks: List<Category>
)

data class Glass(
    val strGlass: String
)

data class GlassResponse(
    val drinks: List<Category>
)

data class Ingredient(
    val strIngredient1: String
)

data class IngredientResponse(
    val drinks: List<Category>
)

data class Alcoholic(
    val strAlcoholic: String
)

data class AlcoholicResponse(
    val drinks: List<Category>
)

data class DrinkResponse(
    val drinks: List<ShortDrink>
)

data class ShortDrink(
    val strDrink: String,
    val strDrinkThumb: String,
    val idDrink: Int
)

data class LongDrinkResponse(
    val drinks: List<Drink>
)

data class Drink(
    val idDrink: Int,
    val strDrink: String,
    val strDrinkAlternate: String,
    val strTags: String,
    val strVideo: String,
    val strCategory: String,
    val strIBA: String,
    val strAlcoholic: String,
    val strGlass: String,
    val strInstructions: String?,
    val strInstructionsES: String?,
    val strInstructionsDE: String?,
    val strInstructionsFR: String?,
    val strInstructionsIT: String?,
    val strInstructionsZHHANS: String?,
    val strInstructionsZHHANT: String?,
    val strDrinkThumb: String,
    val strIngredient1: String,
    val strIngredient2: String,
    val strIngredient3: String,
    val strIngredient4: String,
    val strIngredient5: String,
    val strIngredient6: String,
    val strIngredient7: String,
    val strIngredient8: String,
    val strIngredient9: String,
    val strIngredient10: String,
    val strIngredient11: String,
    val strIngredient12: String,
    val strIngredient13: String,
    val strIngredient14: String,
    val strIngredient15: String,
    val strMeasure1: String,
    val strMeasure2: String,
    val strMeasure3: String,
    val strMeasure4: String,
    val strMeasure5: String,
    val strMeasure6: String,
    val strMeasure7: String,
    val strMeasure8: String,
    val strMeasure9: String,
    val strMeasure10: String,
    val strMeasure11: String,
    val strMeasure12: String,
    val strMeasure13: String,
    val strMeasure14: String,
    val strMeasure15: String,
    val strImageSource: String,
    val strImageAttribution: String,
    val strCreativeCommonsConfirmed: String,
    val dateModified: String
)
package fr.tse.fise2.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

public class NutritionInfo {
    private int calories;
    private int protein;
    private int carbohydrates;
    private int fat;

    public NutritionInfo() {
        this.calories = 0;
        this.protein = 0;
        this.carbohydrates = 0;
        this.fat = 0;
    }

    public void fetchFromAPI(int recipeId, String apiKey) {
        if (recipeId <= 0 || apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Recipe ID or API key cannot be null or empty.");
        }
        
        try {
            String recipeInfoUrl = "https://api.spoonacular.com/recipes/" + recipeId + "/information?apiKey=" + apiKey + "&includeNutrition=true";
            URL recipeInfoURL = new URL(recipeInfoUrl);
            HttpURLConnection recipeInfoConn = (HttpURLConnection) recipeInfoURL.openConnection();
            recipeInfoConn.setRequestMethod("GET");

            BufferedReader recipeInfoReader = new BufferedReader(new InputStreamReader(recipeInfoConn.getInputStream()));
            StringBuilder recipeInfoResponse = new StringBuilder();
            String recipeInfoLine;
            while ((recipeInfoLine = recipeInfoReader.readLine()) != null) {
                recipeInfoResponse.append(recipeInfoLine);
            }
            recipeInfoReader.close();

            JSONObject jsonRecipeInfoResponse = new JSONObject(recipeInfoResponse.toString());

            if (jsonRecipeInfoResponse.has("nutrition")) {
                JSONObject nutritionObject = jsonRecipeInfoResponse.getJSONObject("nutrition");

                JSONObject caloricBreakdown = nutritionObject.getJSONObject("caloricBreakdown");
                double percentCarbs = caloricBreakdown.getDouble("percentCarbs");
                double percentProtein = caloricBreakdown.getDouble("percentProtein");
                double percentFat = caloricBreakdown.getDouble("percentFat");

                JSONObject weightPerServing = nutritionObject.getJSONObject("weightPerServing");
                int amount = weightPerServing.getInt("amount");
                String unit = weightPerServing.getString("unit");

                JSONArray ingredientsArray = nutritionObject.getJSONArray("ingredients");
                for (int i = 0; i < ingredientsArray.length(); i++) {
                    JSONObject ingredient = ingredientsArray.getJSONObject(i);
                    // Process each ingredient as needed...
                }

                // Print or use the extracted nutrition data here as needed
                System.out.println("Percent Carbs: " + percentCarbs);
                System.out.println("Percent Protein: " + percentProtein);
                System.out.println("Percent Fat: " + percentFat);
                System.out.println("Weight Per Serving: " + amount + " " + unit);
                
                // Continue extracting other necessary information...

            } else {
                System.out.println("Nutritional values not found in API response.");
                resetNutritionalValues();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void resetNutritionalValues() {
        this.calories = 0;
        this.protein = 0;
        this.carbohydrates = 0;
        this.fat = 0;
    }

    public int getCalories() {
        return calories;
    }

    public int getProtein() {
        return protein;
    }

    public int getCarbohydrates() {
        return carbohydrates;
    }

    public int getFat() {
        return fat;
    }
}
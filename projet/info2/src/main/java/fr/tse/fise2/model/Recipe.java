package fr.tse.fise2.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Recipe {
    private String title;
    private int recipeId;
    private List<Ingredient> ingredients;
    private String instructions;
    private String urlImage;

    public Recipe(String title, int recipeId, List<Ingredient> ingredients, String instructions, String urlImage) {
        this.title = title;
        this.recipeId = recipeId;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.urlImage = urlImage;
    }
    


    // Getters et setters pour tous les attributs (title, recipeId, ingredients, instructions)

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public void setIngredients(List<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}

	// Méthode pour afficher les détails de la recette
    public void displayRecipeDetails() {
        System.out.println("Recipe Title: " + title);
        System.out.println("Recipe ID: " + recipeId);
        System.out.println("Ingredients:");

        for (Ingredient ingredient : ingredients) {
            System.out.println("- " + ingredient.getName() + ": " + ingredient.getQuantity() + " " + ingredient.getExpirationDate());
        }

        System.out.println("Instructions:");
        System.out.println(instructions);
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }
    // Dans la classe Ingredient
    public static void sortIngredientsByExpiration(List<Ingredient> ingredients) {
        Collections.sort(ingredients, Comparator.comparing(
                ingredient -> {
                    if (ingredient.getExpirationDate() == null || ingredient.getExpirationDate().isEmpty()) {
                        return LocalDate.MIN;
                    }
                    return LocalDate.parse(ingredient.getExpirationDate());
                },
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
    }

    private static LocalDate getEarliestExpirationDate(List<Ingredient> ingredients) {
        if (ingredients.isEmpty()) {
            return null;
        }
        return ingredients.stream()
                .map(Ingredient::getExpirationDate)
                .map(LocalDate::parse)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.MIN);
    }
    public static void getIngredientsByRecipeName(String recipeName, String apiKey) {
        try {
            // Recherche de l'ID de la recette en fonction de son nom
            String encodedRecipeName = URLEncoder.encode(recipeName, "UTF-8");
            String searchRecipeURL = "https://api.spoonacular.com/recipes/search?query=" + encodedRecipeName + "&apiKey=" + apiKey;

            URL recipeURL = new URL(searchRecipeURL);
            HttpURLConnection recipeConn = (HttpURLConnection) recipeURL.openConnection();
            recipeConn.setRequestMethod("GET");

            BufferedReader recipeIn = new BufferedReader(new InputStreamReader(recipeConn.getInputStream()));
            String recipeInputLine;
            StringBuilder recipeResponse = new StringBuilder();

            while ((recipeInputLine = recipeIn.readLine()) != null) {
                recipeResponse.append(recipeInputLine);
            }
            recipeIn.close();

            JSONObject recipeObject = new JSONObject(recipeResponse.toString());
            JSONArray recipesArray = recipeObject.getJSONArray("results");

            // Obtention du premier ID de recette correspondant au nom donné
            if (recipesArray.length() > 0) {
                JSONObject firstRecipe = recipesArray.getJSONObject(0);
                int recipeId = firstRecipe.getInt("id");

                // Utilisation de l'ID pour obtenir les ingrédients de la recette
                String searchIngredientsURL = "https://api.spoonacular.com/recipes/" + recipeId + "/ingredientWidget.json?apiKey=" + apiKey;

                URL ingredientsURL = new URL(searchIngredientsURL);
                HttpURLConnection ingredientsConn = (HttpURLConnection) ingredientsURL.openConnection();
                ingredientsConn.setRequestMethod("GET");

                BufferedReader ingredientsIn = new BufferedReader(new InputStreamReader(ingredientsConn.getInputStream()));
                String ingredientsInputLine;
                StringBuilder ingredientsResponse = new StringBuilder();

                while ((ingredientsInputLine = ingredientsIn.readLine()) != null) {
                    ingredientsResponse.append(ingredientsInputLine);
                }
                ingredientsIn.close();

                JSONObject recipeIngredients = new JSONObject(ingredientsResponse.toString());
                JSONArray ingredientsArray = recipeIngredients.getJSONArray("ingredients");

                System.out.println("Recipe Ingredients \"" + recipeName + "\":");
                for (int i = 0; i < ingredientsArray.length(); i++) {
                    JSONObject ingredientObject = ingredientsArray.getJSONObject(i);
                    String ingredientName = ingredientObject.getString("name");
                    System.out.println("- " + ingredientName);
                }
            }
        }catch (Exception e) {
        	System.err.println("Error API");
            }
        }

    public static List<Recipe> getRecipesByIngredientsAndSortingOption(List<Ingredient> ingredientNames, String apiKey, String sortingOption) {
        List<Recipe> recipes = new ArrayList<>();

        try {
            StringBuilder ingredientList = new StringBuilder();
            for (Ingredient ingredientName : ingredientNames) {
                if (ingredientList.length() > 0) {
                    ingredientList.append(",");
                }
                ingredientList.append(URLEncoder.encode(ingredientName.getName(), "UTF-8"));
            }

            String encodedIngredients = ingredientList.toString();
            String searchURL = getSearchURL(encodedIngredients, apiKey, sortingOption);
            System.out.println(searchURL);

            URL url = new URL(searchURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (response.length() > 0 && response.charAt(0) == '[') {
                // Le format de la réponse est une liste de résultats
                JSONArray resultsArray = new JSONArray(response.toString());
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject recipeObject = resultsArray.getJSONObject(i);
                    String recipeTitle = recipeObject.getString("title");
                    int recipeId = recipeObject.getInt("id");
                    List<Ingredient> recipeIngredients = getIngredientsByRecipeId(recipeId, apiKey);
                    String instructions = getRecipeInstructionsById(recipeId, apiKey);

                    // Récupération de l'URL de l'image
                    String imageUrl = recipeObject.getString("image");

                    Recipe recipe = new Recipe(recipeTitle, recipeId, recipeIngredients, instructions,imageUrl);
                    recipe.setUrlImage(imageUrl);
                    recipes.add(recipe);
                }
            } else if (response.length() > 0 && response.charAt(0) == '{') {
                try {
                    JSONObject responseJson = new JSONObject(response.toString());
                    if (responseJson.has("results") && !responseJson.isNull("results")) {
                        JSONArray recipesArray = responseJson.getJSONArray("results");
                        if (recipesArray.length() > 0) {
                            // Traitement du tableau JSON
                            for (int i = 0; i < recipesArray.length(); i++) {
                                JSONObject recipeObject = recipesArray.getJSONObject(i);
                                String recipeTitle = recipeObject.getString("title");
                                int recipeId = recipeObject.getInt("id");
                                List<Ingredient> recipeIngredients = getIngredientsByRecipeId(recipeId, apiKey);
                                String instructions = getRecipeInstructionsById(recipeId, apiKey);

                                // Récupération de l'URL de l'image
                                String imageUrl = recipeObject.getString("image");

                                Recipe recipe = new Recipe(recipeTitle, recipeId, recipeIngredients, instructions,imageUrl);
                                recipe.setUrlImage(imageUrl);
                                recipes.add(recipe);
                            }
                        } else {
                            // Aucun résultat trouvé
                            // JOptionPane.showMessageDialog(Recipe.this, "Sorry, we couldn't find a recipe for you in our database, modify your ingredients list", "Avertissement", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        // Gérer le cas où la propriété "results" est absente ou est null
                        System.out.println("The 'results' property is not present in the JSON response or is null.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Gérer l'exception JSON
                }
            } else {
                System.err.println("Format de réponse non pris en charge");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Error API");
        }

        return recipes;
    }

    private static String getSearchURL(String encodedIngredients, String apiKey, String sortingOption) {
        // Logique pour obtenir l'URL en fonction de l'option de tri
        if ("max-used-ingredients".equals(sortingOption)) {
            return "https://api.spoonacular.com/recipes/complexSearch"
                    + "?includeIngredients=" + encodedIngredients
                    + "&sort=max-used-ingredients"
                    + "&apiKey=" + apiKey;
        } else if ("min-missing-ingredients".equals(sortingOption)) {
            return "https://api.spoonacular.com/recipes/complexSearch"
                    + "?includeIngredients=" + encodedIngredients
                    + "&sort=min-missing-ingredients"
                    + "&apiKey=" + apiKey;
        } else {
            return "https://api.spoonacular.com/recipes/findByIngredients?ingredients=" + encodedIngredients + "&apiKey=" + apiKey;
        }
    }

    
    private static List<Ingredient> getIngredientsByRecipeId(int recipeId2, String apiKey) {
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            String searchIngredientsURL = "https://api.spoonacular.com/recipes/" + recipeId2 + "/ingredientWidget.json?apiKey=" + apiKey;

            URL ingredientsURL = new URL(searchIngredientsURL);
            HttpURLConnection ingredientsConn = (HttpURLConnection) ingredientsURL.openConnection();
            ingredientsConn.setRequestMethod("GET");

            BufferedReader ingredientsIn = new BufferedReader(new InputStreamReader(ingredientsConn.getInputStream()));
            String ingredientsInputLine;
            StringBuilder ingredientsResponse = new StringBuilder();

            while ((ingredientsInputLine = ingredientsIn.readLine()) != null) {
                ingredientsResponse.append(ingredientsInputLine);
            }
            ingredientsIn.close();

            JSONObject recipeIngredients = new JSONObject(ingredientsResponse.toString());
            JSONArray ingredientsArray = recipeIngredients.getJSONArray("ingredients");

            for (int i = 0; i < ingredientsArray.length(); i++) {
                JSONObject ingredientObject = ingredientsArray.getJSONObject(i);
                String ingredientName = ingredientObject.getString("name");

                // Accéder à la propriété "amount" qui est un objet JSON
                JSONObject amountObject = ingredientObject.getJSONObject("amount");

                // Accéder aux propriétés "metric" et "us" à l'intérieur de l'objet "amount"
                JSONObject metricObject = amountObject.getJSONObject("metric");
                JSONObject usObject = amountObject.getJSONObject("us");

                // Accéder aux propriétés "value" et "unit" à l'intérieur des objets "metric" et "us"
                double metricValue = metricObject.getDouble("value");
                String metricUnit = metricObject.getString("unit");
                String quantity =Double.toString(metricValue)+" "+metricUnit;


                // Ajouter l'ingrédient à la liste
                Ingredient ingredient = new Ingredient(ingredientName, quantity);
                ingredients.add(ingredient);
            }
        } catch (Exception e) {
        	System.err.println("Error API");
        }

        return ingredients;
    }


    private static String getRecipeInstructionsById(int recipeId2, String apiKey) {
        String instructions = "";

        try {
            String recipeInfoURL = "https://api.spoonacular.com/recipes/" + recipeId2 + "/information?apiKey=" + apiKey;

            URL infoURL = new URL(recipeInfoURL);
            HttpURLConnection infoConn = (HttpURLConnection) infoURL.openConnection();
            infoConn.setRequestMethod("GET");

            BufferedReader infoIn = new BufferedReader(new InputStreamReader(infoConn.getInputStream()));
            String infoInputLine;
            StringBuilder infoResponse = new StringBuilder();

            while ((infoInputLine = infoIn.readLine()) != null) {
                infoResponse.append(infoInputLine);
            }
            infoIn.close();

            JSONObject recipeInfo = new JSONObject(infoResponse.toString());
            // Vérifier si le champ "instructions" existe et est une chaîne
            if (recipeInfo.has("instructions") && !recipeInfo.isNull("instructions")) {
            	// Utiliser Jsoup pour supprimer les balises HTML
                String rawInstructions = recipeInfo.getString("instructions");
                Document doc = Jsoup.parse(rawInstructions);
                instructions = doc.text();            } else {
                // Gérer le cas où le champ n'est pas une chaîne ou est manquant
                instructions = "Instructions not available in API ";
            }
        } catch (Exception e) {
        	System.err.println("Error API");
        }

        return instructions;
    }


  
    
    public static void getRecipeInformation(int recipeId, String apiKey) {
        try {
            String recipeInfoURL = "https://api.spoonacular.com/recipes/" + recipeId + "/information?apiKey=" + apiKey;

            URL infoURL = new URL(recipeInfoURL);
            HttpURLConnection infoConn = (HttpURLConnection) infoURL.openConnection();
            infoConn.setRequestMethod("GET");

            BufferedReader infoIn = new BufferedReader(new InputStreamReader(infoConn.getInputStream()));
            String infoInputLine;
            StringBuilder infoResponse = new StringBuilder();

            while ((infoInputLine = infoIn.readLine()) != null) {
                infoResponse.append(infoInputLine);
            }
            infoIn.close();

            JSONObject recipeInfo = new JSONObject(infoResponse.toString());

            // Afficher les détails de la recette
            System.out.println("Recipe information: ");
            System.out.println("Title: " + recipeInfo.getString("title"));

            // Afficher les ingrédients
            JSONArray extendedIngredients = recipeInfo.getJSONArray("extendedIngredients");
            System.out.println("Ingredients:");
            for (int i = 0; i < extendedIngredients.length(); i++) {
                JSONObject ingredient = extendedIngredients.getJSONObject(i);
                String ingredientName = ingredient.getString("name");
                double amount = ingredient.getDouble("amount");
                String unit = ingredient.getString("unit");
                System.out.println("- " + ingredientName + ": " + amount + " " + unit);
            }

          
        } catch (Exception e) {
        	System.err.println("Error API");
        }
    }


    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String imageUrl) {
        this.urlImage = imageUrl;
    }

}

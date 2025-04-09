package fr.tse.fise2.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Ingredient {
	private int ID_Ingredient;
	private String name;
    private String quantity;
    private String expirationDate;
    private alertCouleur alert;

    // Constructeur avec tous les attributs
    public Ingredient(int ID_Ingredient, String name, String quantity, String expirationDate) {
    	this.ID_Ingredient = ID_Ingredient;
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        setColor();
    }
    
    public Ingredient(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;

    }
    
    public alertCouleur getAlert() {
        return alert;
    }

    public void setColor() {
    	if (expirationDate == null) {
            // Handle the case where expirationDate is null
            alert = alertCouleur.DEFAULT_COLOR;
            return;
        }
    	
    	LocalDate currentDate = LocalDate.now();
        LocalDate expirationLocalDate = LocalDate.parse(expirationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        long daysUntilExpiration = currentDate.until(expirationLocalDate).getDays();

        if (daysUntilExpiration < 4) {
        	alert = alertCouleur.RED;
        	
        } else if (daysUntilExpiration >= 4 && daysUntilExpiration <= 6) {
        	alert = alertCouleur.ORANGE;
        } else {
        	alert = alertCouleur.GREEN;
        }
    }
    
    public static void sortIngredientsByExpiration(List<Ingredient> ingredients) {
        Comparator<Ingredient> comparator = Comparator.comparing(
                ingredient -> {
                    String expirationDate = ingredient.getExpirationDate();
                    return expirationDate != null ? LocalDate.parse(expirationDate) : null;
                },
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        Collections.sort(ingredients, comparator);
    }


    // Getters
    public String getName() {
        return name;
    }
    
    public int getID_Ingredient() {
		return ID_Ingredient;
	}

    public String getQuantity() {
        return quantity;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
   

    public void setID_Ingredient(int iD_Ingredient) {
		ID_Ingredient = iD_Ingredient;
	}

	public void getIngredientInfo(String apiKey) {
        try {
            String encodedName = URLEncoder.encode(name, "UTF-8");
            String searchURL = "https://api.spoonacular.com/food/ingredients/search?query=" + encodedName + "&apiKey=" + apiKey;
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

            // Lecture de la réponse JSON pour obtenir les informations sur l'ingrédient
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray results = jsonObject.getJSONArray("results");

            // Obtention des informations du premier ingrédient (si disponible)
            if (results.length() > 0) {
                JSONObject firstResult = results.getJSONObject(0);
                int ingredientId = firstResult.getInt("id");
                String ingredientName = firstResult.getString("name");

                // Utilisation de l'ID pour obtenir les détails de l'ingrédient spécifique
                String ingredientDetailsURL = "https://api.spoonacular.com/food/products/" + ingredientId + "?apiKey=" + apiKey;

                // Requête pour obtenir les informations détaillées de l'ingrédient, y compris les détails nutritionnels
                URL detailsUrl = new URL(ingredientDetailsURL);
                HttpURLConnection detailsConn = (HttpURLConnection) detailsUrl.openConnection();
                detailsConn.setRequestMethod("GET");

                BufferedReader detailsIn = new BufferedReader(new InputStreamReader(detailsConn.getInputStream()));
                String inputLine1;
                StringBuilder detailsResponse = new StringBuilder();
                while ((inputLine1 = detailsIn.readLine()) != null) {
                    detailsResponse.append(inputLine1);
                }
                detailsIn.close();

                JSONObject detailsObject = new JSONObject(detailsResponse.toString());

                // Affichage des informations obtenues
                //System.out.println("Ingredient ID: " + ingredientId);
                System.out.println("Ingredient Name: " + ingredientName);
                System.out.println("Quantity: " + quantity);
                System.out.println("Expiration Date: " + expirationDate);

                // Récupération des détails nutritionnels
                if (detailsObject.has("nutrition")) {
                    JSONObject nutritionObject = detailsObject.getJSONObject("nutrition");
                    JSONArray nutrientsArray = nutritionObject.getJSONArray("nutrients");

                    // Affichage des informations nutritionnelles
                    System.out.println("Nutritional Information:");
                    for (int i = 0; i < nutrientsArray.length(); i++) {
                        JSONObject nutrient = nutrientsArray.getJSONObject(i);
                        String nutrientName = nutrient.getString("name");
                        double amount = nutrient.getDouble("amount");
                        String unit = nutrient.getString("unit");
                        System.out.println("- " + nutrientName + ": " + amount + " " + unit);
                    }
                } else {
                    System.out.println("Nutritional information not available for this ingredient.");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package fr.tse.fise2.view;

import javax.swing.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.Recipe;
import fr.tse.fise2.model.User;

public class IntolerencesView extends JFrame {
	private List<String> selectedIntolerances = new ArrayList<>();
	private JLabel selectedIntolerancesLabel;
    private User user;
    private MenuView menuView;
	private BotButtonsManager botButtonsManager;

    public IntolerencesView(User user_,MenuView menuView) {
        this.user = user_;
        this.menuView = menuView;
        botButtonsManager = new BotButtonsManager(this,menuView);
        JPanel botPanel = botButtonsManager.getButtonsPanel();
        botPanel.setOpaque(false); // Pour rendre le JPanel transparent
        botPanel.setBounds(180, 680, 800, 100);
       
        // Configurer la fenêtre principale
        setTitle("Intolerances");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        // Créer un JPanel pour contenir les composants
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.add(botPanel); // Ajoutez le JPanel des boutons gérés par BotButtonsManager à mainPanel

        BackgroundView backgroundView = new BackgroundView(user, this, menuView.getIsAliceMode());
        JPanel backPanel = backgroundView.getBackgroundView();
        backPanel.setOpaque(false); // Pour rendre le JPanel transparent
        backPanel.setBounds(0, 0, getWidth(), getHeight());
        
     // Créer le JScrollPane au début sans recettes
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBounds(30, 170, 1200, 500);
        

        // Créer une liste déroulante pour les intolérances
        List<String> intoleranceList = new ArrayList<>();
        intoleranceList.add("Dairy");
        intoleranceList.add("Egg");
        intoleranceList.add("Gluten");
        intoleranceList.add("Grain");
        intoleranceList.add("Peanut");
        intoleranceList.add("Seafood");
        intoleranceList.add("Sesame");
        intoleranceList.add("Shellfish");
        intoleranceList.add("Soy");
        intoleranceList.add("Sulfite");
        intoleranceList.add("Tree Nut");
        intoleranceList.add("Wheat");
        selectedIntolerancesLabel = new JLabel("Selected intolerances: ");
        selectedIntolerancesLabel.setBounds(200, 140, 400, 30);
        mainPanel.add(selectedIntolerancesLabel);

        JComboBox<String> intoleranceComboBox = new JComboBox<>(intoleranceList.toArray(new String[0]));
        intoleranceComboBox.setBounds(200, 100, 150, 30);
        mainPanel.add(intoleranceComboBox);

        JButton addButton = new JButton("Add intolerance");
        addButton.setBounds(470, 100, 180, 30);
        mainPanel.add(addButton);
        
     // Créer un bouton pour supprimer la tolérance sélectionnée
        JButton removeIntoleranceButton = new JButton("Remove intolerance");
        removeIntoleranceButton.setBounds(890, 100, 180, 30);
        mainPanel.add(removeIntoleranceButton);

        
        // Créer un bouton pour déclencher la recherche des recettes
        JButton searchButton = new JButton("Search Recipes");
        searchButton.setBounds(680, 100, 180, 30);
        mainPanel.add(searchButton);
        
        // Charger les intolérances de l'utilisateur depuis la base de données
        selectedIntolerances.addAll(DataBaseConnection.getIntolerances(user.getUserID()));
        updateSelectedIntolerancesLabel(selectedIntolerancesLabel, selectedIntolerances);

        
        
        removeIntoleranceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!selectedIntolerances.isEmpty()) {
                    // Supprimer la dernière tolérance ajoutée
                    String removedIntolerance = selectedIntolerances.remove(selectedIntolerances.size() - 1);

                    // Mettre à jour le libellé ou la logique d'affichage des intolérances
                    updateSelectedIntolerancesLabel(selectedIntolerancesLabel, selectedIntolerances);

                    // Supprimer l'intolérance de la base de données
                    DataBaseConnection.removeIntolerance(user.getUserID(), removedIntolerance);
                } else {
                    // Afficher un message indiquant que la liste est déjà vide
                    JOptionPane.showMessageDialog(IntolerencesView.this, "List already empty", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        
        
        
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedIntolerance = (String) intoleranceComboBox.getSelectedItem();
                if (!selectedIntolerances.contains(selectedIntolerance)) {
                    selectedIntolerances.add(selectedIntolerance);
                    updateSelectedIntolerancesLabel(selectedIntolerancesLabel, selectedIntolerances);
               
                    // Ajouter l'intolérance à la base de données
                    DataBaseConnection.addIntolerance(user.getUserID(), selectedIntolerance);

                }
            }
        });


        
        
        // Ajouter un écouteur d'événements pour le bouton
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Recréer le JScrollPane avec les nouvelles recettes
                scrollPane.setViewportView(createRecipesPanel(selectedIntolerances));

                
                // Mettre à jour le panneau principal
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
        
        mainPanel.add(scrollPane);
        mainPanel.add(backPanel);
        add(mainPanel);

        // Rendre la fenêtre visible
        setVisible(true);
    }

    


    private void updateSelectedIntolerancesLabel(JLabel label, List<String> selectedIntolerances) {
        StringBuilder text = new StringBuilder("Selected intolerances: ");
        for (String intolerance : selectedIntolerances) {
            text.append(intolerance).append(", ");
        }
        // Supprimer la virgule finale si la liste n'est pas vide
        if (!selectedIntolerances.isEmpty()) {
            text.delete(text.length() - 2, text.length());
        }
        label.setText(text.toString());
    }




    protected Component createRecipesPanel(List<String> selectedIntolerances) {
        JPanel recipesPanel = new JPanel();
        recipesPanel.setLayout(new BoxLayout(recipesPanel, BoxLayout.Y_AXIS));

        // Récupérer la liste des recettes possibles avec les ingrédients actuels
        List<Ingredient> ingredients = DataBaseConnection.getListeFrigo(user.getUserID());

        String apiKey = "f600b47dce1d4fc7934ad75b0a309a44";
        List<Recipe> recipes = getRecipesByIngredientsListAndIntolerancesList(ingredients, selectedIntolerances, apiKey);
        if (recipes.isEmpty()) { // Affiche un message d'information si la liste est vide
            JOptionPane.showMessageDialog(this, "\r\n"
                    + "The maximum number of API requests has been reached. Please try again tomorrow.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Ajouter chaque recette à la liste déroulante
        for (Recipe recipe : recipes) {
            JPanel recipePanel = createRecipePanel(recipe);
            recipesPanel.add(recipePanel);
        }


        return recipesPanel;
    }


	private JPanel createRecipePanel(Recipe recipe) {
	        JPanel recipePanel = new JPanel();
	        recipePanel.setLayout(null);
	        recipePanel.setSize(new Dimension(1140, 120));
	        recipePanel.setPreferredSize(new Dimension(1140, 120));

	        // Créer des JLabel pour afficher les détails de la recette
	        JLabel titleLabel = new JLabel(recipe.getTitle());
	        titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
	        titleLabel.setSize(new Dimension(500, 50));
	        titleLabel.setPreferredSize(new Dimension(500, 50));
	        titleLabel.setBounds(10, 10, 500, 50);

	        // Ajouter les JLabel au panel de la recette
	        recipePanel.add(titleLabel);
	        // Ajouter un bouton pour ajouter la recette à la liste de courses
	        JButton shoppingListButton = new JButton("Add to Shopping List");
	        shoppingListButton.setSize(new Dimension(160, 50));
	        shoppingListButton.setPreferredSize(new Dimension(160, 50));
	        shoppingListButton.setBounds(480, 10, 160, 50);
	        shoppingListButton.setForeground(new Color(37,138,184));
	        shoppingListButton.setFont(new Font("Comic Sans MS", Font.BOLD, 10));

	        // Ajouter un bouton pour afficher les détails de la recette
	        JButton detailsButton = new JButton("Details");
	        detailsButton.setSize(new Dimension(100, 50));
	        detailsButton.setPreferredSize(new Dimension(100, 50));
	        detailsButton.setBounds(670, 10, 100, 50);
	        detailsButton.setForeground(new Color(37,138,184));
	        detailsButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
	        
	     // Ajouter un bouton pour ajouter la recette aux favoris
	        JButton favoriteButton = new JButton("Favorite");
	        favoriteButton.setSize(new Dimension(100, 50));
	        favoriteButton.setPreferredSize(new Dimension(100, 50));
	        favoriteButton.setBounds(800, 10, 100, 50);
	        favoriteButton.setForeground(new Color(37,138,184));
	        favoriteButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
	        
	        
	        JLabel recipeImageLabel;
		    recipeImageLabel = new JLabel();
		    int imageWidth = 200; 
		    int imageHeight = 100;
		    int imageX = 920;
		    int imageY = 10 ;

		    recipeImageLabel.setBounds(imageX, imageY, imageWidth, imageHeight);
		    recipePanel.add(recipeImageLabel);

	        
	        // Mettez à jour l'image lorsque vous avez l'URL de l'image
	        String imageUrl = recipe.getUrlImage();
	        if (imageUrl != null && !imageUrl.isEmpty()) {
	            ImageIcon imageIcon = null;
				try {
					imageIcon = new ImageIcon(new URL(imageUrl));

			        // Redimensionner l'image tout en maintenant les proportions d'origine
			        Image originalImage = imageIcon.getImage();
			        int targetWidth = 180; // Ajustez la largeur souhaitée
			        int targetHeight = -1; // Utilisez -1 pour maintenir les proportions d'origine
			        Image resizedImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

			        // Créer une nouvelle ImageIcon avec l'image redimensionnée
			        ImageIcon resizedImageIcon = new ImageIcon(resizedImage);

			        // Mettez à jour le JLabel avec la nouvelle ImageIcon
			        recipeImageLabel.setIcon(resizedImageIcon);
			    } catch (MalformedURLException e) {
			        e.printStackTrace();
				}
	        }


	        shoppingListButton.addActionListener(e -> {
	            // Ajouter les ingrédients manquants à la liste de courses
	            addToShoppingList(recipe);
	        });
	        detailsButton.addActionListener(e -> {
	        	new RecipeView(user, recipe,menuView);
	        	dispose();
	        });
	        
	        favoriteButton.addActionListener(e -> {
	        	DataBaseConnection.addRecipe(user.getUserID(), recipe.getRecipeId(), recipe.getTitle(), recipe.getInstructions(), recipe.getIngredients(), recipe.getUrlImage());
	// DEBUG erreur unité
//	        	for (int i = 0; i < recipe.getIngredients().size(); i++) {
//	        	System.out.println(recipe.getIngredients().get(i).getName() + ": "
//	                    + recipe.getIngredients().get(i).getQuantity() + " "
//	        			+ recipe.getIngredients().get(i).getExpirationDate());}
	        });
	        
	        
	        recipePanel.add(shoppingListButton);
	        recipePanel.add(detailsButton);
	        recipePanel.add(favoriteButton);

	        return recipePanel;
	    }

    private void addToShoppingList(Recipe recipe) {
        // Comparer les ingrédients de la recette avec ceux dans le frigo
        List<Ingredient> fridgeIngredients = DataBaseConnection.getListeFrigo(user.getUserID());
        List<Ingredient> missingIngredients = new ArrayList<>();

        for (Ingredient recipeIngredient : recipe.getIngredients()) {
            boolean foundInFridge = false;

            for (Ingredient fridgeIngredient : fridgeIngredients) {
                if (fridgeIngredient.getName().equalsIgnoreCase(recipeIngredient.getName())) {
                    foundInFridge = true;
                    break;
                }
            }

            if (!foundInFridge) {
                missingIngredients.add(recipeIngredient);
            }
        }

        // Ajouter les ingrédients manquants à la liste de courses
        if (!missingIngredients.isEmpty()) {
            for (Ingredient missingIngredient : missingIngredients) {
                // Add each missing ingredient individually
                DataBaseConnection.addIngredientToShoppingList(user.getUserID(), missingIngredient);

                System.out.println("- " + missingIngredient.getName());
            }
            JOptionPane.showMessageDialog(null, "Ingredients added to the shopping list!");
        } else {
            JOptionPane.showMessageDialog(null, "All ingredients are already in the fridge!");
        }
    }

	
	protected List<Recipe> getRecipesByIngredientsListAndIntolerancesList(List<Ingredient> ingredientNames, List<String> intolerances, String apiKey) {
		 List<Recipe> recipes = new ArrayList<>();

	        try {
	            StringBuilder ingredientList = new StringBuilder();
	            for (Ingredient ingredientName : ingredientNames) {
	                if (ingredientList.length() > 0) {
	                    ingredientList.append(",");
	                }
	                ingredientList.append(URLEncoder.encode(ingredientName.getName(), "UTF-8"));
	            }
	            // Construire la liste d'intolérances pour l'URL
	            String intoleranceList = String.join(",", intolerances);
	            String encodedIngredients = ingredientList.toString();
	            String searchURL = "https://api.spoonacular.com/recipes/complexSearch?includeIngredients=" + encodedIngredients +
	                    "&intolerances=" + intoleranceList + "&apiKey=" + apiKey;
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
	           // System.out.println("API Response: " + response.toString());

	         // Lecture de la réponse JSON pour obtenir les informations sur les recettes possibles
	            JSONObject responseJson = new JSONObject(response.toString());
	            //System.out.println(responseJson);
	         // Vérifier si la propriété "results" existe dans l'objet JSON
	            if (responseJson.has("results") && !responseJson.isNull("results")) {
	            	
	                JSONArray recipesArray = responseJson.getJSONArray("results");
	                if (recipesArray.length() > 0) {
	                // Traitement du tableau JSON
	                for (int i = 0; i < recipesArray.length(); i++) {
	                    JSONObject recipeObject = recipesArray.getJSONObject(i);
		                String recipeTitle = recipeObject.getString("title");
		                int recipeId = recipeObject.getInt("id");
		                List<Ingredient> recipeIngredients = getIngredientsByRecipeId1(recipeId, apiKey);
		                String instructions = getRecipeInstructionsById(recipeId, apiKey);
	
		                // Récupération de l'URL de l'image
		                String imageUrl = recipeObject.getString("image");
		                
		                Recipe recipe = new Recipe(recipeTitle, recipeId, recipeIngredients, instructions, imageUrl);
		                recipe.setUrlImage(imageUrl);
		                recipes.add(recipe);
	                }
	                } else {
	                    // Aucun résultat trouvé
	                    JOptionPane.showMessageDialog(IntolerencesView.this, "Sorry, we couldn't find a recipe for you in our database, modify your ingredients list", "Avertissement", JOptionPane.WARNING_MESSAGE);
	                }
	            } else {
	                // Gérer le cas où la propriété "results" est absente ou est null
	                System.out.println("The 'results' property is not present in the JSON response or is null.");
	            }
	        } catch (Exception e) {
	        	System.err.println("Error API");
	        }

	        return recipes;
	    }

	 private static List<Ingredient> getIngredientsByRecipeId1(int recipeId2, String apiKey) {
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
	            e.printStackTrace();
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
	                instructions = doc.text();          
	            } else {
	                // Gérer le cas où le champ n'est pas une chaîne ou est manquant
	                instructions = "Instructions not available in API ";
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return instructions;
	    }

	    public void showPage() {
	        this.setVisible(true);
	        // Masquez ou fermez la fenêtre actuelle si nécessaire
	    }
}

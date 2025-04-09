package fr.tse.fise2.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.Recipe;
import fr.tse.fise2.model.User;

public class DataBaseConnection {

	    private static final String DATABASE_URL = "jdbc:sqlite:src/main/resources/bdd/smartFridge.db";

	    public static Connection connect() throws SQLException {
	        return DriverManager.getConnection(DATABASE_URL);
	    }

	    // Fonction pour récupérer tous les utilisateurs
	    public static List<User> getUsers() {
	        List<User> users = new ArrayList<>();

	        try (Connection connection = connect();
	             Statement statement = connection.createStatement();
	             ResultSet resultSet = statement.executeQuery("SELECT * FROM Utilisateur")) {

	            while (resultSet.next()) {
	                int userID = resultSet.getInt("ID_User");
	                String username = resultSet.getString("username");
	                String hashedPassword = resultSet.getString("password"); // Récupérez le mot de passe haché
	                users.add(new User(userID, username, hashedPassword));
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return users;
	    }

	    // Fonction pour récupérer un utilisateur par son nom d'utilisateur
	    public static User getUserByUsername(String username) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Utilisateur WHERE username = ?")) {

	            preparedStatement.setString(1, username);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            if (resultSet.next()) {
	                int userID = resultSet.getInt("ID_User");
	                String password = resultSet.getString("password");
	                return new User(userID, username, password);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return null;
	    }

	    // Fonction pour récupérer la liste de frigo d'un utilisateur
	    public static List<Ingredient> getListeFrigo(int userID) {
	        List<Ingredient> ingredients = new ArrayList<>();

	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "SELECT * FROM Ingredient i "
	                     + "LEFT JOIN ListeFrigo lf ON i.ID_ingredient = lf.ID_ingredient "
	                     + "WHERE lf.ID_User = ?;")) {

	            preparedStatement.setInt(1, userID);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            while (resultSet.next()) {
	                int ingredientID = resultSet.getInt("ID_ingredient");
	                String nom = resultSet.getString("nom");
	                String quantite = resultSet.getString("quantite");
	                String datePeremption = resultSet.getString("date_peremption");
	                ingredients.add(new Ingredient(ingredientID, nom, quantite, datePeremption));
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return ingredients;
	    }

	    public static List<Recipe> getFavoritesRecipes(int userID) {
	        List<Recipe> recipes = new ArrayList<>();

	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "SELECT * FROM Recettes_Favories WHERE ID_User = ?")) {

	            preparedStatement.setInt(1, userID);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            while (resultSet.next()) {
	                int recipeID = resultSet.getInt("ID_recette");
	                String title = resultSet.getString("nom");
	                String instructions = resultSet.getString("instructions");
	                String urlImage = resultSet.getString("urlImage");

	                // Récupérer la liste des ingrédients pour chaque recette
	                List<Ingredient> ingredients = getIngredientsForRecipe(recipeID);

	                Recipe recipe = new Recipe(title, recipeID, ingredients, instructions, urlImage);

	                recipes.add(recipe);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return recipes;
	    }

	    private static List<Ingredient> getIngredientsForRecipe(int recipeID) {
	        List<Ingredient> ingredients = new ArrayList<>();

	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "SELECT i.* FROM IngredientsRecettes ir "
	                     + "JOIN Ingredient i ON ir.ID_ingredient = i.ID_ingredient "
	                     + "WHERE ir.ID_recette = ?")) {

	            preparedStatement.setInt(1, recipeID);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            while (resultSet.next()) {
	                int ingredientID = resultSet.getInt("ID_ingredient");
	                String nom = resultSet.getString("nom");
	                String quantite = resultSet.getString("quantite");
	                String datePeremption = resultSet.getString("date_peremption");
	                ingredients.add(new Ingredient(ingredientID, nom, quantite, datePeremption));
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return ingredients;
	    }

	    // Fonction pour ajouter un utilisateur
	    public static void addUser(String username, String password) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "INSERT INTO Utilisateur (username, password) VALUES (?, ?)")) {

	            // Hachez le mot de passe avant de l'ajouter à la base de données
	            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
	            preparedStatement.setString(1, username);
	            preparedStatement.setString(2, hashedPassword);
	            preparedStatement.executeUpdate();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    // Fonction pour ajouter un ingrédient à la liste de frigo d'un utilisateur
	    public static void addIngredientToList(int userID, String nom, String quantite, String datePeremption) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "INSERT INTO Ingredient (nom, quantite, date_peremption) VALUES (?, ?, ?)",
	                     Statement.RETURN_GENERATED_KEYS)) {

	            preparedStatement.setString(1, nom);
	            preparedStatement.setString(2, quantite);
	            preparedStatement.setString(3, datePeremption);
	            preparedStatement.executeUpdate();

	            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
	            if (generatedKeys.next()) {
	                int ingredientID = generatedKeys.getInt(1);

	                try (PreparedStatement insertListeFrigo = connection.prepareStatement(
	                        "INSERT INTO ListeFrigo (ID_User, ID_ingredient) VALUES (?, ?)")) {

	                    insertListeFrigo.setInt(1, userID);
	                    insertListeFrigo.setInt(2, ingredientID);
	                    insertListeFrigo.executeUpdate();
	                }
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public static void addRecipe(int userID, int recipeID, String nom, String instructions, List<Ingredient> ingredients, String urlImage) {
	    	if (!checkIfRecipeExists(userID, nom)) {
		    	try (Connection connection = connect();
		             PreparedStatement preparedStatement = connection.prepareStatement(
		                     "INSERT INTO Recettes_Favories (urlImage, nom, instructions, ID_User) VALUES (?, ?, ?, ?)")) {
	
		    		preparedStatement.setString(1, urlImage);
		    		preparedStatement.setString(2, nom);
		            preparedStatement.setString(3, instructions);
		            preparedStatement.setInt(4, userID);
		            preparedStatement.executeUpdate();
		            
		            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
		            if (generatedKeys.next()) {
		                int recipeID_ = generatedKeys.getInt(1);
			            for (Ingredient ingredient : ingredients) {
			            	
			                // Vérifier si l'ingrédient existe déjà dans la table Ingredient
			                if (!checkIfIngredientExists(ingredient.getName(), ingredient.getQuantity())) {
			                    // Si l'ingrédient n'existe pas, l'ajouter dans la table Ingredient
			                    addIngredient(ingredient, recipeID_);
			                }
			                else {
			                	addIngredientsForRecipe(recipeID, ingredient.getID_Ingredient());
			                }
			            }
		            }
		        } catch (SQLException e) {

		            e.printStackTrace();
		        }
	    	}
	    }
	    
	 // Ajouter une méthode pour vérifier si une recette existe déjà pour un utilisateur
	    private static boolean checkIfRecipeExists(int userID, String nom) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "SELECT * FROM Recettes_Favories WHERE nom = ? AND ID_User = ?")) {

	            preparedStatement.setString(1, nom);
	            preparedStatement.setInt(2, userID);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            return resultSet.next();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return false;
	    }
	    
	    private static void addIngredientsForRecipe(int recipeID, int ingredient) {
	        try (Connection connection = connect();
	             PreparedStatement insertIngredientsStatement = connection.prepareStatement(
	                     "INSERT INTO IngredientsRecettes (ID_ingredient, ID_recette) VALUES (?, ?)")) {

	                insertIngredientsStatement.setInt(1, ingredient);
	                insertIngredientsStatement.setInt(2, recipeID);
	                insertIngredientsStatement.executeUpdate();

	        } catch (SQLException e) {
	            System.err.println("Attention quelques ingredients existe déjà dans la BDD ");

	           // e.printStackTrace();
	        }
	    }

	    // Ajouter une méthode pour vérifier si un ingrédient existe déjà dans la table Ingredient
	    private static boolean checkIfIngredientExists(String nom, String quantity) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	            		 "SELECT * FROM Ingredient WHERE LOWER(nom) = LOWER(?) AND quantite = ?")) {

	            preparedStatement.setString(1, nom);
	            preparedStatement.setString(2, quantity);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            return resultSet.next();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return false;
	    }

	    
	    // Ajouter une méthode pour ajouter un ingrédient dans la table Ingredient
	    private static void addIngredient(Ingredient ingredient, int recipeID) {
	        try (Connection connection = connect();
	             PreparedStatement insertIngredientStatement = connection.prepareStatement(
	                     "INSERT INTO Ingredient (nom, quantite) VALUES (?, ?)")) {

	            insertIngredientStatement.setString(1, ingredient.getName());
	            insertIngredientStatement.setString(2, ingredient.getQuantity());
	            insertIngredientStatement.executeUpdate();
	            
	            ResultSet generatedKeys = insertIngredientStatement.getGeneratedKeys();
	            if (generatedKeys.next()) {
	                int ingredientID = generatedKeys.getInt(1);
	                addIngredientsForRecipe(recipeID, ingredientID);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    
	    public static void deleteIngredient(int userID, int ingredientID) {
	        try (Connection connection = connect()) {
	        	
	        	try (PreparedStatement deleteFrigoStatement = connection.prepareStatement(
	                    "DELETE FROM ListeFrigo WHERE ID_ingredient = ? AND ID_User = ?")) {

	        		deleteFrigoStatement.setInt(1, ingredientID);
	        		deleteFrigoStatement.setInt(2, userID);
	        		deleteFrigoStatement.executeUpdate();
	            }
	            try (PreparedStatement deleteIngredientStatement = connection.prepareStatement(
                        "DELETE FROM Ingredient WHERE ID_ingredient = ?")) {

                    deleteIngredientStatement.setInt(1, ingredientID);
                    deleteIngredientStatement.executeUpdate();
                }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public static void deleteRecipe(int userID, int recipeID, List<Ingredient> ingredients) {
	        try (Connection connection = connect()) {
	            // Supprimer la recette de la table Recettes_Favories
	            try (PreparedStatement deleteRecipeStatement = connection.prepareStatement(
	                    "DELETE FROM Recettes_Favories WHERE ID_recette = ? AND ID_User = ?")) {

	                deleteRecipeStatement.setInt(1, recipeID);
	                deleteRecipeStatement.setInt(2, userID);
	                deleteRecipeStatement.executeUpdate();
	            }
	            
	            for (Ingredient ingredient : ingredients) {
	            	try (PreparedStatement deleteIngredientStatement = connection.prepareStatement(
	                        "DELETE FROM Ingredient WHERE ID_ingredient = ?")) {

	                    deleteIngredientStatement.setInt(1, ingredient.getID_Ingredient());
	                    deleteIngredientStatement.executeUpdate();
	                }
	            }

	            // Supprimer les ingrédients associés à la recette de la table IngredientsRecettes
	            try (PreparedStatement deleteIngredientsStatement = connection.prepareStatement(
	                    "DELETE FROM IngredientsRecettes WHERE ID_recette = ?")) {

	                deleteIngredientsStatement.setInt(1, recipeID);
	                deleteIngredientsStatement.executeUpdate();
	            }
	            

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public static boolean addIntolerance(int userID, String intoleranceName) {
	        // Vérifier si l'intolérance existe déjà
	        if (!checkIfIntoleranceExists(userID, intoleranceName)) {
	            try (Connection connection = connect();
	                 PreparedStatement preparedStatement = connection.prepareStatement(
	                         "INSERT INTO Intolerence (nom, ID_User) VALUES (?, ?)")) {

	                preparedStatement.setString(1, intoleranceName);
	                preparedStatement.setInt(2, userID);
	                preparedStatement.executeUpdate();
	                
	                return true; // Intolérance ajoutée avec succès

	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }

	        // L'intolérance existe déjà
	        return false;
	    }
	    
	    private static boolean checkIfIntoleranceExists(int userID, String intoleranceName) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "SELECT * FROM Intolerence WHERE nom = ? AND ID_User = ?")) {

	            preparedStatement.setString(1, intoleranceName);
	            preparedStatement.setInt(2, userID);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            return resultSet.next();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return false;
	    }

	    public static List<String> getIntolerances(int userID) {
	        List<String> intolerances = new ArrayList<>();

	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "SELECT nom FROM Intolerence WHERE ID_User = ?")) {

	            preparedStatement.setInt(1, userID);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            while (resultSet.next()) {
	                String intoleranceName = resultSet.getString("nom");
	                intolerances.add(intoleranceName);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return intolerances;
	    }
	    public static void removeIntolerance(int userID, String intoleranceName) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "DELETE FROM Intolerence WHERE ID_User = ? AND nom = ?")) {

	            preparedStatement.setInt(1, userID);
	            preparedStatement.setString(2, intoleranceName);
	            preparedStatement.executeUpdate();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public static void updateIngredientQuantity(int userID, int ingredientID, String newQuantity) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "UPDATE Ingredient SET quantite = ? WHERE ID_ingredient = ?")) {

	            preparedStatement.setString(1, newQuantity);
	            preparedStatement.setInt(2, ingredientID);
	            preparedStatement.executeUpdate();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public static void addIngredientToShoppingList(int userID, Ingredient ingredient) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "INSERT INTO shopping_list (user_id, ingredient_name) VALUES (?, ?)")) {

	            preparedStatement.setInt(1, userID);
	            preparedStatement.setString(2, ingredient.getName());
	            preparedStatement.executeUpdate();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }



	    public static List<String> getShoppingList(int userID) {
	        List<String> shoppingList = new ArrayList<>();

	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "SELECT ingredient_name FROM shopping_list WHERE user_id = ?;")) {

	            preparedStatement.setInt(1, userID);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            while (resultSet.next()) {
	                String ingredientName = resultSet.getString("ingredient_name");
	                shoppingList.add(ingredientName);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return shoppingList;
	    }



	    public static void removeIngredientFromShoppingList(int userID, String ingredientName) {
	        try (Connection connection = connect();
	             PreparedStatement preparedStatement = connection.prepareStatement(
	                     "DELETE FROM shopping_list WHERE user_id = ? AND ingredient_name = ?")) {

	            preparedStatement.setInt(1, userID);
	            preparedStatement.setString(2, ingredientName);
	            preparedStatement.executeUpdate();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
}

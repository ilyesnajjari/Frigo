package fr.tse.fise2.view;

import javax.swing.*;
import javax.swing.border.Border;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.Recipe;
import fr.tse.fise2.model.User;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FavoritesView extends JFrame {
	
	private User user;
    private MenuView menuView;
	private BotButtonsManager botButtonsManager;

    public FavoritesView(User user_,MenuView menuView) {
    	
    	this.user = user_;
        this.menuView = menuView;
        botButtonsManager = new BotButtonsManager(this,menuView);
        JPanel botPanel = botButtonsManager.getButtonsPanel();
        botPanel.setOpaque(false); // Pour rendre le JPanel transparent
        botPanel.setBounds(180, 680, 800, 100);
       
        
	    // Configurer la fenêtre principale
	    setTitle("Favorites");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setSize(1300, 800); // Remplacez ces valeurs par les dimensions souhaitées
	    setLocationRelativeTo(null);
	
	    // Créer un JPanel pour contenir les composants
	    JPanel mainPanel = new JPanel();
	    mainPanel.setLayout(null);
        mainPanel.add(botPanel); // Ajoutez le JPanel des boutons gérés par BotButtonsManager à mainPanel

        BackgroundView backgroundView = new BackgroundView(user, this, menuView.getIsAliceMode());
	    JPanel backPanel = backgroundView.getBackgroundView();
	    backPanel.setOpaque(false); // Pour rendre le JPanel transparent
	    backPanel.setBounds(0, 0, getWidth(), getHeight());
	    
        // Ajouter une liste déroulante pour afficher les ingrédients
        JScrollPane scrollPane = new JScrollPane(createRecipesPanel());
        scrollPane.getViewport().setBackground(Color.WHITE);
	    scrollPane.setBounds(30, 130, 1200, 540);
        
        mainPanel.add(scrollPane);
        mainPanel.add(backPanel);

        // Ajouter le panel principal à la fenêtre
        add(mainPanel);

        // Rendre la fenêtre visible
        setVisible(true);
    }
    
    private JPanel createRecipesPanel() {
        JPanel recipesPanel = new JPanel();
        recipesPanel.setLayout(new BoxLayout(recipesPanel, BoxLayout.Y_AXIS));

        List<Recipe> recipes = DataBaseConnection.getFavoritesRecipes(user.getUserID());

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
        shoppingListButton.setBounds(530, 10, 160, 50);
        shoppingListButton.setForeground(new Color(37,138,184));
        shoppingListButton.setFont(new Font("Comic Sans MS", Font.BOLD, 10));

        // Ajouter un bouton pour afficher les détails de la recette
        JButton detailsButton = new JButton("Details");
        detailsButton.setSize(new Dimension(100, 50));
        detailsButton.setPreferredSize(new Dimension(100, 50));
        detailsButton.setBounds(700, 10, 100, 50);
        detailsButton.setForeground(new Color(37,138,184));
        detailsButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        
        // Ajouter un bouton pour ajouter la recette aux favoris
        JButton deleteButton = new JButton("Delete");
        deleteButton.setSize(new Dimension(100, 50));
        deleteButton.setPreferredSize(new Dimension(100, 50));
        deleteButton.setBounds(810, 10, 100, 50);
        deleteButton.setForeground(new Color(37,138,184));
        deleteButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
	        	DataBaseConnection.deleteRecipe(user.getUserID(), recipe.getRecipeId(), recipe.getIngredients());
	        	
	        	// Update the UI or refresh the ingredient list as needed
	        	recipePanel.removeAll();
	        	recipePanel.add(createRecipesPanel());
	        	recipePanel.revalidate();
	        	recipePanel.repaint();
            }
        });

        JLabel recipeImageLabel;
	    recipeImageLabel = new JLabel();
	    int imageWidth = 200; 
	    int imageHeight = 100;
	    int imageX = 960;
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
        
    
        recipePanel.add(shoppingListButton);
        recipePanel.add(detailsButton);
        recipePanel.add(deleteButton);



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

}
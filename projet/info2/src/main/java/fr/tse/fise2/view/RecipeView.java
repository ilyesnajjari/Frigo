package fr.tse.fise2.view;
import java.net.URL;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.Recipe;
import fr.tse.fise2.model.User;

public class RecipeView extends JFrame {
	
	private Recipe recipe;
    private JLabel recipeImageLabel;
    private MenuView menuView;
	private BotButtonsManager botButtonsManager;

	// TODO
    public RecipeView(User user, Recipe recipe_,MenuView menuView) {
    	
    	this.recipe = recipe_;
        this.menuView = menuView;
        botButtonsManager = new BotButtonsManager(this,menuView);
        JPanel botPanel = botButtonsManager.getButtonsPanel();
        botPanel.setOpaque(false); // Pour rendre le JPanel transparent
        botPanel.setBounds(180, 680, 800, 100);
       
        
	    // Configurer la fenêtre principale
	    setTitle("Recipe");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setSize(1300, 800); // Remplacez ces valeurs par les dimensions souhaitées
	    setLocationRelativeTo(null);
	
	    // Créer un JPanel pour contenir les composants
	    JPanel mainPanel = new JPanel();
	    mainPanel.setLayout(null);
	    
        BackgroundView backgroundView = new BackgroundView(user, this, menuView.getIsAliceMode());
	    JPanel backPanel = backgroundView.getBackgroundView();
	    backPanel.setOpaque(false); // Pour rendre le JPanel transparent
	    backPanel.setBounds(0, 0, getWidth(), getHeight());
        mainPanel.add(botPanel); // Ajoutez le JPanel des boutons gérés par BotButtonsManager à mainPanel

	    // Configure the "Return" button
        JButton returnButton = new JButton("Return");
        returnButton.setBackground(new Color(253, 185, 76));
        returnButton.setForeground(new Color(37,138,184));
        returnButton.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        returnButton.setBounds(130, 125, 150, 50);
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	new RecipesView(user, DataBaseConnection.getListeFrigo(user.getUserID()),menuView);

                dispose();
            }
        });
        mainPanel.add(returnButton);
        
        // Créer des JLabel pour afficher les détails de la recette
        JLabel titleLabel = new JLabel(recipe.getTitle());
        titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        titleLabel.setBounds(450, 130, 700, 60);
        titleLabel.setForeground(new Color(254,180,79));
        
        JLabel ingredientsLabel = new JLabel("Ingredients:");
        ingredientsLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        ingredientsLabel.setForeground(new Color(254, 130, 50));
        ingredientsLabel.setBounds(450, 190, 700, 50);

        // Ajouter une liste déroulante pour afficher les ingrédients
        JScrollPane scrollPane = new JScrollPane(createIngredientsPanel());
        scrollPane.getViewport().setBackground(Color.WHITE);
	    scrollPane.setBounds(450, 240, 400, 130);
        
	 // Ajouter le panneau d'images à droite de la liste déroulante
	    recipeImageLabel = new JLabel();
	    int imageWidth = 300; // Ajustez la largeur de l'image selon vos besoins
	    int imageHeight = 300; // Ajustez la hauteur de l'image selon vos besoins
	    int ingredientsX = scrollPane.getX();
	    int ingredientsWidth = scrollPane.getWidth();
	    int imageX = ingredientsX + ingredientsWidth + 40; // Ajoutez un espacement entre la liste déroulante et l'image
	    
	 // Ajustez la position y de l'image en tenant compte de la hauteur du titre
	    int titleY = titleLabel.getY();
	    int titleHeight = titleLabel.getHeight();
	    int imageY = titleY + titleHeight-40 ; // Ajoutez un espacement entre le titre et l'image

	    
	    recipeImageLabel.setBounds(imageX, imageY, imageWidth, imageHeight); // Ajustez la position et la taille selon vos besoins
	    mainPanel.add(recipeImageLabel);

        
        // Mettez à jour l'image lorsque vous avez l'URL de l'image
        String imageUrl = recipe_.getUrlImage();
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
        
        JLabel instructionsLabel = new JLabel("Instructions:");
    	
    	instructionsLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
    	instructionsLabel.setBounds(450, 380, 700, 50);
    	instructionsLabel.setForeground(new Color(254, 130, 50));
    	
    	JTextArea instructions = new JTextArea(recipe.getInstructions());
    	if(instructions.getText().equals("")) {
    		instructions.setText("No instructions available");
    	}
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        instructions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane instructionsPane = new JScrollPane(instructions);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     	instructionsPane.setBounds(450, 430, 700, 230);

    	mainPanel.add(titleLabel);
    	mainPanel.add(ingredientsLabel);
    	mainPanel.add(scrollPane);
    	mainPanel.add(instructionsLabel);
    	mainPanel.add(instructionsPane);
    	mainPanel.add(backPanel);

        // Ajouter le panel principal à la fenêtre
        add(mainPanel);

        // Rendre la fenêtre visible
        setVisible(true);
    }
    private JPanel createIngredientsPanel() {
        JPanel ingredientsPanel = new JPanel();
        ingredientsPanel.setLayout(new BoxLayout(ingredientsPanel, BoxLayout.Y_AXIS));
        ingredientsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ajouter chaque ingrédient à la liste déroulante
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            JLabel ingredientLabel = new JLabel("- " + recipe.getIngredients().get(i).getName() + ": "
                    + recipe.getIngredients().get(i).getQuantity() + " ");
            ingredientLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
            ingredientsPanel.add(ingredientLabel);
        }

        return ingredientsPanel;
    }



    
}

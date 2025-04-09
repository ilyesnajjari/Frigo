package fr.tse.fise2.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.User;

public class ShoppingListView extends JFrame {

    private User user;
    private List<String> shoppingList;
    private MenuView menuView;
	private BotButtonsManager botButtonsManager;

    public ShoppingListView(User user_,MenuView menuView) {
        this.user = user_;
        this.menuView = menuView;
        botButtonsManager = new BotButtonsManager(this,menuView);
        JPanel botPanel = botButtonsManager.getButtonsPanel();
        botPanel.setOpaque(false); // Pour rendre le JPanel transparent
        botPanel.setBounds(180, 680, 800, 100);
       
        
        setTitle("Shopping List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        shoppingList = DataBaseConnection.getShoppingList(user.getUserID());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);

        BackgroundView backgroundView = new BackgroundView(user, this, menuView.getIsAliceMode());
        JPanel backPanel = backgroundView.getBackgroundView();
        backPanel.setOpaque(false);
        backPanel.setBounds(0, 0, getWidth(), getHeight());
        mainPanel.add(botPanel); // Ajoutez le JPanel des boutons gérés par BotButtonsManager à mainPanel

        // Ajouter le texte "My Shopping List" au-dessus du panneau de la liste de courses
        JLabel shoppingListLabel = new JLabel("Items to buy");
        shoppingListLabel.setFont(new Font("Snap ITC", Font.BOLD, 30));
        shoppingListLabel.setForeground(new Color(253, 189, 79));
        shoppingListLabel.setBounds(600, 150, 400, 100);
        mainPanel.add(shoppingListLabel);

        JPanel shoppingListPanel = new JPanel();
        shoppingListPanel.setLayout(new BoxLayout(shoppingListPanel, BoxLayout.Y_AXIS));

        shoppingListPanel.setBounds(470, 230, 500, 420);
        shoppingListPanel.setOpaque(false);


        for (String ingredientName : shoppingList) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new BorderLayout());
            itemPanel.setOpaque(false); // Set background to be transparent

            JLabel ingredientLabel = new JLabel(ingredientName);
            ingredientLabel.setFont(new Font("Comic Sans MS", Font.CENTER_BASELINE, 18));  // Ajustez la police et la taille

            JButton deleteButton = new JButton("Bought");
           deleteButton.setOpaque(false); // Set background to be transparent

            deleteButton.addActionListener(e -> {
                // Supprimer l'ingrédient de la liste de courses dans la base de données
                DataBaseConnection.removeIngredientFromShoppingList(user.getUserID(), ingredientName);

                // Retirer l'ingrédient de la liste de courses de l'affichage
                shoppingListPanel.remove(itemPanel);

                // Mettre à jour l'affichage
                mainPanel.revalidate();
                mainPanel.repaint();
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));  // Aligner les boutons à droite
            buttonPanel.setOpaque(false);
            buttonPanel.add(deleteButton);

            itemPanel.add(ingredientLabel, BorderLayout.CENTER);
            itemPanel.add(buttonPanel, BorderLayout.EAST);  // Aligner le panneau de boutons à droite
            shoppingListPanel.add(itemPanel);
        }
        JScrollPane scrollPane = new JScrollPane(shoppingListPanel);
        scrollPane.setBounds(470, 230, 500, 420);
        scrollPane.setOpaque(false);
     // Définir la couleur de fond du composant affiché dans le JScrollPane
        JPanel viewportView = new JPanel();
        viewportView.setBackground(new Color(0, 0, 0, 0));  // Couleur transparente
        viewportView.setLayout(new BorderLayout()); // Ajout de cette ligne

        // Ajouter votre JPanel avec les éléments à viewportView
        viewportView.add(shoppingListPanel, BorderLayout.NORTH); // Ou une autre position selon votre besoin

        scrollPane.setViewportView(viewportView);

        mainPanel.add(scrollPane);
        mainPanel.add(backPanel);

        add(mainPanel);
        setVisible(true);
    }
}

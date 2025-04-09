package fr.tse.fise2.view;

import javax.swing.*;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.User;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MenuView extends JFrame {

    private User user;
    private AtomicBoolean isAliceMode ;
    private List<JButton> buttons;
    private JButton switchButton;
    private List<JButton> botbuttons;
    private BotButtonsManager botButtonsManager;

    private JPanel buttonPanel; // Ajoutez cette ligne
    private BackgroundView backgroundView;  // Ajoutez cette ligne

    public MenuView(User user, AtomicBoolean isAliceMode) {
        this.user = user;
        this.isAliceMode=isAliceMode;
        botButtonsManager = new BotButtonsManager(this,this);
        JPanel botPanel = botButtonsManager.getButtonsPanel();
        botPanel.setOpaque(false); // Pour rendre le JPanel transparent
        botPanel.setBounds(180, 680, 800, 100);
       
        
        // Configurer la fenêtre principale
        setTitle("Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        // Créer un JPanel pour contenir les composants
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
System.out.println(isAliceMode);
        backgroundView = new BackgroundView(user, this, isAliceMode);
        JPanel backPanel = backgroundView.getBackgroundView();
        backPanel.setOpaque(false);
        backPanel.setBounds(0, 0, getWidth(), getHeight());
        mainPanel.add(botPanel); // Ajoutez le JPanel des boutons gérés par BotButtonsManager à mainPanel


       
        // Créer un JPanel pour les boutons
        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(550, 220, 400, 600);

        // Créer un JPanel pour le switch button
        JPanel switchPanel = new JPanel();
        switchPanel.setOpaque(false);
        switchPanel.setBounds(950, 150, 200, 50);

        
        buttons = new ArrayList<>();

        buttons.add(new JButton("Find recipes"));
        buttons.add(new JButton("Ingredients"));
        buttons.add(new JButton("Add ingredient"));
        buttons.add(new JButton("Shopping list"));



        for (int i = 0; i < buttons.size(); i++) {
            final int window = i;
            buttons.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switch (window) {
                        case 0:
                            new RecipesView(user, DataBaseConnection.getListeFrigo(user.getUserID()),MenuView.this);
                            dispose();
                            break;
                        case 1:
                            new IngredientsListView(user, MenuView.this);
                            dispose();
                            break;
                        case 2:
                            new AddIngredientView(user, MenuView.this);
                            dispose();
                            break;
                        case 3:
                            new ShoppingListView (user, MenuView.this);
                            dispose();
                            break;


                        default:
                            break;
                    }
                }
            });
        }

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBackground(Color.BLUE);
            buttons.get(i).setOpaque(false);
            buttons.get(i).setForeground(Color.WHITE);
            buttons.get(i).setFont(new Font("Comic Sans MS", Font.BOLD, 40));
            buttons.get(i).setBounds(100, 50 + i * 150, 100, 50);
            buttonPanel.add(buttons.get(i));
        }
        
        
        switchButton = new JButton("Switch Mode");
        switchButton.addActionListener(e -> {

            isAliceMode.set(!isAliceMode.get());
            updateButtonLabels();

        });
        

        switchButton.setBackground(Color.BLUE);
        switchButton.setOpaque(false);
        switchButton.setForeground(new Color(253,189,79));
        switchButton.setFont(new Font("Snap ITC", Font.BOLD, 20));
        switchPanel.add(switchButton);
        mainPanel.add(switchPanel);
        updateButtonLabels();

        mainPanel.add(buttonPanel);
        mainPanel.add(backPanel);

        add(mainPanel);
        setVisible(true);
    }

	private void updateButtonLabels() {
        if (isAliceMode.get()) {
            buttons.get(0).setText("Find recipes");
            buttons.get(1).setText("Ingredients");
            buttons.get(2).setText("Add ingredient");
            buttons.get(3).setText("Shopping list");
         
            for (int i = 4; i < buttons.size(); i++) {
                buttonPanel.remove(buttons.get(i));
            }
        } else {
            buttons.get(0).setText("Find recipes");
            buttons.get(1).setText("Ingredients");
            buttons.get(2).setText("Add ingredient");
           
            // Ajoutez le bouton "Intolerances" pour le mode Bob
            JButton intolerancesButton = new JButton("Intolerances");
            intolerancesButton.setBackground(Color.BLUE);
            intolerancesButton.setOpaque(false);
            intolerancesButton.setForeground(Color.WHITE);
            intolerancesButton.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
            intolerancesButton.setBounds(100, 50 + 4 * 150, 100, 50);
            intolerancesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Ajoutez les actions pour le bouton "Intolerances" ici
                    new IntolerencesView(user,MenuView.this);
                    dispose();
                }
            });

            buttons.add(intolerancesButton);

            // Ajoutez le nouveau bouton à buttonPanel
            buttonPanel.add(intolerancesButton);
        }
        botButtonsManager.configureButtons();

        // Rafraîchissez le panneau de boutons
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }


    public AtomicBoolean getIsAliceMode() {
        return isAliceMode;
        
    }

	public User getUser() {
		return user;
	}
}



package fr.tse.fise2.view;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class BotButtonsManager {
    private List<JButton> botbuttons;
    private MenuView window;
	private JFrame window_;

    public BotButtonsManager(JFrame window_,MenuView window) {
        this.window = window;
		this.window_ = window_;

        initializeButtons();
    }

    private void initializeButtons() {
        botbuttons = List.of(new JButton("Menu"), new JButton("Favorites"), new JButton("Alerts"), new JButton("Log Out"));
        configureButtons();  // Appeler la méthode pour configurer les boutons

        for (int i = 0; i < botbuttons.size(); i++) {
            final int buttonIndex = i;
            botbuttons.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleBotButtonClick(buttonIndex);
                	

                }
            });
        }
    }

    private void handleBotButtonClick(int index) {
        switch (index) {
            case 0:

                new MenuView(window.getUser(), window.getIsAliceMode());
                System.out.println("Disposing AddingIngredientView...");

                window_.dispose();

                
                break;
            case 1:
                new FavoritesView(window.getUser(), window);
                window_.dispose();
                break;
            case 2:
                new AlertsView(window.getUser(), window);
                window_.dispose();
                break;
            case 3:
                new LoginView();
                window_.dispose();
                break;
            default:
                // Actions par défaut, si nécessaire
                break;
        }
    }

    public List<JButton> getBotbuttons() {
        return botbuttons;
    }
    public JPanel getButtonsPanel() {
        JPanel botPanel = new JPanel();
        botPanel.setOpaque(false);

        for (int i = 0; i < botbuttons.size(); i++) {
            botbuttons.get(i).setBackground(Color.BLUE);
            botbuttons.get(i).setOpaque(false);
            botbuttons.get(i).setForeground(new Color(37, 138, 184));
            botbuttons.get(i).setFont(new Font("Comic Sans MS", Font.BOLD, 40));
            botbuttons.get(i).setBounds(0 + i * 600, 15, 100, 50);
            botPanel.add(botbuttons.get(i));
        }
     configureButtons();  // Appeler la méthode pour configurer les boutons

        return botPanel;
    }
    
    public void configureButtons() {
        if (window.getIsAliceMode().get()) {
            hideFavoritesButton();
            hideAlertsButton();
        } else {
            showFavoritesButton();
            showAlertsButton();
        }
    }

    private void hideFavoritesButton() {
        botbuttons.get(1).setVisible(false);
    }

    private void showFavoritesButton() {
        botbuttons.get(1).setVisible(true);
    }

    private void hideAlertsButton() {
        botbuttons.get(2).setVisible(false);
    }

    private void showAlertsButton() {
        botbuttons.get(2).setVisible(true);
    }
}

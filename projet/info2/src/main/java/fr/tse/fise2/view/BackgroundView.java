package fr.tse.fise2.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.tse.fise2.model.User;

public class BackgroundView {
	
	private JPanel background;
	private User user;
	private JFrame window_;
    private List<JButton> buttons;
    private AtomicBoolean isAliceMode;  // Ajout de cette variable

	
	public JPanel getBackgroundView() {
		return background;
	}



	public void setBackgroundView(JPanel mainPanel) {
		this.background = mainPanel;
	}



    public BackgroundView(User user_, JFrame window_, AtomicBoolean isAliceMode) {
        this.isAliceMode = isAliceMode;

		this.window_ = window_;
		user = user_;

        // Créer un JPanel pour contenir les composants
        background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Charger l'image de fond
                ImageIcon imageIcon = new ImageIcon("src/main/resources/img/background.jpg");
                Image image = imageIcon.getImage();
                g.drawImage(image, 0, 0, 1300, 750, this);
            }
        };
        background.setLayout(null);
        
        // Ajouter un JLabel pour le titre
        JLabel titleLabel = new JLabel("My Smart Fridge");
        titleLabel.setFont(new Font("Snap ITC", Font.BOLD, 70));
        titleLabel.setForeground(new Color(253,189,79));
        titleLabel.setBounds(210, 3, 900, 80); 

        background.add(titleLabel);
        
    }
}

package fr.tse.fise2.view;
import javax.swing.*;
import org.mindrot.jbcrypt.BCrypt;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.User;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private User user;
    private BackgroundView backgroundView;
    private AtomicBoolean isAliceMode = new AtomicBoolean(true);


    public User getUser() {
		return user;
	}

	public LoginView() {
        // Configurer la fenêtre principale
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        backgroundView = new BackgroundView(user, this, isAliceMode);  // Assure-toi de passer les paramètres appropriés

        // Créer un JPanel pour contenir les composants
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Charger l'image de fond
                ImageIcon imageIcon = new ImageIcon("src/main/resources/img/backgroundLogin.jpg");
                Image image = imageIcon.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setLayout(null);

        // Ajouter un JLabel pour le titre
        JLabel titleLabel = new JLabel("My Smart Fridge");
        titleLabel.setFont(new Font("Snap ITC", Font.BOLD, 70));
        titleLabel.setForeground(new Color(253,189,79));
        titleLabel.setBounds(210, 10, 900, 100); 
        mainPanel.add(titleLabel);

        // Création des composants pour la connexion
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 30));

        usernameField = new JTextField(30);
        usernameField.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        passwordField = new JPasswordField(30);
        passwordField.setFont(new Font("Comic Sans MS", Font.BOLD, 25));

        JButton loginButton = new JButton("Log In");
        JButton signUpButton = new JButton("Sign Up");
        
        loginButton.setForeground(new Color(37,138,184));
        loginButton.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
        signUpButton.setForeground(new Color(37,138,184));
        signUpButton.setFont(new Font("Comic Sans MS", Font.BOLD, 28));

        // Ajout des composants au conteneur
        mainPanel.add(usernameLabel);
        mainPanel.add(usernameField);
        mainPanel.add(passwordLabel);
        mainPanel.add(passwordField);
        mainPanel.add(loginButton);
        mainPanel.add(signUpButton);

        // Positionnement des composants
        usernameLabel.setBounds(580, 280, 400, 50);
        usernameField.setBounds(800, 280, 200, 50);
        passwordLabel.setBounds(580, 350, 400, 50);
        passwordField.setBounds(800, 350, 200, 50);
        loginButton.setBounds(580, 430, 200, 50);
        signUpButton.setBounds(800, 430, 200, 50);

        // Ajout des actions aux boutons
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //signUp();
            	new SignUpView();
            	dispose();
            }
        });

        // Ajout du JPanel à la fenêtre principale
        add(mainPanel);
        
        // Rendre la fenêtre visible
        setVisible(true);
    }

    
    // Vérifier si les données rentrées correspondent à un utilisateur présente dans la base de données
    // Si oui aller à la fenêtre Menu 
    // Sinon faire apparaître un toast text "username or password incorrect"
	private void login() {
	    String username = usernameField.getText();
	    String password = new String(passwordField.getPassword());

	    // Utilisez la fonction getUserByUsername pour vérifier les informations d'identification
	    User user_ = DataBaseConnection.getUserByUsername(username);

	    if (user_ != null && BCrypt.checkpw(password, user_.getPassword())) {
	        // Les identifiants sont valides, aller à la fenêtre Menu
	        user = user_;
	        new MenuView(user, isAliceMode);  // Passer isAliceMode ici
	        // Fermer la fenêtre actuelle
	        dispose();
	    } else {
	        // Les identifiants sont incorrects, afficher un message d'erreur
	        JOptionPane.showMessageDialog(this, "Username or password incorrect", "Login Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
    
    
    // Vérifier si l'username est présent dans la base de données
    // Si oui faire apparaître un toast text "username already exists"
    // Sinon ajouter utilisateur dans la bdd et faire apparaître un toast text "Your sign up was successfull"
    private void signUp() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Vérifier si l'username est déjà présent dans la base de données
        User existingUser = DataBaseConnection.getUserByUsername(username);

        if (existingUser != null) {
            // L'utilisateur existe déjà, afficher un message d'erreur
            JOptionPane.showMessageDialog(this, "Username already exists", "Sign Up Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // L'utilisateur n'existe pas, ajouter l'utilisateur dans la base de données
            DataBaseConnection.addUser(username, password);

            // Afficher un message de succès
            JOptionPane.showMessageDialog(this, "Your sign up was successful", "Sign Up Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

package fr.tse.fise2.view;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class AddIngredientView extends JFrame {
    private JTextField nameTextField;
    private JSpinner quantitySpinner;
    private DefaultListModel<String> quantityListModel;
    private JList<String> quantityJList;
    private JDatePickerImpl expirationDatePicker;
    private User user;
    private MenuView menuView;
	private BotButtonsManager botButtonsManager;

    public AddIngredientView(User user, MenuView menuView) {
    	this.user = user;
        this.menuView = menuView;
        botButtonsManager = new BotButtonsManager(this,menuView);
        JPanel botPanel = botButtonsManager.getButtonsPanel();
        botPanel.setOpaque(false); // Pour rendre le JPanel transparent
        botPanel.setBounds(180, 680, 800, 100);

        // Configurer la fenêtre principale
        setTitle("Add Ingredient");
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

        // Création des composants
        ArrayList<JLabel> labels = new ArrayList<>();
        labels.add(new JLabel("Name:"));
        labels.add(new JLabel("Quantity:"));
        labels.add(new JLabel("Expiration Date:"));

        nameTextField = new JTextField();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Float.MAX_VALUE, 0.5));

        // Utilisation de JDatePicker pour la sélection de la date
        UtilDateModel dateModel = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, properties);
        expirationDatePicker = new JDatePickerImpl(datePanel,  new DateLabelFormatter());

        JButton addButton = new JButton("Add");
        addButton.setBackground(new Color(253, 185, 76));
        addButton.setForeground(new Color(37,138,184));
        addButton.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        addButton.setBounds(900, 550, 200, 60);

        for (int i = 0; i < labels.size(); i++) {
            // Personnaliser l'apparence des étiquettes
            labels.get(i).setForeground(Color.WHITE);
            labels.get(i).setFont(new Font("Comic Sans MS", Font.BOLD, 25));
            labels.get(i).setBounds(500, 220 + i * 100, 300, 50);
            mainPanel.add(labels.get(i));
        }

        // Personnaliser l'apparence du champ de texte pour le nom
        nameTextField.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        nameTextField.setBounds(800, 220, 300, 50);
        mainPanel.add(nameTextField);

        // Personnaliser l'apparence du spinner pour la quantité
        quantitySpinner.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        quantitySpinner.setBounds(800, 320, 145, 50);
        mainPanel.add(quantitySpinner);
        
        String[] quantityValues = {"l", "g", "pcs"};

        quantityListModel = new DefaultListModel<>();
        for (String value : quantityValues) {
            quantityListModel.addElement(value);
        }

        quantityJList = new JList<>(quantityListModel);
        quantityJList.setFont(new Font("Comic Sans MS", Font.BOLD, 25));

        // Créer un JScrollPane pour rendre la JList scrollable
        JScrollPane quantityScrollPane = new JScrollPane(quantityJList);
        quantityScrollPane.setBounds(950, 320, 145, 50);
        mainPanel.add(quantityScrollPane);


        // Personnaliser l'apparence du sélecteur de date pour la date d'expiration
        expirationDatePicker.setBounds(800, 420, 300, 25);
        mainPanel.add((Component) expirationDatePicker);

        // Ajout des écouteurs d'événements
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Logique à exécuter lorsque le bouton "Add" est cliqué
                String name = nameTextField.getText();
                double quantity = (double) quantitySpinner.getValue();
                Date selectedDate = (Date) expirationDatePicker.getModel().getValue();

                // Get the selected quantity value from the JList
                String selectedQuantity = quantityJList.getSelectedValue();
                
                // Vérifier si tous les champs sont remplis
                if (name.isEmpty() || quantity <= 0 || selectedDate == null || selectedQuantity == null) {
                    JOptionPane.showMessageDialog(AddIngredientView.this, "Please complete all fields.");
                    return;
                }


                // Vérifier si l'ingrédient n'est pas déjà présent dans la base de données
                if (!isIngredientAlreadyPresent(name)) {
                    // Ajouter l'ingrédient à la base de données
                    try {
                        addIngredientToDatabase(name, quantity, selectedQuantity, selectedDate);
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }

                    new MenuView(user,menuView.getIsAliceMode());
                    dispose(); // Ferme la fenêtre
                } else {
                    JOptionPane.showMessageDialog(AddIngredientView.this, "The ingredient is already present in the database.");
                }
            }
        });

        // Ajouter les composants dans le panel
        mainPanel.add(addButton);
        mainPanel.add(backPanel);

        // Ajouter le panel principal à la fenêtre
        add(mainPanel);

        // Rendre la fenêtre visible
        setVisible(true);
    }
    
    // Vérifier si l'ingrédient est déjà présent dans la base de données
    private boolean isIngredientAlreadyPresent(String name) {
        ArrayList<Ingredient> ingredients = (ArrayList<Ingredient>) DataBaseConnection.getListeFrigo(user.getUserID());
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // Ajouter l'ingrédient à la base de données
    private void addIngredientToDatabase(String name, double quantity, String selectedQuantity, Date selectedDate) throws ParseException {
        String formattedDate = formatDate(selectedDate);
        String quantityWithUnit = quantity + " " + selectedQuantity;
        DataBaseConnection.addIngredientToList(user.getUserID(), name, quantityWithUnit, formattedDate);
    }

    // Formater la date
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
}
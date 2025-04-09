package fr.tse.fise2.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.User;
import fr.tse.fise2.model.alertCouleur;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class AlertsView extends JFrame {
	

    private User user;
    private JTable ingredientsTable;
    private DefaultTableModel tableModel;
    private List<Ingredient> ingredientsRecipes;
    private MenuView menuView;
	private BotButtonsManager botButtonsManager;

    public AlertsView(User user_,MenuView menuView) {
        this.user = user_;
        ingredientsRecipes = new ArrayList<Ingredient>();
        this.menuView = menuView;
        botButtonsManager = new BotButtonsManager(this,menuView);
        JPanel botPanel = botButtonsManager.getButtonsPanel();
        botPanel.setOpaque(false); // Pour rendre le JPanel transparent
        botPanel.setBounds(180, 680, 800, 100);
        
        // Configurer la fenêtre principale
        setTitle("Frigo List");
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
	    
	    // Configure the "Find recipes" button
        JButton findRecipesButton = new JButton("Find Recipes");
        findRecipesButton.setBounds(1090, 635, 150, 40);
        findRecipesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	new RecipesView(user, ingredientsRecipes, menuView);

                dispose();
            }
        });
        mainPanel.add(findRecipesButton);

        // Créer le modèle de table
        tableModel = new DefaultTableModel(new Object[]{"Name", "Expiration Date", "Alert", "Select"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        // Créer la JTable avec le modèle
        ingredientsTable = new JTable(tableModel);
        
        // Set the cell renderer for the table to customize font and size
        ingredientsTable.setDefaultRenderer(Object.class, new CustomCellRenderer());
        ingredientsTable.setRowHeight(45);
        
        // Set the font and height for the table header
        JTableHeader header = ingredientsTable.getTableHeader();
        header.setDefaultRenderer(new CustomHeaderRenderer());
        ingredientsTable.setTableHeader(header);


        // Ajouter une colonne de boutons de suppression personnalisés
        ingredientsTable.getColumnModel().getColumn(2).setCellRenderer(new AlertRenderer());
        ingredientsTable.getColumnModel().getColumn(3).setCellRenderer(new CheckBoxRenderer());
        ingredientsTable.getColumnModel().getColumn(3).setCellEditor(new CheckBoxEditor(new JCheckBox()));

        // Ajouter une liste déroulante pour afficher les ingrédients
        JScrollPane scrollPane = new JScrollPane(ingredientsTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
	    scrollPane.setBounds(400, 130, 840, 500);
	    
	    mainPanel.add(scrollPane);
        mainPanel.add(backPanel);

        // Ajouter le panel principal à la fenêtre
        add(mainPanel);

        // Charger les ingrédients depuis la base de données
        loadIngredients();

        // Rendre la fenêtre visible
        setVisible(true);
    }
    
    public List<Ingredient> getListAlerts(){
    	List<Ingredient> ingredients = DataBaseConnection.getListeFrigo(user.getUserID());
    	List<Ingredient> alertsList = new ArrayList<Ingredient>();
    	
    	for (Ingredient ingredient : ingredients) {
    		if(ingredient.getAlert() == alertCouleur.RED)
    			alertsList.add(ingredient);
    	}
    	
    	return alertsList;
    }

    private void loadIngredients() {
        // Récupérer la liste des ingrédients depuis la base de données
        List<Ingredient> ingredients = getListAlerts();

        if (!ingredients.isEmpty()) {
            // Vider le modèle de la table
            tableModel.setRowCount(0);

            for (Ingredient ingredient : ingredients) {
                // Initialize rowData array
                Object[] rowData = null;

                rowData = new Object[]{
                        ingredient.getName(),
                        ingredient.getExpirationDate(),
                        ingredient.getAlert(),
                        Boolean.FALSE
                };
                tableModel.addRow(rowData);
            }

            // Notify the table that the data has changed
            tableModel.fireTableDataChanged();
        } else {
            // If the list is empty, set the table model to null and reinitialize it
            tableModel = new DefaultTableModel(new Object[]{"Name", "Expiration Date", "Alert", "Select"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 3;
                }
            };

            // Set the new table model to the JTable
            ingredientsTable.setModel(tableModel);

            // Notify the table that the data has changed
            tableModel.fireTableDataChanged();
        }
    }
    

    private class AlertRenderer extends JPanel implements TableCellRenderer {
        private Color alertColor;

        public AlertRenderer() {
            setOpaque(false);
            setPreferredSize(new Dimension(20, 20)); // Set the preferred size of the circle
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            alertColor = getAlertColor(value.toString());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Draw a filled circle with the specified color
            g2d.setColor(alertColor);
            int diameter = Math.min(getWidth(), getHeight())-20;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g2d.fill(new Ellipse2D.Double(x, y, diameter, diameter));
        }

        private Color getAlertColor(String alert) {
            switch (alert) {
                case "RED":
                    return Color.RED;
                case "ORANGE":
                    return Color.ORANGE;
                case "GREEN":
                    return Color.GREEN;
                default:
                    return Color.WHITE; // Default color if the value is not recognized
            }
        }
    }
    private class CustomCellRenderer extends DefaultTableCellRenderer {
        Font font = new Font("Comic Sans MS", Font.BOLD, 13);

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            component.setFont(font);
            return component;
        }
    }
    
    private class CustomHeaderRenderer extends DefaultTableCellRenderer {
        Font font = new Font("Comic Sans MS", Font.BOLD, 15);
        int headerHeight = 40;

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(font);
            label.setBackground(new Color(253, 185, 76));
            label.setForeground(new Color(37,138,184));
            label.setPreferredSize(new Dimension(label.getWidth(), headerHeight));
            label.setHorizontalAlignment(JLabel.CENTER);
            return label;
        }
    }
    
    // Custom renderer for the checkbox column
    private class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckBoxRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected((value != null && ((Boolean) value)));
            return this;
        }
    }

    // Custom editor for the checkbox column
    private class CheckBoxEditor extends DefaultCellEditor {
        private int currentRow;

        public CheckBoxEditor(JCheckBox checkBox) {
            super(checkBox);
            checkBox.setHorizontalAlignment(JLabel.CENTER);
            checkBox.setPreferredSize(new Dimension(20, 20));
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();

                    // When the checkbox is clicked, check its state
                    boolean isChecked = checkBox.isSelected();
                    List<Ingredient> ingredients = DataBaseConnection.getListeFrigo(user.getUserID());

                    // If checked, add the corresponding ingredient to the recipes list
                    if (isChecked && currentRow >= 0 && currentRow < ingredients.size()) {
                        Ingredient selectedIngredient = ingredients.get(currentRow);
                        ingredientsRecipes.add(selectedIngredient);
                        
                    } else {
                        // If unchecked, remove the corresponding ingredient from the recipes list
                        if (currentRow >= 0 && currentRow < ingredientsRecipes.size()) {
                            ingredientsRecipes.remove(currentRow);
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return super.getCellEditorValue();
        }
    }
}

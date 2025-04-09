package fr.tse.fise2.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import fr.tse.fise2.backend.DataBaseConnection;
import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.User;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class IngredientsListView extends JFrame {

    private User user;
    private JTable ingredientsTable;
    private DefaultTableModel tableModel;
    private List<Ingredient> ingredientsRecipes;
    private MenuView menuView;
	private BotButtonsManager botButtonsManager;

    public IngredientsListView(User user_,MenuView menuView) {
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
            	new RecipesView(user, ingredientsRecipes,menuView);

                dispose();
            }
        });
        mainPanel.add(findRecipesButton);

        // Créer le modèle de table
        tableModel = new DefaultTableModel(new Object[]{"Name", "Quantity", "Expiration Date", "Alert", "Decrement", "Increment", "Delete", "Select"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5 || column == 6 || column == 7;
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
        ingredientsTable.getColumnModel().getColumn(3).setCellRenderer(new AlertRenderer());
        ingredientsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        ingredientsTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        ingredientsTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        ingredientsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        ingredientsTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        ingredientsTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
        ingredientsTable.getColumnModel().getColumn(7).setCellRenderer(new CheckBoxRenderer());
        ingredientsTable.getColumnModel().getColumn(7).setCellEditor(new CheckBoxEditor(new JCheckBox()));

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

    private void loadIngredients() {
        // Récupérer la liste des ingrédients depuis la base de données
        List<Ingredient> ingredients = DataBaseConnection.getListeFrigo(user.getUserID());

        if (!ingredients.isEmpty()) {
            // Vider le modèle de la table
            tableModel.setRowCount(0);

            for (Ingredient ingredient : ingredients) {
                // Initialize rowData array
                Object[] rowData = null;

                rowData = new Object[]{
                        ingredient.getName(),
                        ingredient.getQuantity(),
                        ingredient.getExpirationDate(),
                        ingredient.getAlert(),
                        "-",
                        "+",
                        "Delete",
                        Boolean.FALSE
                };
                tableModel.addRow(rowData);
            }

            // Notify the table that the data has changed
            tableModel.fireTableDataChanged();
        } else {
            // If the list is empty, set the table model to null and reinitialize it
            tableModel = new DefaultTableModel(new Object[]{"Name", "Quantity", "Expiration Date", "Alert", "Decrement", "Increment", "Delete", "Select"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 4 || column == 5 || column == 6;
                }
            };

            // Set the new table model to the JTable
            ingredientsTable.setModel(tableModel);

            // Notify the table that the data has changed
            tableModel.fireTableDataChanged();
        }
    }


    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    // Update the table
                    loadIngredients();
                }
            });
        }
       

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText(value.toString());
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
            	// Get the list of ingredients
                List<Ingredient> ingredients = DataBaseConnection.getListeFrigo(user.getUserID());

                // Check if the list is not empty and if the currentRow is within bounds
                if (!ingredients.isEmpty() && currentRow >= 0 && currentRow < ingredients.size()) {
                    int userID = user.getUserID();
                    int ingredientID = ingredients.get(currentRow).getID_Ingredient();

	                // Check if any column is selected
	                int column = ingredientsTable.getSelectedColumn();
	                if (column != -1) {
	                    switch (column) {
	                        case 4: // Decrement button
	                            // Handle increment logic and update the database
	                            updateQuantity(false, ingredientID);
	                            break;
	                        case 5: // Increment button
	                            updateQuantity(true, ingredientID);
	                            break;
	                        case 6: // Delete button
	                            // Handle delete logic and update the database
	                            DataBaseConnection.deleteIngredient(userID, ingredientID);
	                            break;
	                        default:
	                            break;
                    }
                }
            }
            isPushed = false;
            }

            if (button == null) {
                return null;
            }
            return button.getText();
        }

        
        // Helper method to calculate the updated quantity
        private String getUpdatedQuantity(boolean increment, int ingredientID) {
            // Retrieve the current quantity from the database
            String currentQuantity = DataBaseConnection.getListeFrigo(user.getUserID())
                    .stream()
                    .filter(ingredient -> ingredient.getID_Ingredient() == ingredientID)
                    .findFirst()
                    .map(Ingredient::getQuantity)
                    .orElse("");

            // Split the quantity into value and unit
            String[] parts = currentQuantity.split(" ");
            if (parts.length == 2) {
                String valueStr = parts[0].trim();
                String unit = parts[1].trim();

                try {
                    // Parse the value as an integer
                    float value = Float.parseFloat(valueStr);

                    // Increment or decrement the value based on the flag
                    if (increment) {
                    	if(unit.equals("pcs"))
                    		value++;
                    	if(unit.equals("g"))
                    		value = value + 10;
                    	if(unit.equals("l"))
                    		value = (float) (value + 0.5);
                    } else {
                    	if(unit.equals("pcs"))
                    		value = Math.max(0, value - 1); // Ensure the value doesn't go below 0
                    	if(unit.equals("g"))
                    		value = Math.max(0, value - 10); // Ensure the value doesn't go below 0
                    	if(unit.equals("l"))
                    		value = (float) Math.max(0, value - 0.5); // Ensure the value doesn't go below 0
                        if(value==0) {
                        	DataBaseConnection.deleteIngredient(user.getUserID(), ingredientID);
                        	return null;
                        }
                    }

                    // Convert the updated value back to string
                    String updatedValue = Float.toString(value);

                    // Construct the updated quantity string
                    return updatedValue + " " + unit;
                } catch (NumberFormatException e) {
                    // Handle the case where parsing the value as an integer fails
                    e.printStackTrace();
                }
            }

            // Return the original quantity if parsing fails or the format is unexpected
            return currentQuantity;
        }

        private void updateQuantity(boolean increment, int ingredientID) {
        	String newQuantity = getUpdatedQuantity(increment, ingredientID);
        	if(newQuantity==null) {
        		DataBaseConnection.deleteIngredient(user.getUserID(), ingredientID);
        	}
        	else {
        		DataBaseConnection.updateIngredientQuantity(user.getUserID(), ingredientID, newQuantity);
        	}
		}


		@Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
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

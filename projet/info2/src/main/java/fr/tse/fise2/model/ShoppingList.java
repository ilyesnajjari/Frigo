package fr.tse.fise2.model;

import fr.tse.fise2.model.Ingredient;
import fr.tse.fise2.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {
    private List<Ingredient> ingredients;

    public ShoppingList() {
        this.ingredients = new ArrayList<>();
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public void removeIngredient(Ingredient ingredient) {
        this.ingredients.remove(ingredient);
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public void clearList() {
        this.ingredients.clear();
    }

    public void generateFromRecipes(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            List<Ingredient> recipeIngredients = recipe.getIngredients();
            this.ingredients.addAll(recipeIngredients);
        }
    }
}

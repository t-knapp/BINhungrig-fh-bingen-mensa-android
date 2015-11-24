package de.fhbingen.mensa.data;

import java.util.List;

import de.fhbingen.mensa.data.orm.Building;
import de.fhbingen.mensa.data.orm.Date;
import de.fhbingen.mensa.data.orm.Delete;
import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.Ingredient;
import de.fhbingen.mensa.data.orm.OfferedAt;
import de.fhbingen.mensa.data.orm.Rating;
import de.fhbingen.mensa.data.orm.Sequence;

/**
 * Created by tknapp on 07.11.15.
 */
public class Changes {

    private List<Building> buildings;

    private List<Ingredient> ingredients;

    private List<Dish> dishes;

    private Sequence sequence;

    private List<Delete> deletes;

    private List<Rating> ratings;

    private List<Date> dates;

    private List<OfferedAt> offeredAt;

    public List<OfferedAt> getOfferedAt() {
        return offeredAt;
    }

    public List<Delete> getDeletes() {
        return deletes;
    }

    public List<Date> getDates() {
        return dates;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public boolean needToUpdate;

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}

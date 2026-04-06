"""
Tests for meals_classifier.py — classification logic.
"""

import sys
import os
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
import meals_classifier


# =====================================================================
# matches_addon_pattern
# =====================================================================

class TestMatchesAddonPattern:
    """Tests for matches_addon_pattern()."""

    # --- Breads / bases ---
    def test_flour_tortilla(self):
        assert meals_classifier.matches_addon_pattern("Flour Tortilla") is True

    def test_corn_tortilla(self):
        assert meals_classifier.matches_addon_pattern("Corn Tortilla") is True

    def test_plain_bagel(self):
        assert meals_classifier.matches_addon_pattern("Plain Bagel") is True

    def test_everything_bagel(self):
        assert meals_classifier.matches_addon_pattern("Everything Bagel") is True

    def test_pizza_dough(self):
        assert meals_classifier.matches_addon_pattern("Pizza Dough") is True

    def test_white_bread(self):
        assert meals_classifier.matches_addon_pattern("White Bread") is True

    def test_ciabatta_bun(self):
        assert meals_classifier.matches_addon_pattern("Ciabatta Bun") is True

    # --- Sauces / dressings ---
    def test_ranch_dressing(self):
        assert meals_classifier.matches_addon_pattern("Ranch Dressing") is True

    def test_bbq_sauce(self):
        assert meals_classifier.matches_addon_pattern("BBQ Sauce") is True

    def test_cholula_hot_sauce(self):
        assert meals_classifier.matches_addon_pattern("Cholula Hot Sauce") is True

    def test_ketchup(self):
        assert meals_classifier.matches_addon_pattern("Ketchup") is True

    def test_guacamole(self):
        assert meals_classifier.matches_addon_pattern("Guacamole") is True

    def test_pico_de_gallo(self):
        assert meals_classifier.matches_addon_pattern("Pico De Gallo") is True

    # --- Cheeses ---
    def test_shredded_cheddar(self):
        assert meals_classifier.matches_addon_pattern("Shredded Cheddar") is True

    def test_provolone(self):
        assert meals_classifier.matches_addon_pattern("Provolone Cheese") is True

    def test_feta(self):
        assert meals_classifier.matches_addon_pattern("Crumbled Feta") is True

    # --- Proteins as ingredients ---
    def test_bacon(self):
        assert meals_classifier.matches_addon_pattern("Bacon") is True

    def test_egg_patty(self):
        assert meals_classifier.matches_addon_pattern("Egg Patty") is True

    def test_pepperoni(self):
        assert meals_classifier.matches_addon_pattern("Pepperoni") is True

    def test_scrambled_eggs(self):
        assert meals_classifier.matches_addon_pattern("Scrambled Eggs") is True

    def test_turkey_bacon(self):
        assert meals_classifier.matches_addon_pattern("Turkey Bacon") is True

    # --- Vegetables ---
    def test_spinach(self):
        assert meals_classifier.matches_addon_pattern("Spinach") is True

    def test_lettuce_leaf(self):
        assert meals_classifier.matches_addon_pattern("Lettuce Leaf") is True

    def test_avocado(self):
        assert meals_classifier.matches_addon_pattern("Avocado") is True

    def test_broccoli(self):
        assert meals_classifier.matches_addon_pattern("Steamed Broccoli") is True

    # --- Toppings ---
    def test_granola(self):
        assert meals_classifier.matches_addon_pattern("Granola") is True

    def test_whipped_cream(self):
        assert meals_classifier.matches_addon_pattern("Whipped Cream") is True

    def test_chia_seeds(self):
        assert meals_classifier.matches_addon_pattern("Chia Seeds") is True

    # --- Spreads ---
    def test_cream_cheese(self):
        assert meals_classifier.matches_addon_pattern("Plain Cream Cheese") is True

    def test_peanut_butter(self):
        assert meals_classifier.matches_addon_pattern("Creamy Peanut Butter") is True

    def test_hummus(self):
        assert meals_classifier.matches_addon_pattern("Roasted Garlic Hummus") is True

    # --- Starches ---
    def test_jasmine_rice(self):
        assert meals_classifier.matches_addon_pattern("Jasmine Rice") is True

    def test_penne_pasta(self):
        assert meals_classifier.matches_addon_pattern("Penne Pasta") is True

    # --- Milk ---
    def test_oat_milk(self):
        assert meals_classifier.matches_addon_pattern("Oat Milk") is True

    def test_2_percent_milk(self):
        assert meals_classifier.matches_addon_pattern("2% Milk") is True

    # --- NOT addon ---
    def test_chicken_parmesan_not_addon(self):
        assert meals_classifier.matches_addon_pattern("Chicken Parmesan") is False

    def test_turkey_club_sandwich_not_addon(self):
        assert meals_classifier.matches_addon_pattern("Turkey Club Sandwich") is False

    def test_cheese_pizza_not_addon(self):
        # "Cheese Pizza" should NOT match because "pizza" is not a cheese pattern
        assert meals_classifier.matches_addon_pattern("Cheese Pizza") is False


# =====================================================================
# matches_item_pattern
# =====================================================================

class TestMatchesItemPattern:
    """Tests for matches_item_pattern()."""

    def test_sandwich(self):
        assert meals_classifier.matches_item_pattern("Turkey Club Sandwich") is True

    def test_burger(self):
        assert meals_classifier.matches_item_pattern("Veggie Burger") is True

    def test_pizza(self):
        assert meals_classifier.matches_item_pattern("Cheese Pizza") is True

    def test_bowl(self):
        assert meals_classifier.matches_item_pattern("Chicken Bowl") is True

    def test_wrap(self):
        assert meals_classifier.matches_item_pattern("Chicken Wrap") is True

    def test_quesadilla(self):
        assert meals_classifier.matches_item_pattern("Cheese Quesadilla") is True

    def test_smoothie(self):
        assert meals_classifier.matches_item_pattern("Berry Smoothie") is True

    def test_latte(self):
        assert meals_classifier.matches_item_pattern("Vanilla Latte") is True

    def test_pancakes(self):
        assert meals_classifier.matches_item_pattern("Buttermilk Pancakes") is True

    def test_omelette(self):
        assert meals_classifier.matches_item_pattern("Western Omelette") is True

    def test_ramen(self):
        assert meals_classifier.matches_item_pattern("Spicy Miso Ramen") is True

    def test_nuggets(self):
        assert meals_classifier.matches_item_pattern("Chicken Nuggets") is True

    def test_hot_dog(self):
        assert meals_classifier.matches_item_pattern("Classic Hot Dog") is True

    def test_biryani(self):
        assert meals_classifier.matches_item_pattern("Chicken Biryani") is True

    # --- Brands ---
    def test_donatos(self):
        assert meals_classifier.matches_item_pattern("Donato's Pepperoni Pizza") is True

    def test_sloopys(self):
        assert meals_classifier.matches_item_pattern("Sloopy's Burger") is True

    # --- NOT item ---
    def test_bacon_not_item(self):
        assert meals_classifier.matches_item_pattern("Bacon") is False

    def test_flour_tortilla_not_item(self):
        assert meals_classifier.matches_item_pattern("Flour Tortilla") is False

    def test_spinach_not_item(self):
        assert meals_classifier.matches_item_pattern("Spinach") is False

    def test_ketchup_not_item(self):
        assert meals_classifier.matches_item_pattern("Ketchup") is False


# =====================================================================
# classify_single_item
# =====================================================================

class TestClassifySingleItem:
    """Tests for classify_single_item()."""

    def _make_item(self, name, section, calories):
        return {"item": name, "section": section, "nutrition": {"calories": calories}}

    # --- ALL_ITEMS_SECTIONS always returns "item" ---
    def test_featured_items_section(self):
        item = self._make_item("Bacon", "Featured Items", 50)
        assert meals_classifier.classify_single_item(item) == "item"

    def test_grab_and_go_section(self):
        item = self._make_item("Ketchup", "Grab & Go", 10)
        assert meals_classifier.classify_single_item(item) == "item"

    def test_soups_section(self):
        item = self._make_item("Tomato Soup", "Soups", 150)
        assert meals_classifier.classify_single_item(item) == "item"

    def test_beverages_section(self):
        item = self._make_item("Coffee", "Beverages & Milkshakes", 5)
        assert meals_classifier.classify_single_item(item) == "item"

    # --- Addon patterns override generic sections ---
    def test_addon_pattern_in_generic_section(self):
        item = self._make_item("Flour Tortilla", "Other", 120)
        assert meals_classifier.classify_single_item(item) == "addon_ingredient"

    def test_bacon_in_generic_section(self):
        item = self._make_item("Bacon", "Grill", 159)
        assert meals_classifier.classify_single_item(item) == "addon_ingredient"

    # --- Item patterns ---
    def test_sandwich_recognized(self):
        item = self._make_item("Turkey Club Sandwich", "Deli", 450)
        assert meals_classifier.classify_single_item(item) == "item"

    def test_burger_recognized(self):
        item = self._make_item("Classic Burger", "Hot Bar", 500)
        assert meals_classifier.classify_single_item(item) == "item"

    # --- Smoothies section heuristic ---
    def test_smoothie_low_cal_is_addon(self):
        item = self._make_item("Whey Protein", "Smoothies", 10)
        assert meals_classifier.classify_single_item(item) == "addon_ingredient"

    def test_smoothie_high_cal_is_item(self):
        item = self._make_item("Mango Berry Smoothie", "Smoothies", 300)
        assert meals_classifier.classify_single_item(item) == "item"

    # --- Pizza section heuristic ---
    def test_pizza_low_cal_is_addon(self):
        item = self._make_item("Pizza Sauce", "Pizza", 30)
        assert meals_classifier.classify_single_item(item) == "addon_ingredient"

    def test_pizza_high_cal_is_item(self):
        item = self._make_item("Pepperoni Pizza Slice", "Pizza", 400)
        assert meals_classifier.classify_single_item(item) == "item"

    # --- General heuristic ---
    def test_high_cal_long_name_is_item(self):
        item = self._make_item("Slow Roasted BBQ Pulled Pork", "Action", 350)
        assert meals_classifier.classify_single_item(item) == "item"

    def test_low_cal_short_name_is_addon(self):
        item = self._make_item("Salt", "Other", 0)
        assert meals_classifier.classify_single_item(item) == "addon_ingredient"

    # --- BUILD_HEAVY_SECTIONS short name → addon ---
    def test_build_heavy_short_name(self):
        item = self._make_item("Rice", "Poke Bowls", 130)
        assert meals_classifier.classify_single_item(item) == "addon_ingredient"

    # --- Griddle section → item ---
    def test_griddle_is_item(self):
        item = self._make_item("Pancake", "Griddle", 200)
        assert meals_classifier.classify_single_item(item) == "item"


# =====================================================================
# classify_addon_type
# =====================================================================

class TestClassifyAddonType:
    """Tests for classify_addon_type()."""

    # --- Burrito section ---
    def test_burrito_tortilla_required(self):
        assert meals_classifier.classify_addon_type("Flour Tortilla", "Burrito") == "required"

    def test_burrito_sour_cream_optional(self):
        assert meals_classifier.classify_addon_type("Sour Cream", "Burrito") == "optional"

    def test_burrito_tortilla_chips_required(self):
        assert meals_classifier.classify_addon_type("Tortilla Chips", "Burrito") == "required"

    # --- Omelets section ---
    def test_omelets_eggs_required(self):
        assert meals_classifier.classify_addon_type("Eggs", "Omelets") == "required"

    def test_omelets_egg_whites_required(self):
        assert meals_classifier.classify_addon_type("Egg Whites", "Omelets") == "required"

    def test_omelets_mushrooms_optional(self):
        assert meals_classifier.classify_addon_type("Mushrooms", "Omelets") == "optional"

    # --- Poke Bowls ---
    def test_poke_rice_required(self):
        assert meals_classifier.classify_addon_type("Jasmine Rice", "Poke Bowls") == "required"

    def test_poke_topping_optional(self):
        assert meals_classifier.classify_addon_type("Sesame Seeds", "Poke Bowls") == "optional"

    # --- Pizza ---
    def test_pizza_dough_required(self):
        assert meals_classifier.classify_addon_type("Pizza Dough", "Pizza") == "required"

    def test_pizza_mozzarella_required(self):
        assert meals_classifier.classify_addon_type("Shredded Mozzarella", "Pizza") == "required"

    def test_pizza_pepperoni_optional(self):
        assert meals_classifier.classify_addon_type("Pepperoni", "Pizza") == "optional"

    # --- Subs ---
    def test_subs_bun_required(self):
        assert meals_classifier.classify_addon_type("Ciabatta Bun", "Subs") == "required"

    def test_subs_lettuce_optional(self):
        assert meals_classifier.classify_addon_type("Lettuce", "Subs") == "optional"

    # --- Salad Bar ---
    def test_salad_lettuce_required(self):
        assert meals_classifier.classify_addon_type("Chopped Romaine Lettuce", "Salad Bar") == "required"

    def test_salad_croutons_optional(self):
        assert meals_classifier.classify_addon_type("Croutons", "Salad Bar") == "optional"

    # --- Mongolian ---
    def test_mongolian_noodles_required(self):
        assert meals_classifier.classify_addon_type("Lo Mein Noodles", "Mongolian") == "required"

    def test_mongolian_rice_required(self):
        assert meals_classifier.classify_addon_type("Brown Rice", "Mongolian") == "required"

    # --- Yogurt Bar ---
    def test_yogurt_bar_yogurt_required(self):
        assert meals_classifier.classify_addon_type("Greek Yogurt", "Yogurt Bar") == "required"

    def test_yogurt_bar_granola_optional(self):
        assert meals_classifier.classify_addon_type("Granola", "Yogurt Bar") == "optional"

    # --- Unknown section ---
    def test_unknown_section_always_optional(self):
        assert meals_classifier.classify_addon_type("Anything", "Unknown Section") == "optional"

    # --- Grill ---
    def test_grill_bun_required(self):
        assert meals_classifier.classify_addon_type("Brioche Bun", "Grill") == "required"


# =====================================================================
# _make_synthetic_item
# =====================================================================

class TestMakeSyntheticItem:
    """Tests for _make_synthetic_item()."""

    def test_basic_structure(self):
        sample = {
            "place": "Traditions at Kennedy",
            "item": "Flour Tortilla",
            "section": "Burrito",
            "hours": "Mon-Fri: 7am-9pm",
            "address": "251 W. 12th Ave",
            "dietary": {"vegan": False, "vegetarian": True, "no-gluten": False, "halal": False},
            "allergens": {"coconut": False, "dairy": False},
        }
        result = meals_classifier._make_synthetic_item(sample, "Build Your Own Burrito")
        assert result["place"] == "Traditions at Kennedy"
        assert result["item"] == "Build Your Own Burrito"
        assert result["section"] == "Burrito"
        assert result["hours"] == "Mon-Fri: 7am-9pm"
        assert result["address"] == "251 W. 12th Ave"

    def test_is_synthetic_true(self):
        sample = {
            "place": "X", "item": "Y", "section": "Z",
            "hours": "", "address": "",
            "dietary": {}, "allergens": {},
        }
        result = meals_classifier._make_synthetic_item(sample, "Build Your Own Z")
        assert result["is_synthetic"] is True

    def test_item_type_is_item(self):
        sample = {
            "place": "X", "item": "Y", "section": "Z",
            "hours": "", "address": "",
            "dietary": {}, "allergens": {},
        }
        result = meals_classifier._make_synthetic_item(sample, "Build Your Own Z")
        assert result["item_type"] == "item"

    def test_nutrition_is_none(self):
        sample = {
            "place": "X", "item": "Y", "section": "Z",
            "hours": "", "address": "",
            "dietary": {}, "allergens": {},
        }
        result = meals_classifier._make_synthetic_item(sample, "Build Your Own Z")
        assert result["nutrition"]["protein"] is None
        assert result["nutrition"]["carbs"] is None
        assert result["nutrition"]["calories"] is None

    def test_parent_item_is_none(self):
        sample = {
            "place": "X", "item": "Y", "section": "Z",
            "hours": "", "address": "",
            "dietary": {}, "allergens": {},
        }
        result = meals_classifier._make_synthetic_item(sample, "Build Your Own Z")
        assert result["parent_item"] is None


# =====================================================================
# classify (full pipeline)
# =====================================================================

class TestClassify:
    """Tests for the full classify() pipeline."""

    def _make_raw_item(self, name, place, section, calories, **kwargs):
        item = {
            "place": place,
            "item": name,
            "section": section,
            "nutrition": {"protein": kwargs.get("protein", 10), "carbs": kwargs.get("carbs", 20), "calories": calories},
            "hours": kwargs.get("hours", ""),
            "address": kwargs.get("address", ""),
            "dietary": kwargs.get("dietary", {"vegan": False, "vegetarian": False, "no-gluten": False, "halal": False}),
            "allergens": kwargs.get("allergens", {"coconut": False, "dairy": False}),
        }
        return item

    def test_all_items_get_item_type(self):
        data = [
            self._make_raw_item("Chicken Sandwich", "Place A", "Featured Items", 450),
            self._make_raw_item("Flour Tortilla", "Place A", "Burrito", 120),
        ]
        result = meals_classifier.classify(data)
        for item in result:
            assert "item_type" in item

    def test_standalone_classified_as_item(self):
        data = [self._make_raw_item("Chicken Sandwich", "Place A", "Featured Items", 450)]
        result = meals_classifier.classify(data)
        assert result[0]["item_type"] == "item"

    def test_addon_classified_correctly(self):
        data = [
            self._make_raw_item("Chicken Sandwich", "Place A", "Deli", 450),
            self._make_raw_item("Flour Tortilla", "Place A", "Burrito", 120),
        ]
        result = meals_classifier.classify(data)
        tortilla = [r for r in result if r["item"] == "Flour Tortilla"][0]
        assert tortilla["item_type"] == "addon_ingredient"

    def test_orphan_group_gets_synthetic_parent(self):
        """Group with only addons and no items should get a synthetic parent."""
        data = [
            self._make_raw_item("Flour Tortilla", "Place B", "Burrito", 120),
            self._make_raw_item("Sour Cream", "Place B", "Burrito", 30),
        ]
        result = meals_classifier.classify(data)
        synthetic = [r for r in result if r.get("is_synthetic")]
        assert len(synthetic) >= 1
        assert synthetic[0]["item_type"] == "item"
        assert "Build Your Own" in synthetic[0]["item"]

    def test_addon_gets_parent_item(self):
        data = [
            self._make_raw_item("Chicken Sandwich", "Place A", "Deli", 450),
            self._make_raw_item("White Bread", "Place A", "Deli", 80),
        ]
        result = meals_classifier.classify(data)
        bread = [r for r in result if r["item"] == "White Bread"][0]
        assert bread["item_type"] == "addon_ingredient"
        assert bread["parent_item"] is not None
        assert "Chicken Sandwich" in bread["parent_item"]

    def test_addon_gets_addon_type(self):
        data = [
            self._make_raw_item("Chicken Sandwich", "Place A", "Subs", 450),
            self._make_raw_item("Ciabatta Bun", "Place A", "Subs", 150),
            self._make_raw_item("Lettuce Leaf", "Place A", "Subs", 5),
        ]
        result = meals_classifier.classify(data)
        bun = [r for r in result if r["item"] == "Ciabatta Bun"][0]
        lettuce = [r for r in result if r["item"] == "Lettuce Leaf"][0]
        assert bun["addon_type"] == "required"
        assert lettuce["addon_type"] == "optional"

    def test_item_has_no_parent(self):
        data = [self._make_raw_item("Chicken Sandwich", "Place A", "Featured Items", 450)]
        result = meals_classifier.classify(data)
        assert result[0]["parent_item"] is None

    def test_item_has_no_addon_type(self):
        data = [self._make_raw_item("Chicken Sandwich", "Place A", "Featured Items", 450)]
        result = meals_classifier.classify(data)
        assert result[0]["addon_type"] is None

    def test_synthetic_items_marked(self):
        data = [
            self._make_raw_item("Eggs", "Place C", "Omelets", 70),
            self._make_raw_item("Mushrooms", "Place C", "Omelets", 15),
        ]
        result = meals_classifier.classify(data)
        synthetic = [r for r in result if r.get("is_synthetic")]
        non_synthetic = [r for r in result if not r.get("is_synthetic")]
        assert len(synthetic) >= 1
        for s in synthetic:
            assert s["is_synthetic"] is True
        for ns in non_synthetic:
            assert ns["is_synthetic"] is False

    def test_empty_input(self):
        result = meals_classifier.classify([])
        assert result == []

    def test_preserves_original_fields(self):
        data = [
            self._make_raw_item("Test Item", "Place A", "Featured Items", 300,
                                hours="Mon-Fri: 8am-5pm", address="123 Main St"),
        ]
        result = meals_classifier.classify(data)
        assert result[0]["hours"] == "Mon-Fri: 8am-5pm"
        assert result[0]["address"] == "123 Main St"
        assert result[0]["nutrition"]["calories"] == 300

    def test_multiple_places_same_section(self):
        """Items from different places in the same section should have separate parent groups."""
        data = [
            self._make_raw_item("Flour Tortilla", "Place A", "Burrito", 120),
            self._make_raw_item("Flour Tortilla", "Place B", "Burrito", 120),
        ]
        result = meals_classifier.classify(data)
        # Both should get synthetic parents, and they should be distinct
        synthetic = [r for r in result if r.get("is_synthetic")]
        places = {s["place"] for s in synthetic}
        assert "Place A" in places
        assert "Place B" in places

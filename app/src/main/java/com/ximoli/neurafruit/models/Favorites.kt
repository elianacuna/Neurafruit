package com.ximoli.neurafruit.models

class Favorites {

    //variables
    var id: String = ""
    var uid: String = ""
    var name: String = ""
    var family: String = ""
    var order: String = ""
    var genus: String = ""
    var calories: String = ""
    var fat: String = ""
    var sugar: String = ""
    var carbohydrates: String = ""
    var image_fruit: String = ""

    //empty constructor
    constructor()
    constructor(id: String, uid: String, name: String, family: String, order: String, genus: String, calories: String, fat: String, sugar: String, carbohydrates: String, image_fruit: String)
    {
        this.id = id
        this.uid = uid
        this.name = name
        this.family = family
        this.order = order
        this.genus = genus
        this.calories = calories
        this.fat = fat
        this.sugar = sugar
        this.carbohydrates = carbohydrates
        this.image_fruit = image_fruit
    }

    //parameterized constructor



}
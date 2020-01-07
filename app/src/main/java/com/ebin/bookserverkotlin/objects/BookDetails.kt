package com.ebin.bookserverkotlin.objects

class BookDetails {

    var id: String = ""
        get() = field       // field here ~ `this.firstName` in Java
        set(value) {
            field = value
        }

    var title: String = ""
        get() = field       // field here ~ `this.firstName` in Java
        set(value) {
            field = value
        }

    var price: String = ""
        get() = field       // field here ~ `this.firstName` in Java
        set(value) {
            field = value
        }

    var currencyCode: String = ""
        get() = field       // field here ~ `this.firstName` in Java
        set(value) {
            field = value
        }

    var author: String = ""
        get() = field       // field here ~ `this.firstName` in Java
        set(value) {
            field = value
        }
}
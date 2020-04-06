package com.example.blindspot;

import androidx.annotation.NonNull;

/**
 * <h1>Cloth Class</h1>
 *
 * Contains cloth information.
 *
 * @author Tomer Ben Ari
 * @version 0.16.1
 * @since 0.12.0 (05/03/2020)
 */

public class Cloth {
    private String type;
    private String color;
    private String size;
    private Long amount;

    /**
     * Creates Cloth object with empty fields.
     */

    public Cloth(){
        this.type = "";
        this.color = "";
        this.size = "";
        this.amount = 0L;
    }

    /**
     * Creates Cloth object with the given type, color, size and amount.
     *
     * @param type Type of the cloth.
     * @param color Color of the cloth.
     * @param size Size of the cloth.
     * @param amount Amount of the cloth.
     */

    public Cloth(String type, String color, String size, Long amount){
        this.type = type;
        this.color = color;
        this.size = size;
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getAmount() {
        return amount;
    }

    /**
     * Converts cloth information into one string.
     *
     * @return Returns cloth information.
     */
    @NonNull
    @Override
    public String toString() {
        return (size + ";" + color + ";" + type + ";" + amount + " pcs.");
    }
}

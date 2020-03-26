package com.example.blindspot;

import androidx.annotation.NonNull;

/**
 * @author Tomer Ben Ari
 * @version 0.15.2
 * @since 0.12.0 (05/03/2020)
 *
 * Cloth class
 * <p>
 *     Contains cloth info
 * </p>
 */

public class Cloth {
    private String type;
    private String color;
    private String size;
    private Long amount;

    public Cloth(){
        this.type = "";
        this.color = "";
        this.size = "";
        this.amount = 0L;
    }

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

    @NonNull
    @Override
    public String toString() {
        return (size + ";" + color + ";" + type + ";" + amount + " pcs.");
    }
}

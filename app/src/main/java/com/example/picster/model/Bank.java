package com.example.picster.model;

public class Bank {
    private String bank, cardNumber;

    public Bank(String bank, String cardNumber) {
        this.bank = bank;
        this.cardNumber = cardNumber;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}

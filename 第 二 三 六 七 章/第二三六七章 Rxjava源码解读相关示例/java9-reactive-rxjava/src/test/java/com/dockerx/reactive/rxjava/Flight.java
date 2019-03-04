package com.dockerx.reactive.rxjava;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/2/20 16:02.
 */
public class Flight {
    private int discount;

    public Flight(int discount) {
        this.discount = discount;
    }

    public int getDiscount() {
        return discount;
    }

    public Flight setDiscount(int discount) {
        this.discount = discount;
        return this;
    }
}

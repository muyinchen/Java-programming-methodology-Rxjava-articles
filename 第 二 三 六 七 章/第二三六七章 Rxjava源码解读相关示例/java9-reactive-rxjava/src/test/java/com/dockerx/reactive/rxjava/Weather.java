package com.dockerx.reactive.rxjava;

import java.util.Random;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/2/17 1:26.
 */
public class Weather {
    private final Temperature temperature;

    private final Wind wind;

    private boolean sunny;

    public Weather setSunny(boolean sunny) {
        this.sunny = sunny;
        return this;
    }

    public boolean isSunny() {
        return sunny;
    }

    public Weather(Temperature temperature, Wind wind) {
        this.temperature = temperature;
        this.wind = wind;
    }
    public boolean computeSunny() {
        Random random = new Random();
        return random.nextInt(10)%2==0;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Wind getWind() {
        return wind;
    }
}

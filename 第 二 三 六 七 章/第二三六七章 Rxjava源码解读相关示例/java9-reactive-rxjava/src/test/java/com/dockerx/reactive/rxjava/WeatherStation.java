package com.dockerx.reactive.rxjava;

import io.reactivex.Observable;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/2/17 1:23.
 */
public interface WeatherStation {
    Observable<Temperature> temperature();
    Observable<Wind> wind();
}

class BasicWeatherStation implements WeatherStation {

    @Override
    public Observable<Temperature> temperature() {
        return Observable.just(new Temperature());
    }

    @Override
    public Observable<Wind> wind() {
        return Observable.just(new Wind());
    }
}

class Temperature {}

class Wind {}

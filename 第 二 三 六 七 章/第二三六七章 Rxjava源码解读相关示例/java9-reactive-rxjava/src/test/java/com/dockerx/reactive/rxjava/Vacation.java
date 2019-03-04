package com.dockerx.reactive.rxjava;

import io.reactivex.Observable;

import java.time.LocalDate;
import java.util.Random;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/2/20 16:05.
 */
public class Vacation {
    private  final City where;
    private  final LocalDate when;

    private  Flight flight;

    private  Hotel hotel;
    private  Weather weather;

    public LocalDate getWhen() {
        return when;
    }

    public Weather getWeather() {
        return weather;
    }

    public Vacation setWeather(Weather weather) {
        this.weather = weather;
        return this;
    }

    Vacation(City where, LocalDate when) {
        this.where = where;
        this.when = when;
    }


    public City getWhere() {
        return where;
    }

    public Flight getFlight() {
        return flight;
    }

    public Vacation setFlight(Flight flight) {
        this.flight = flight;
        return this;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public Vacation setHotel(Hotel hotel) {
        this.hotel = hotel;
        return this;
    }

    public Observable<Flight> cheapFlightFrom(City from) {
        //性价比最高的航班
        return Observable.just(new Flight(new Random().nextInt(5)+1));
    }

    public Observable<Hotel> cheapHotel() {
        //打折的酒店
        return Observable.just(new Hotel());
    }
}

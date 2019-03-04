package com.dockerx.reactive.rxjava;

import com.google.common.collect.ImmutableList;
import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.reactivex.Observable.interval;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/1/25 5:30.
 */
public class OperatorsTest<T> {

    private static void log(Object msg) {
        System.out.println(
                Thread.currentThread().getName() +
                        ": " + msg);
    }

    static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void filter_test() {
        String[] monthArray = {"Jan", "Feb", "Mar", "Apl", "Maly", "Jun",
                "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        Observable.fromArray(monthArray)
                  .filter(item -> item.contains("y"))
                  .filter(item -> item.contains("l"))
                  .count()
                  .subscribe(item -> System.out.println("we got: " +
                          item + " from the Observable"));
        System.out.println("######################################################");

        Observable.fromArray(monthArray)
                  .filter(item -> item.contains("y"))
                  .filter(item -> item.contains("l"))
                  .subscribe(item -> System.out.println("we got: " + item + " item from the Observable"),
                          Throwable::printStackTrace,
                          () -> System.out.println("I am Done!! Completed normally"));
    }

    @Test
    void mul_Subject() {

        PublishSubject<Object> publishSubject = PublishSubject.create();
        PublishSubject<Object> publishSubject1 = PublishSubject.create();
        PublishSubject<Object> publishSubject2 = PublishSubject.create();
        PublishSubject<Object> publishSubject3 = PublishSubject.create();

        publishSubject.subscribe(x -> {
                    log("一郎神: " + x);
                    publishSubject1.onNext(x);
                },
                throwable -> System.out.println("异常指向: 一郎神-> " + throwable.getMessage()),
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        publishSubject1.subscribe(x -> {
                    log("二郎神: " + x);
                    publishSubject2.onNext(x);
                },
                throwable -> publishSubject.onError(new RuntimeException("二郎神-> " + throwable.getMessage())),
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        publishSubject2.subscribe(x -> {
                    log("三郎神: " + x);
                    publishSubject3.onNext(x);
                },
                throwable -> publishSubject1.onError(new RuntimeException("三郎神-> " + throwable.getMessage())),
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        publishSubject3.subscribe(x -> {
                    log("四郎神: " + x);
                    if ((Long) x == 2L) throw new RuntimeException("四郎神来耍下宝");
                },
                throwable -> publishSubject2.onError(new RuntimeException(throwable.getMessage())),
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        publishSubject.onNext(1l);
        publishSubject.onNext(2l);

    }

    @Test
    void map_test() {
        String[] monthArray = {"Jan", "Feb", "Mar", "Apl", "Maly", "Jun",
                "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        Observable.fromArray(monthArray)
                  .map(String::toUpperCase)
                  .subscribe(System.out::println);

        Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                  .filter(i -> i % 3 > 0)
                  .map(i -> "#" + i * 10)
                  .filter(s -> s.length() < 4)
                  .subscribe(System.out::println);

    }

    @Test
    void flatmap_test() {
        Observable.range(1, 3).flatMap(item -> Observable.range(item,
                3)).subscribe(value -> System.out.print(value + "->"));
    }

    @Test
    void flatmap_test2() {
        List<List<String>> lists = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            List main_list = new ArrayList();
            lists.add(main_list);
            for (int j = 1; j < 3; j++) {
                main_list.add(i + "—— 打虎 ——" + j);
            }
        }
        Observable.fromArray(lists.toArray())
                  .map(item -> ((List) item).toArray())
                  .flatMap(Observable::fromArray)
                  .subscribe(System.out::println);
        System.out.println("++++++++++++++++++++++++++++++++++++++");

        Observable.fromArray(lists.toArray())
                  .flatMap(item -> Observable.fromArray(((List) item).toArray()))
                  .subscribe(System.out::println);
        System.out.println("++++++++++++++++++++++++++++++++++++++");
        Observable.fromArray(lists.toArray())
                  .flatMapIterable(item -> (List) item)
                  .subscribe(System.out::println);
    }

    Observable<Long> upload(int id) {
        return Observable.just(1L, 2L, 3L, 4L, 5L,
                6L, 7L, 8L, 9L, 10L);
    }

    Observable<String> rate(Integer id) {
        return Observable.just("上传完毕！");
    }

    @Test
    void flatMap3() {
        int id = new Random().nextInt(100);
        System.out.println(id);
        upload(id)
                .map(item -> {
                    if ((id % 2) == 1) {
                        return item;
                    } else throw new RuntimeException("我就是个BUG!!");
                })
                .flatMap(
                        bytes -> {
                            //System.out.println(bytes);
                            return Observable.empty();
                        },
                        e -> Observable.just(e.getMessage()),
                        () -> rate(id)
                )
                .subscribe(item -> System.out.println("we got: " + item.toString() + "  from the Observable"),
                        throwable -> System.out.println("异常-> " + throwable.getMessage()),
                        () -> System.out.println("Emission completed"));

    }

    @Test
    void scan_test() {
        Integer[] prices = {100, 200, 300, 15, 15};
        Observable.fromArray(prices).scan((item1, item2) -> item1 +
                item2).subscribe(integer -> System.out.println("amount: " + integer),
                Throwable::printStackTrace,
                () -> System.out.println("I am Done!! Completed normally"));
    }

    @Test
    void groupby_test() {
        String[] monthArray = {"January", "Feb", "March", "Apl",
                "May", "Jun", "July", "Aug", "Sept", "Oct", "Nov",
                "Dec"};

        Observable.fromArray(monthArray).groupBy(item -> item.length() <= 3 ?
                "THREE" : item.length() < 6 ?
                ">4" : "DEFAULT").subscribe(observable -> {
            // if (Objects.equals(observable.getKey(), ">4")) observable.subscribe(item -> System.out.println("选中的分组所包含元素: " + item));

            observable.subscribe(item -> log(item + ":" + observable.getKey()));
            System.out.println("*********************************************");
        });

    }


    @Test
    void groupby_test2() {
        String[] monthArray = {"January", "Feb", "March", "Apl",
                "May", "Jun", "July", "Aug", "Sept", "Oct", "Nov",
                "Dec"};

        Observable.fromArray(monthArray).groupBy(item -> {
            //sleep(1,TimeUnit.SECONDS);
            return item.length() <= 3 ?
                    "THREE" : item.length() < 6 ?
                    ">4" : "DEFAULT";
        }, String::toUpperCase).subscribe(observable -> {
            //if (Objects.equals(observable.getKey(), ">4")) observable.subscribe(item -> System.out.println("选中的分组所包含元素: " + item));

            observable.subscribe(item -> {
                sleep(2, TimeUnit.SECONDS);
                log(item + ":" + observable.getKey());
            });
            System.out.println("*********************************************");
        });

    }

    @Test
    void merg_test() {
        Observable.merge(Observable.range(1, 5), Observable.range(10, 3))
                  .subscribe(item -> System.out.println("we got: " + item.toString() + "  from the Observable"),
                          throwable -> System.out.println("异常-> " + throwable.getMessage()),
                          () -> System.out.println("Emission completed"));
    }

    @Test
    void zip_test() {
        final WeatherStation station = new BasicWeatherStation();

        Observable<Temperature> temperature = station.temperature();
        Observable<Wind> wind = station.wind();

        Observable.zip(temperature, wind, Weather::new);
        //temperature.zipWith(wind, Weather::new);
    }

    @Test
    void zip_test2() {
        final WeatherStation station = new BasicWeatherStation();

        Observable<Temperature> temperature = station.temperature();
        Observable<Wind> wind = station.wind();

        Observable<Weather> weatherObservable = Observable.zip(temperature, wind, Weather::new);
        //temperature.zipWith(wind, Weather::new);

        Observable<LocalDate> nextWeekDays = Observable.range(1, 7)
                                                       .map(i -> LocalDate.now().plusDays(i));
        Observable.fromArray(City.values())
                  .flatMap(city -> nextWeekDays.map(date -> new Vacation(city, date)))
                  .flatMap(vacation ->
                          Observable.zip(
                                  weatherObservable.filter(Weather::computeSunny),
                                  vacation.cheapFlightFrom(vacation.getWhere()),
                                  vacation.cheapHotel(),
                                  (w, f, h) -> {
                                      w.setSunny(true);
                                      return vacation.setWeather(w).setFlight(f).setHotel(h);
                                  }
                          ))
                  .subscribe(item -> System.out.println("we got: " + item.getWhere() + " 航班折扣：" + item.getFlight()
                                                                                                      .getDiscount() + "折  from the Observable"),
                          throwable -> System.out.println("异常-> " + throwable.getMessage()),
                          () -> System.out.println("Emission completed"));
    }

    @Test
    void zip_test3() {
        Integer[] numbers = {1, 2, 13, 34, 15, 17};
        String[] fruits = {"苹果", "梨", "李子", "荔枝",
                "芒果"};

        Observable<Integer> source1 = Observable.fromArray(numbers);
        Observable<String> source2 = Observable.fromArray(fruits);
        Observable<Integer> source3 = Observable.range(10, 3);

        Observable.zip(source1, source2, source3,
                (value1, value2, value3) -> value1 + ":" + value2 + "*" + value3)
                  .subscribe(item -> System.out.println("we got: " + item + "  from the Observable"),
                          throwable -> System.out.println("异常-> " + throwable.getMessage()),
                          () -> System.out.println("Emission completed"));
    }

    @Test
    void combineLatest_test() {
        Integer[] numbers = {1, 2, 13, 34, 15, 17};
        String[] fruits = {"苹果", "梨", "李子", "荔枝",
                "芒果"};

        Observable<Integer> source1 = Observable.fromArray(numbers);
        Observable<String> source2 = Observable.fromArray(fruits);
        Observable.combineLatest(source2, source1, (item1, item2) -> item1 + item2)
                  .subscribe(item -> System.out.println("we got: " + item + "  from the Observable"),
                          throwable -> System.out.println("异常-> " + throwable.getMessage()),
                          () -> System.out.println("Emission completed"));
    }

    @Test
    void combineLatest_test2() {
        Observable.combineLatest(
                interval(2, TimeUnit.SECONDS).map(x -> "Java" + x),
                interval(1, TimeUnit.SECONDS).map(x -> "Spring" + x),
                (s, f) -> f + ":" + s
        ).forEach(System.out::println);

        sleep(6, TimeUnit.SECONDS);
    }

    @Test
    void withLatestFrom_test() {
        Integer[] numbers = {1, 2, 13, 34, 15, 17};
        String[] fruits = {"苹果", "梨", "李子", "荔枝",
                "芒果"};
        // 此处加入delay通过对第一次发送元素的打的时间差来确定withLatestFrom带来的差异，以确定书中此处的论断
        Observable<Integer> source1 = Observable.fromArray(numbers).delay(2, TimeUnit.MILLISECONDS);
        Observable<String> source2 = Observable.fromArray(fruits).delay(3, TimeUnit.MILLISECONDS);
        source1.withLatestFrom(source2, (item1, item2) -> item1 + item2)
               .forEach(System.out::println);

        System.out.println("#################################");

        Observable<String> stringObservable1 = interval(2, TimeUnit.SECONDS).map(x -> "Java" + x);
        Observable<String> stringObservable2 = interval(1, TimeUnit.SECONDS).map(x -> "Spring" + x);

        stringObservable2.withLatestFrom(stringObservable1, (s, f) -> s + ":" + f)
                         .forEach(System.out::println);

        sleep(6, TimeUnit.SECONDS);
    }

    @Test
    void amb_test() {
        Integer[] numbers = {1, 2, 13, 34, 15, 17};
        String[] fruits = {"苹果", "梨", "李子", "荔枝",
                "芒果"};
        Observable<Integer> source1 = Observable.fromArray(numbers).delay(1, TimeUnit.SECONDS);
        Observable<String> source2 = Observable.fromArray(fruits);
        Observable.ambArray(source1, source2)
                  .forEach(System.out::println);
    }

    @Test
    void scan_test2() {
        Observable<BigInteger> scan2 = Observable
                .range(2, 10)
                .scan(BigInteger.ONE, (big, cur) ->
                        big.multiply(BigInteger.valueOf(cur)));
        scan2.forEach(System.out::println);
    }

    /*public <R> Observable<R> reduce(
            R initialValue,
            BiFunction<R, ? super T, R> reducer) {
        return Observable.scan(initialValue, reducer).takeLast(1);
    }*/

    @Test
    void reduce_test() {
        Observable
                .range(10, 20)
                .reduce(new ArrayList<>(), (list, item) -> {
                    list.add(item);
                    return list;
                }).subscribe(System.out::println);
    }

    @Test
    void reduce_test2() {
        Observable
                .range(10, 20)
                .reduce(BigInteger.ONE, (big, cur) ->
                        big.multiply(BigInteger.valueOf(cur))).subscribe(System.out::println);
    }

    @Test
    void collect_test() {
        Observable
                .range(10, 20)
                .collect(ArrayList::new, List::add)
                .subscribe(System.out::println);
    }

    @Test
    void distinct_test() {
        Observable<LocalDate> dates = Observable.just(
                LocalDate.of(2018, 1, 3),
                LocalDate.of(2018, 3, 4),
                LocalDate.of(2018, 1, 5),
                LocalDate.of(2018, 11, 3)
        );
        //	get	distinct	months
        dates.map(LocalDate::getMonth)
             .distinct()
             .subscribe(System.out::println);
        System.out.println("############################");
        //	get	distinct	days
        dates.distinct(LocalDate::getMonth)
             .subscribe(System.out::println);
    }

    @Test
    void distinctUntilChanged_test() {
        Observable.just(1, 1, 1, 2, 2, 3, 3, 2, 1, 1)
                  .distinctUntilChanged()
                  .subscribe(i -> System.out.println("RECEIVED:	" + i));
    }

    @Test
    void distinctUntilChanged_test2() {
         Function<Integer, Integer> flags_mask = flags -> flags == 0 ? 0
                : ~(flags | ((1 & flags) << 1) | ((2 & flags) >> 1));


         Consumer<Integer> consumer= System.out::println;

        Observable.just("Apple", "Orange", "Appla", "Eatla", "HOHO",
                "Meta")
                  .distinctUntilChanged(String::length)
                  .subscribe(i -> System.out.println("RECEIVED:	" + i));

        Integer apply = flags_mask.apply(5);
        System.out.println(apply);
        consumer.accept(3);
    }



    @Test
    void take_skip_test() {
        Observable<Integer> range = Observable.range(1, 5);
        range.take(3)
             .collect(ArrayList::new, List::add)
             .subscribe(System.out::println);  // [1, 2, 3]

        range.skip(3)
             .collect(ArrayList::new, List::add)
             .subscribe(System.out::println);  // [4, 5]
        range.skip(5)
             .collect(ArrayList::new, List::add)
             .subscribe(System.out::println);  // []


        range.takeLast(2)
             .collect(ArrayList::new, List::add)
             .subscribe(System.out::println);  // [4, 5]
        range.skipLast(2)
             .collect(ArrayList::new, List::add)
             .subscribe(System.out::println);  // [1, 2, 3]

        range.takeUntil(x -> x == 3)
             .collect(ArrayList::new, List::add)
             .subscribe(System.out::println);  // [1, 2, 3]
        range.takeWhile(x -> x != 3)
             .collect(ArrayList::new, List::add)
             .subscribe(System.out::println);  // [1, 2]

        range.all(x -> x != 4)
             .subscribe(System.out::println);    // [false]
        range.contains(4)
             .subscribe(System.out::println);         // [true]
    }

    @Test
    void compose_test() {
        Observable.just("Apple", "Orange", "Appla", "Eatla", "HOHO","Meta")
                  .collect(ImmutableList::builder,	ImmutableList.Builder::add)
                  .map(ImmutableList.Builder::build)
                  .subscribe(System.out::println);
        Observable.range(1,15)
                  .collect(ImmutableList::builder,	ImmutableList.Builder::add)
                  .map(ImmutableList.Builder::build)
                  .subscribe(System.out::println);
    }

    @Test
    void compose_test2() {
        Observable.just("Apple", "Orange", "Appla", "Eatla", "HOHO","Meta")
                  .compose(toImmutableList())
                  .subscribe(System.out::println);
        Observable.range(1,15)
                  .compose(toImmutableList())
                  .subscribe(System.out::println);
    }
    public static <T> ObservableTransformer<T,ImmutableList<T>> toImmutableList(){
        return upstream -> upstream.collect(ImmutableList::<T>builder, ImmutableList.Builder::add)
                              .map(ImmutableList.Builder::build)
                              .toObservable(); //must turn Single into Observable

    }

    @Test
    void lift_test() {
        Observable.range(1,5)
                  .lift(doOnEmpty(() -> System.out.println("Operation 1 Empty!")))
                  .subscribe(v -> System.out.println("Operation 1: " + v));
        Observable.<Integer>empty()
                .lift(doOnEmpty(() -> System.out.println("Operation 2 Empty!")))
                .subscribe(v -> System.out.println("Operation 2: " + v));
    }

    public static <T> ObservableOperator<T,T> doOnEmpty(Action action){
        return observer -> new DisposableObserver<T>() {
            boolean isEmpty = true;
            @Override
            public void	onNext(T value) {
                isEmpty = false;
                observer.onNext(value);
            }
            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }
            @Override
            public void onComplete() {
                if (isEmpty) {
                    try {
                        action.run();
                    } catch (Exception e) {
                        onError(e);
                        return;
                    }
                }
                observer.onComplete();
            }
        };
    }


}
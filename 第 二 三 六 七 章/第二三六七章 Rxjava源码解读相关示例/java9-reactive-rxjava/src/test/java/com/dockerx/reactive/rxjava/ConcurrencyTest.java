package com.dockerx.reactive.rxjava;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/4/7 6:29.
 */
public class ConcurrencyTest {
    private static void log(Object msg) {
        System.out.println(
                Thread.currentThread().getName() +
                        ": " + msg);
    }

    public static void sleep(int millis)	{
        try	{
            Thread.sleep(millis);
        }	catch	(InterruptedException	e)	{
            e.printStackTrace();
        }
    }
    private static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public	static	<T>	T delayCalculation(T	value)	{
        sleep(ThreadLocalRandom.current().nextInt(1000));
        System.out.println(
                Thread.currentThread().getName() +
                        ": " + value);
        return	value;
    }
    @Test
    void demo1() {
        Observable.interval(1,	TimeUnit.SECONDS)
                  .map(i	->	i	+	"	次")
                  .subscribe(System.out::println);
        sleep(5,TimeUnit.SECONDS);
    }
    @Test
    void demo2() {
        Observable.interval(1,	TimeUnit.SECONDS)
                  .map(ConcurrencyTest::delayCalculation)
                  .subscribe(System.out::println);

        Observable.interval(1,	TimeUnit.SECONDS)
                  .map(ConcurrencyTest::delayCalculation)
                  .subscribe(ConcurrencyTest::log);
        sleep(4,TimeUnit.SECONDS);
    }
    @Test
    void demo3() {
        Observable.intervalRange(1,2,1,	1,TimeUnit.SECONDS)
                  .map(ConcurrencyTest::delayCalculation)
                  .blockingSubscribe(System.out::println);

        Observable.intervalRange(1,2,1,	1,TimeUnit.SECONDS)
                  .map(i	->	{
                      log(i);
                      return i;
                  })
                  .blockingSubscribe(ConcurrencyTest::log);

    }

    @Test
    void blockingSubscribe_test() {
        Observable.just("Apple", "Orange", "Appla", "Eatla", "HOHO",
                "Meta")
                  .subscribeOn(Schedulers.computation())
                  .map(ConcurrencyTest::delayCalculation)
                  .map(String::toUpperCase)
                  .blockingSubscribe(ConcurrencyTest::log);
    }
    @Test
    void blockingSubscribe_test3() {
        Observable.just("Apple", "Orange", "Appla", "Eatla", "HOHO",
                "Meta")
                  .subscribeOn(Schedulers.computation())
                  .map(ConcurrencyTest::delayCalculation)
                  .subscribeOn(Schedulers.io())
                  .map(String::length)
                  .subscribe(ConcurrencyTest::log);
        sleep(10000);
    }
    @Test
    void blockingSubscribe_test2() {
        Observable.just("Apple", "Orange", "Appla", "Eatla", "HOHO",
                "Meta")
                  .subscribeOn(Schedulers.computation())
                  .map(ConcurrencyTest::delayCalculation)
                  .subscribeOn(Schedulers.computation())
                  .subscribeOn(Schedulers.computation())
                  .map(String::length)
                  .subscribe(ConcurrencyTest::log);
        sleep(10000);
    }

    @Test
    void interval_test() {
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.newThread())
                  .subscribe(i	->	System.out.println(	 i +
                          "	来自线程:	" +	Thread.currentThread().getName()));
        sleep(5000);
    }
    @Test
    void observeOn_test() {
        Observable.just("Apple", "Orange", "Appla", "Eatla", "HOHO",
                "Meta")
                  .subscribeOn(Schedulers.computation())
                  .map(ConcurrencyTest::delayCalculation)
                  .observeOn(Schedulers.computation())
                  .map(String::length)
                  .subscribe(ConcurrencyTest::log);
        sleep(10000);
    }
    @Test
    void observeOn_test2() {
        Observable.just("Apple", "Orange")
                  .subscribeOn(Schedulers.computation())
                  .map(ConcurrencyTest::delayCalculation)
                  .observeOn(Schedulers.newThread())
                  .map(ConcurrencyTest::delayCalculation)
                  .map(String::length)
                  .observeOn(Schedulers.io())
                  .map(Object::toString)
                  .subscribe(ConcurrencyTest::log);
        sleep(10000);
    }

    @Test
    void unsubscribeOn_test() {
        Disposable disposable =
                Observable.interval(1, TimeUnit.SECONDS)
                          .map(i -> "receive " +i)
                          .doOnDispose(()->	System.out.println("Disposing	on	thread	"
                                  +	Thread.currentThread().getName()))
                          .unsubscribeOn(Schedulers.io())
                          .doOnDispose(()->	System.out.println("Disposing1	on	thread	"
                                  +	Thread.currentThread().getName()))
                          .subscribe(ConcurrencyTest::log);
        sleep(4,TimeUnit.SECONDS);
        disposable.dispose();
        sleep(4,TimeUnit.SECONDS);
    }
    @Test
    void unsubscribeOn_test1() {
        Disposable disposable =
                Observable.interval(1, TimeUnit.SECONDS)
                          .map(i -> "receive " +i)
                          .doOnDispose(()->	System.out.println("Disposing	on	thread	"
                              +	Thread.currentThread().getName()))
                          .subscribe(ConcurrencyTest::log);
        sleep(4,TimeUnit.SECONDS);
        disposable.dispose();
        sleep(4,TimeUnit.SECONDS);
    }



    @Test
    void custom_Scheduler_test() {
        Scheduler customScheduler = custom_Scheduler();
        Observable.just("Apple", "Orange", "Appla")
                  .subscribeOn(customScheduler)
                  .map(ConcurrencyTest::delayCalculation)
                  .observeOn(customScheduler)
                  .map(String::length)
                  .subscribe(ConcurrencyTest::log);
        sleep(10000);
    }
    @Test
    void custom_Scheduler_test2() {
        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(10));
        Observable.just("Apple", "Orange", "Appla")
                  .subscribeOn(scheduler)
                  .map(ConcurrencyTest::delayCalculation)
                  .observeOn(scheduler)
                  .map(String::length)
                  .subscribe(ConcurrencyTest::log);
        sleep(10000);
    }

    public static Scheduler custom_Scheduler() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("自定义线程池-%d")
                .build();
        Executor executor = new ThreadPoolExecutor(
                10,  //corePoolSize
                10,  //maximumPoolSize
                0L, TimeUnit.MILLISECONDS, //keepAliveTime, unit
                new LinkedBlockingQueue<>(1000),  //workQueue
                threadFactory
        );
        return Schedulers.from(executor);
    }
}

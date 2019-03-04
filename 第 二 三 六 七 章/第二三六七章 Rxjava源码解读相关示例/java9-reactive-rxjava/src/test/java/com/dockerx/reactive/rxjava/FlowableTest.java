package com.dockerx.reactive.rxjava;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/4/13 22:21.
 */
public class FlowableTest {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
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

    public static <T> T delayCalculation(T value) {
        sleep(ThreadLocalRandom.current().nextInt(1000));
        System.out.println(
                Thread.currentThread().getName() +
                        ": " + value);
        return value;
    }

    static final class Customer {
        final int id;

        Customer(int id) {
            this.id = id;
            System.out.println("正在构造	" + id + "Customer");
        }
    }


    @Test
    void Observable_Customer_test() {
        Observable.range(1, 999_999)
                  .map(Customer::new)
                  .subscribe(customer -> {
                      sleep(20);
                      System.out.println("所获取到的Customer的Id为: " + customer.id);
                  });
    }
    private static void log(Object msg) {
        System.out.println(
                Thread.currentThread().getName() +
                        ": " + msg);
    }
    @Test
    void name() {
        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(10));
        Flowable.just("Apple", "Orange", "Appla")
                  .subscribeOn(Schedulers.computation())
                  .map(ConcurrencyTest::delayCalculation)
                  .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
                  .observeOn(scheduler)
                  .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
                  .subscribe(customer -> {
                      sleep(200);
                      System.out.println("所获取到的Customer的为: " + customer);
                  });

        sleep(1000000000);
    }
    @Test
    public void DemoSubscriber_test_flux_generate3() {
        final Random random = new Random();
        Flowable.generate(ArrayList::new, (list, sink) -> {
            int value = random.nextInt(100);
            list.add(value);
            System.out.println("所发射元素产生的线程: "+Thread.currentThread().getName());
            sink.onNext(value);
            sleep(400);
            if (list.size() == 16) {
                sink.onComplete();
            }
            return list;
        })
            .observeOn(Schedulers.io(),true,2)
            .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
            .subscribe(customer -> {
                sleep(200);
                System.out.println("所获取到的Customer的为: " + customer);
            });
        sleep(20000);
    }

    @Test
    void observable_Customer_test2() {
        Observable.range(1, 999)
                  .map(Customer::new)
                  .observeOn(Schedulers.io(),true,2)
                  .subscribe(customer -> {
                      sleep(200);
                      System.out.println("所获取到的Customer的Id为: " + customer.id+ " "+ Thread.currentThread().getName());
                  });
        sleep(1000000000);
    }

    @Test
    void flowable_test() {
        Flowable.range(1, 999_999)
                .map(Customer::new)
                .observeOn(Schedulers.io())
                .subscribe(customer -> {
                    sleep(20);
                    System.out.println("所获取到的Customer的Id为: " + customer.id);
                });
        sleep(1000000000);
    }

    @Test
    void flowable_test1() {
        Flowable.range(1, 100)
                .doOnNext(s -> System.out.println("Source	pushed	" + s))
                .observeOn(Schedulers.io())
                .map(FlowableTest::delayCalculation)
                .subscribe(s -> System.out.println("Subscriber	received	" + s),
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!")
                );
        sleep(20000);
    }

    @Test
    void flowable_test2() {
        Flowable.range(1, 200)
                .doOnNext(s -> System.out.println("Source	pushed	" + s))
                .observeOn(Schedulers.io())
                .map(FlowableTest::delayCalculation)
                .subscribe(new FlowableSubscriber<Integer>() {
                               @Override
                               public void onSubscribe(Subscription s) {
                                   s.request(Long.MAX_VALUE);
                               }

                               @Override
                               public void onNext(Integer integer) {
                                   sleep(50);
                                   System.out.println("Subscriber received	" + integer);
                               }

                               @Override
                               public void onError(Throwable t) {
                                   t.printStackTrace();
                               }

                               @Override
                               public void onComplete() {
                                   System.out.println("Done!");
                               }
                           }
                );
        sleep(20000);
    }

    @Test
    void flowable_test3() {
        Flowable.range(1, 200)
                .doOnNext(s -> System.out.println("Source	pushed	" + s))
                .observeOn(Schedulers.io())
                .map(FlowableTest::delayCalculation)
                .subscribe(new FlowableSubscriber<Integer>() {
                               AtomicInteger count = new AtomicInteger(0);
                               Subscription subscription;

                               @Override
                               public void onSubscribe(Subscription s) {
                                   this.subscription = s;
                                   System.out.println("请求40个元素");
                                   s.request(40);
                               }

                               @Override
                               public void onNext(Integer integer) {
                                   sleep(50);
                                   System.out.println("Subscriber received	" + integer);
                                   if (count.incrementAndGet() % 20 == 0 && count.get() >= 40) {
                                       System.out.println("再次请求	20	个元素!");
                                       subscription.request(20);
                                   }
                               }

                               @Override
                               public void onError(Throwable t) {
                                   t.printStackTrace();
                               }

                               @Override
                               public void onComplete() {
                                   System.out.println("Done!");
                               }
                           }
                );
        sleep(200000);
    }


    @Test
    public void flowable_bug_generate() {
        final Random random = new Random();
        Flowable.generate(ArrayList::new, (list, emitter) -> {
            int value = random.nextInt(100);
            list.add(value);
            // change the state value in the emitter.onNext
            new Thread(() -> {
                System.out.println("Thread 1");
                emitter.onNext(value);
            }).start();
            new Thread(() -> {
                System.out.println("Thread 2");
                emitter.onNext(value);
            }).start();

           // emitter.onNext(value);
            if (list.size() == 5) {
                emitter.onComplete();
            }
            return list;
        }).doOnNext(item -> System.out.println("emitted	on	thread	"
                + Thread.currentThread().getName() + "  " + item))
            .subscribeOn(Schedulers.computation())
            .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
            .subscribe(FlowableTest::log);

        sleep(10000);
    }

    @Test
    void observable_Create() {
        Observable<Integer> source = Observable.create(emitter -> {
            for (int i = 0; i <= 100; i++) {
                if (emitter.isDisposed()) return;
                emitter.onNext(i);
            }
            emitter.onComplete();
        });
        source.observeOn(Schedulers.io())
              .subscribe(System.out::println, Throwable::printStackTrace,
                      () -> System.out.println("Done!"));
        sleep(1000);
    }

    @Test
    void flowable_Create() {
        Flowable<Object> objectFlowable = Flowable.create(emitter -> {
            for (int i = 0; i <= 1000; i++) {
                if (emitter.isCancelled()) {
                    return;
                }
                //new Customer(i);
                emitter.onNext(i);
            }
        }, BackpressureStrategy.LATEST);
        objectFlowable.doOnNext(s -> System.out.println("Source	pushed	" + s))
                      .observeOn(Schedulers.computation())
                      .subscribe(customer -> {
                          sleep(20);
                          System.out.println("所获取到的Customer的Id为: " + customer);
                      });
        sleep(10000);
    }

    @Test
    void observableToFlowable_test() {
        Observable.range(1, 1000)
                  .toFlowable(BackpressureStrategy.BUFFER)
                  .observeOn(Schedulers.computation())
                  .subscribe(System.out::println);
        sleep(1111);
    }

    @Test
    void observableMixFlowable_test() {
        Flowable<Integer> integerFlowable
                = Flowable.range(1, 10)
                          .subscribeOn(Schedulers.computation());
        Observable.just("Apple", "Orange", "Appla", "Eatla")
                  .flatMap(item -> integerFlowable.map(i -> item.toUpperCase() + "-" + i).toObservable())
                  .subscribe(System.out::println);

        sleep(1111);
    }

    @Test
    void interval_test() {
        Flowable.interval(1, TimeUnit.MILLISECONDS)
                .doOnNext(s -> System.out.println("Source	pushed	" + s))
                .observeOn(Schedulers.io())
                .map(FlowableTest::delayCalculation)
                .subscribe(System.out::println, Throwable::printStackTrace);
        sleep(Long.MAX_VALUE);
    }

    @Test
    void interval_buffer_test() {
        Flowable.interval(1, TimeUnit.SECONDS)
                .onBackpressureBuffer()
                .observeOn(Schedulers.computation())
                .subscribe(item -> {
                    sleep(20);
                    System.out.println("所获取到的值为: " + item);
                });
        sleep(5000);
    }

    @Test
    void interval_buffer_custom_test() {
        Flowable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer(10,
                        () -> System.out.println("overflow!"),
                        BackpressureOverflowStrategy.DROP_LATEST)
                .observeOn(Schedulers.computation())
                .subscribe(item -> {
                    sleep(20);
                    System.out.println("所获取到的值为: " + item);
                });
        sleep(5000);
    }

    @Test
    void onBackPressureLatest_test() {
        Flowable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .observeOn(Schedulers.computation())
                .subscribe(item -> {
                    sleep(20);
                    System.out.println("所获取到的值为: " + item);
                });
        sleep(5000);
    }

    @Test
    void onBackPressureDrop_test() {
        Flowable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureDrop(i -> System.out.println("舍弃元素 : " + i))
                .observeOn(Schedulers.computation())
                .subscribe(item -> {
                    sleep(20);
                    System.out.println("所获取到的值为: " + item);
                });
        sleep(5000);
    }

    static Flowable<Integer> randomGenerator(int min, int max) {
        return Flowable.generate(emitter ->
                emitter.onNext(ThreadLocalRandom.current().nextInt(min, max))
        );
    }

    int a = 0, b = 0;

    @Test
    void generate_test() {

        randomGenerator(1, 300)
                .subscribeOn(Schedulers.computation())
                .doOnNext(i -> System.out.println("所发送的元素:	" + i + "所发送到第: " + (++b) + "个元素"))
                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    sleep(50);
                    System.out.println("所接收到的元素:" + i + "所接收到第: " + (++a) + "个元素");
                }, System.out::println);
        sleep(10000);
    }

    static Flowable<Integer> rangeDescend(int upperBound, int lowerBound) {
        return Flowable.generate(() -> new AtomicInteger(upperBound + 1),
                (state, emitter) -> {
                    int current = state.decrementAndGet();
                    emitter.onNext(current);
                    if (current == lowerBound)
                        emitter.onComplete();
                }
        );
    }

    @Test
    void generate_test2() {
        rangeDescend(500, -500)
                .doOnNext(i -> System.out.println("所发送的元素:	" + i + "所发送到第: " + (++b) + "个元素"))
                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    sleep(5);
                    System.out.println("所接收到的元素:" + i + "所接收到第: " + (++a) + "个元素");
                }, System.out::println);
        sleep(10000);
    }
}

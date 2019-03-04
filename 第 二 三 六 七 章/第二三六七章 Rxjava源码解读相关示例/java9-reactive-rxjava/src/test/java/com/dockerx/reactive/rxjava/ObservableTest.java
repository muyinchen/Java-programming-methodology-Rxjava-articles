package com.dockerx.reactive.rxjava;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.operators.observable.ObservablePublish;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/1/5 2:45.
 */
public class ObservableTest {

    private static void log(Object msg) {
        System.out.println(
                Thread.currentThread().getName() +
                        ": " + msg);
    }

    public static <T> Observable<T> just(T item) {
        ObjectHelper.requireNonNull(item, "The item is null");
        return Observable.create(subscriber -> {
            subscriber.onNext(item);
            subscriber.onComplete();
        });
    }

    @Test
    void cold_Observable() {
        Observable<Object> observable = Observable.create(observer -> {
            observer.onNext("处理的数字是: " + Math.random() * 100);
            observer.onComplete();
        });
        observable.subscribe(consumer -> {
            System.out.println("我处理的元素是: " + consumer);
        });
        observable.subscribe(consumer -> {
            System.out.println("我处理的元素是: " + consumer);
        });

    }

    @Test
    void hot_publish_Observable() {
        ConnectableObservable<Object> observable = Observable.create(observer -> {
            System.out.println("Establishing connection");
            observer.onNext("处理的数字是: " + Math.random() * 100);
            observer.onNext("处理的数字是: " + Math.random() * 100);
            observer.onComplete();
        }).publish();
        observable.subscribe(consumer -> {
            System.out.println("一郎神: " + consumer);
        });
        observable.subscribe(consumer -> {
            System.out.println("二郎神: " + consumer);
        });
        observable.connect();
    }

    @Test
    void infinite_publish_test() {
        ConnectableObservable<Object> observable = Observable.create(observer -> {
            BigInteger i = BigInteger.ZERO;
            while (true) {
                observer.onNext(i);
                i = i.add(BigInteger.ONE);
               /* if (i.compareTo(BigInteger.valueOf(1000))==0) {
                    break;
                }*/
            }
        }).publish();
        observable.subscribe(x -> log(x));
        observable.connect();
    }

    @Test
    void hot_Observable() {
        Observable<Object> observable = Observable.create(observer -> {
            observer.onNext("处理的数字是: " + Math.random() * 100);
            observer.onNext("处理的数字是: " + Math.random() * 100);
            observer.onComplete();
        }).cache();
        observable.subscribe(consumer -> {
            System.out.println("一郎神: " + consumer);
        });
        observable.subscribe(consumer -> {
            System.out.println("二郎神: " + consumer);
        });
    }


    @Test
    void defer_test() {
        String[] monthArray = {"Jan", "Feb", "Mar", "Apl", "May", "Jun",
                "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        Observable.defer(() -> Observable.fromArray(monthArray))
                  .subscribe(System.out::println,
                          Throwable::printStackTrace,
                          () -> System.out.println("Emission completed"));
    }

    @Test
    void empty_test() {
        Observable<Object> empty = Observable.empty();
        empty.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("I am Done!! Completed normally"));
    }

    @Test
    void error_test() {
        Observable<String> observable = Observable.error(new
                Exception("We got an Exception"));
        observable.subscribe(System.out::println,
                System.out::println,
                () -> System.out.println("I am Done!! Completed normally"));
    }

    @Test
    void range_test() {
        log("Range_test Before");
        Observable
                .range(5, 3)
                .subscribe(ObservableTest::log);
        log("Range_test After");
    }

    @Test
    void just_test() {
        log("Just_test Before");
        Observable
                .just("Jan", "Feb", "Mar", "Apl", "May", "Jun")
                .subscribe(ObservableTest::log);
        log("Just_test After");
    }

    @Test
    void infinite_test() {
        Observable<Object> observable = Observable.create(observer -> {
            BigInteger i = BigInteger.ZERO;
            while (true) {
                observer.onNext(i);
                i = i.add(BigInteger.ONE);
               /* if (i.compareTo(BigInteger.valueOf(1000))==0) {
                    break;
                }*/
            }
        });
        observable.subscribe(x -> log(x));
    }

    @Test
    void infinite_thread_test() {
        Observable<Object> observable = Observable.create(observer -> {
            Runnable runnable = () -> {
                BigInteger i = BigInteger.ZERO;
                while (true) {
                    observer.onNext(i);
                    i = i.add(BigInteger.ONE);
                }
            };
            new Thread(runnable).start();
        });
        observable.subscribe(x -> log(x));
        observable.subscribe(x -> log(x));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void hot_s_Observable() {
        Observable<Object> observable = Observable.create(observer -> {

            Runnable runnable = () -> {
                BigInteger i = BigInteger.ZERO;
                while (true) {
                    observer.onNext(i);
                    i = i.add(BigInteger.ONE);
                    if (i.compareTo(BigInteger.valueOf(50)) == 0) {
                        observer.onComplete();
                        break;
                    }
                    System.out.println(Thread.currentThread().getName() + " 下一个消费的数字" + i.toString());
                }
            };
            new Thread(runnable).start();
        }).cache();

        observable.subscribe(consumer -> log("一郎神: " + consumer));
       // sleep(100,TimeUnit.MILLISECONDS);
        observable.subscribe(consumer -> log("二郎神: " + consumer));
        try {
            TimeUnit.MILLISECONDS.sleep(50);
            System.out.println("程序进行50ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.println("程序结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void infinite_unsubscribed_cache_thread_test() {
        Observable<Object> observable = Observable.create(observer -> {
            Runnable runnable = () -> {
                BigInteger i = BigInteger.ZERO;
                while (!observer.isDisposed()) {
                    observer.onNext(i);
                    i = i.add(BigInteger.ONE);
                    System.out.println(Thread.currentThread().getName() + " 下一个消费的数字" + i.toString());
                }
            };
            new Thread(runnable).start();
        }).cache();
        final Disposable subscribe1 = observable.subscribe(x -> log("一郎神: " + x));
        final Disposable subscribe2 = observable.subscribe(x -> log("二郎神: " + x));

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        subscribe1.dispose();
        subscribe2.dispose();
        System.out.println("我取消订阅了");
        try {
            TimeUnit.MILLISECONDS.sleep(0);
            System.out.println("程序结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    void infinite_unsubscribed_thread_test() {
        Observable<Object> observable = Observable.create(observer -> {
            Runnable runnable = () -> {
                BigInteger i = BigInteger.ZERO;
                while (!observer.isDisposed()) {
                    observer.onNext(i);
                    i = i.add(BigInteger.ONE);
                    System.out.println(Thread.currentThread().getName() + " 下一个消费的数字" + i.toString());
                }
            };
            new Thread(runnable).start();
        });
        final Disposable subscribe1 = observable.subscribe(x -> log(x));
        final Disposable subscribe2 = observable.subscribe(x -> log(x));

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        subscribe1.dispose();
        subscribe2.dispose();
        System.out.println("我取消订阅了");
        try {
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.println("程序结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    void pool_pushCollection() {
        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        try {
            pushCollection(integers, forkJoinPool).subscribe(x -> log(x));
            pushCollection(integers, forkJoinPool).subscribe(x -> log(x));
        } finally {
            try {
                forkJoinPool.shutdown();
                int shutdownDelaySec = 1;
                System.out.println("………………等待 " + shutdownDelaySec + " 秒后结束服务……………… ");
                forkJoinPool.awaitTermination(shutdownDelaySec, TimeUnit.SECONDS);
            } catch (Exception ex) {
                System.out.println("捕获到 forkJoinPool.awaitTermination()方法的异常: " + ex.getClass().getName());
            } finally {
                System.out.println("调用 forkJoinPool.shutdownNow()结束服务...");
                List<Runnable> l = forkJoinPool.shutdownNow();
                System.out.println("还剩 " + l.size() + " 个任务等待被执行，服务已关闭 ");
            }
        }
    }

    @Test
    void pool_pushCollectionDANGER() {
        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        pushCollectionDANGER(integers, forkJoinPool).subscribe(x -> log(x + "我是订阅者1"));
        pushCollectionDANGER(integers, forkJoinPool).subscribe(x -> log(x + "我是订阅者2"));
        sleep(2, TimeUnit.SECONDS);
    }

    @Test
    void pool_publish_pushCollection() {
        List<Integer> integers = new ArrayList<>();
        for (int i=1;i<3;i++){
            integers.add(i);
        }
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        try {
            ObservablePublish<Integer> integerObservable = (ObservablePublish<Integer>) pushCollectionDANGER(integers, forkJoinPool).publish();
            integerObservable.subscribe(x -> {
                log("一郎神: " + x);
                sleep(2,TimeUnit.SECONDS);
            },Throwable::printStackTrace, () -> System.out.println("Emission completed"));
            integerObservable.subscribe(x -> {
                log("二郎神: " + x);
                sleep(2,TimeUnit.SECONDS);
            },Throwable::printStackTrace, () -> System.out.println("Emission completed"));
            integerObservable.connect();
            sleep(2,TimeUnit.SECONDS);
            //此处在我们解读API的时候有提到过，接收一个Disposable对象，并实现其消费动作
            integerObservable.connect(ps -> ps.dispose());
        } finally {
            try {
                forkJoinPool.shutdown();
                int shutdownDelaySec = 15;
                System.out.println("………………等待 " + shutdownDelaySec + " 秒后结束服务……………… ");
                forkJoinPool.awaitTermination(shutdownDelaySec, TimeUnit.SECONDS);
            } catch (Exception ex) {
                System.out.println("捕获到 forkJoinPool.awaitTermination()方法的异常: " + ex.getClass().getName());
            } finally {
                System.out.println("调用 forkJoinPool.shutdownNow()结束服务...");
                List<Runnable> l = forkJoinPool.shutdownNow();
                System.out.println("还剩 " + l.size() + " 个任务等待被执行，服务已关闭 ");
            }
        }
    }

    @Test
    void infinite_refCount_publish_test() {
        List<Integer> integers = new ArrayList<>();
        for (int i=1;i<10;i++){
            integers.add(i);
        }
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        try {
            Observable<Integer> integerObservable =  pushCollectionDANGER(integers, forkJoinPool).publish().refCount();
            integerObservable.subscribe(x -> {
                log("一郎神: " + x);
                sleep(2,TimeUnit.SECONDS);
            },Throwable::printStackTrace, () -> System.out.println("Emission completed"));
            sleep(1,TimeUnit.SECONDS);
            integerObservable.subscribe(x -> {
                log("二郎神: " + x);
                sleep(1,TimeUnit.SECONDS);
            },Throwable::printStackTrace, () -> System.out.println("Emission completed"));
            sleep(20,TimeUnit.SECONDS);
        } finally {
            try {
                forkJoinPool.shutdown();
                int shutdownDelaySec = 2;
                System.out.println("………………等待 " + shutdownDelaySec + " 秒后结束服务……………… ");
                forkJoinPool.awaitTermination(shutdownDelaySec, TimeUnit.SECONDS);
            } catch (Exception ex) {
                System.out.println("捕获到 forkJoinPool.awaitTermination()方法的异常: " + ex.getClass().getName());
            } finally {
                System.out.println("调用 forkJoinPool.shutdownNow()结束服务...");
                List<Runnable> l = forkJoinPool.shutdownNow();
                System.out.println("还剩 " + l.size() + " 个任务等待被执行，服务已关闭 ");
            }
        }
    }

    @Test
    void replay_PublishSubject_test() {
        PublishSubject<Object> publishSubject = PublishSubject.create();
        ConnectableObservable<Object> replay = publishSubject.replay();
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        List<Integer> integers = new ArrayList<>();
        for (int i=1;i<10;i++){
            integers.add(i);
        }
        Disposable subscribe1 = replay.subscribe(x -> {
            log("一郎神: " + x);
        }, Throwable::printStackTrace, () -> System.out.println("Emission completed"));

        Disposable subscribe2 = replay.subscribe(x -> {
            log("二郎神: " + x);
        }, Throwable::printStackTrace, () -> System.out.println("Emission completed"));
        Disposable subscribe3 = replay.subscribe(x -> {
            log("三郎神: " + x);
        }, Throwable::printStackTrace, () -> System.out.println("Emission completed"));
        AtomicInteger atomicInteger = new AtomicInteger(integers.size());
        try {
            forkJoinPool.submit(() -> {
                integers.forEach(id -> {
                    sleep(1,TimeUnit.SECONDS);
                    publishSubject.onNext(id);
                    if (atomicInteger.decrementAndGet() == 0) {
                        publishSubject.onComplete();
                    }
                });
            });
           /* integers.forEach(id -> forkJoinPool.submit(() -> {
                sleep(3,TimeUnit.SECONDS);
                publishSubject.onNext(id);
                if (atomicInteger.decrementAndGet() == 0) {
                    publishSubject.onComplete();
                }
            }));*/
            replay.connect();
            sleep(2,TimeUnit.SECONDS);
            subscribe1.dispose();
            sleep(1,TimeUnit.SECONDS);
            //replay.connect(consumer -> consumer.dispose());
            publishSubject.onComplete();
        } finally  {
            try {
                forkJoinPool.shutdown();
                int shutdownDelaySec = 2;
                System.out.println("………………等待 " + shutdownDelaySec + " 秒后结束服务……………… ");
                forkJoinPool.awaitTermination(shutdownDelaySec, TimeUnit.SECONDS);
            } catch (Exception ex) {
                System.out.println("捕获到 forkJoinPool.awaitTermination()方法的异常: " + ex.getClass().getName());
            } finally {
                System.out.println("调用 forkJoinPool.shutdownNow()结束服务...");
                List<Runnable> l = forkJoinPool.shutdownNow();
                System.out.println("还剩 " + l.size() + " 个任务等待被执行，服务已关闭 ");
            }
        }
    }

    public static Observable<Integer> pushCollection(List<Integer> ids, ForkJoinPool commonPool) {
        return Observable.create(observer -> {
            System.out.println("Establishing connection");
            AtomicInteger atomicInteger = new AtomicInteger(ids.size());
            commonPool.submit(() -> {
                ids.forEach(id -> {
                    observer.onNext(id);
                    if (atomicInteger.decrementAndGet() == 0) {
                        observer.onComplete();
                    }
                });
            });
        });
    }

    public static Observable<Integer> pushCollectionDANGER(List<Integer> ids, ForkJoinPool commonPool) {
        return Observable.create(observer -> {
            System.out.println("Establishing connection");
            AtomicInteger atomicInteger = new AtomicInteger(ids.size());
            ids.forEach(id -> commonPool.submit(() -> {
                observer.onNext(id);
                if (atomicInteger.decrementAndGet() == 0) {
                    commonPool.shutdownNow();
                    observer.onComplete();
                }
            }));
        });
    }

    static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void error_test(int n) {
        if (n == 5) throw new RuntimeException("我就是喜欢来搞个惊喜");
        System.out.println("我消费的元素是--> " + n);
    }


    static void observable_error_test(int n) {
        Observable.create(observer -> {
            try {
                observer.onNext(n);
                observer.onComplete();
            } catch (Exception e) {
                observer.onError(e);
            }
        })
                  .subscribe(x -> error_test((int) x),
                          Throwable::printStackTrace,
                          () -> System.out.println("Emission completed"));
    }

    @Test
    void observable_error_test_acc() {
        observable_error_test(1);
        observable_error_test(5);
    }

    static Integer error_test_p(int n) {
        if (n == 5) throw new RuntimeException("我就是喜欢来搞个惊喜");
        System.out.println("我消费的元素是--> " + n);
        return n + 10;
    }

    static Observable<Integer> error_test_pro(int n) {
        return Observable.fromCallable(() -> error_test_p(n));
    }

    @Test
    void fromCallable_test() {
        error_test_pro(1).subscribe(x -> log(x),
                Throwable::printStackTrace,
                () -> System.out.println("Emission completed"));
        System.out.println("*********************我是一个大大的分割线*********************");
        error_test_pro(5).subscribe(x -> log(x),
                Throwable::printStackTrace,
                () -> System.out.println("Emission completed"));
    }

    @Test
    void timer_test() {
        Observable.timer(2, TimeUnit.SECONDS).subscribe(x -> log(10));
        Observable.timer(2, TimeUnit.SECONDS).subscribe(x -> log(10));
        Observable.timer(2, TimeUnit.SECONDS).subscribe(x -> log(10));
        sleep(10, TimeUnit.SECONDS);
    }

    @Test
    void interval_test() {
        Observable.interval(2, TimeUnit.SECONDS).subscribe(x -> log(10));
        sleep(10, TimeUnit.SECONDS);
    }

    @Test
    void asyncSubject_test() {

        AsyncSubject<Object> asyncSubject = AsyncSubject.create();
        asyncSubject.subscribe(x -> log("一郎神: " + x),
                Throwable::printStackTrace,
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        asyncSubject.onNext(1L);
        asyncSubject.onNext(2L);
        asyncSubject.onNext(10L);
        //asyncSubject.onError(new RuntimeException("我来耍下宝"));
        asyncSubject.onComplete();
    }

    @Test
    void behaviorSubject_test() {
        BehaviorSubject<Object> behaviorSubject = BehaviorSubject.create();

        behaviorSubject.onNext(1L);
        behaviorSubject.onNext(2L);
        behaviorSubject.subscribe(x -> log("一郎神: " + x),
                Throwable::printStackTrace,
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        behaviorSubject.onNext(10L);
        behaviorSubject.onComplete();
    }
    @Test
    void behaviorSubject_error_test() {
        BehaviorSubject<Object> behaviorSubject = BehaviorSubject.create();

        behaviorSubject.onNext(1L);
        behaviorSubject.onNext(2L);
        behaviorSubject.onError(new RuntimeException("我来耍下宝"));
        behaviorSubject.subscribe(x -> log("一郎神: " + x),
                Throwable::printStackTrace,
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        behaviorSubject.onNext(10L);
        behaviorSubject.onComplete();
    }

    @Test
    void replaySubject_test() {
        ReplaySubject<Object> replaySubject = ReplaySubject.create();
        replaySubject.onNext(1l);
        replaySubject.onNext(2l);
        replaySubject.subscribe(x -> log("一郎神: " + x),
                Throwable::printStackTrace,
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        replaySubject.onNext(10l);
        replaySubject.onComplete();

    }
    @Test
    void replaySubject_conf_test() {
        //ReplaySubject<Object> replaySubject = ReplaySubject.createWithSize(5);//只缓存订阅前最后5条数据
        //ReplaySubject<Object> replaySubject = ReplaySubject.createWithTime(5,TimeUnit.SECONDS, Schedulers.computation()); //只缓存被订阅前5秒内的数据
        ReplaySubject<Object> replaySubject =ReplaySubject.createWithTimeAndSize(5,
                TimeUnit.SECONDS, Schedulers.computation(),3); // 请结合以上两者注释
        for (Long i=1l;i<10l;i++){
            replaySubject.onNext(i);
            sleep(1,TimeUnit.SECONDS);
        }

        replaySubject.subscribe(x -> log("一郎神: " + x),
                Throwable::printStackTrace,
                () -> System.out.println("Emission completed"),
                disposable -> System.out.println("onSubscribe")
        );
        replaySubject.onNext(10l);
        replaySubject.onComplete();

    }
}

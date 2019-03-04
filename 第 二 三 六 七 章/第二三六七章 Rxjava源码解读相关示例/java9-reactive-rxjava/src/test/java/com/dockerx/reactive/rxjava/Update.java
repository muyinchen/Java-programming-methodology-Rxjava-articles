package com.dockerx.reactive.rxjava;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import java.sql.*;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/4/21 23:31.
 */
public class Update {
    public static Single<Integer> create(Callable<Connection> connectionFactory, List<Object> parameters, String sql) {
        Callable<PreparedStatement> resourceFactory = () -> {
            Connection con = connectionFactory.call();
            // TODO set parameters
            return con.prepareStatement(sql);
        };
        Function<PreparedStatement, Single<Integer>> singleFactory = ps -> Single.just(ps.executeUpdate());
        Consumer<PreparedStatement> disposer = Update::closeAll;
        return Single.using(resourceFactory, singleFactory, disposer);
    }

    public static <T> Flowable<T> create(Callable<Connection> connectionFactory, List<Object> parameters, String sql,
                                         Function<? super ResultSet, T> mapper) {
        Callable<PreparedStatement> resourceFactory = () -> {
            Connection con = connectionFactory.call();
            // TODO set parameters
            return con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        };
        Function<PreparedStatement, Flowable<T>> singleFactory = ps -> create(ps, mapper);
        Consumer<PreparedStatement> disposer = Update::closeAll;
        return Flowable.using(resourceFactory, singleFactory, disposer);
    }

    private static <T> Flowable<T> create(PreparedStatement ps, Function<? super ResultSet, T> mapper) {
        Callable<ResultSet> initialState = () -> {
            ps.execute();
            return ps.getGeneratedKeys();
        };
        BiConsumer<ResultSet, Emitter<T>> generator = (rs, emitter) -> {
            if (rs.next()) {
                emitter.onNext(mapper.apply(rs));
            } else {
                emitter.onComplete();
            }
        };
        Consumer<ResultSet> disposer = Update::closeAll;
        return Flowable.generate(initialState, generator, disposer);
    }


    public static void closeAll(ResultSet rs) {
        Statement stmt = null;
        try {
            stmt = rs.getStatement();
        } catch (SQLException e) {
            // ignore
        }
        try {
            rs.close();
        } catch (SQLException e) {
            // ignore
        }
        if (stmt != null) {
            closeAll(stmt);
        }

    }

    public static void closeAll(Statement stmt) {
        try {
            stmt.close();
        } catch (SQLException e) {
            // ignore
        }
        Connection con = null;
        try {
            con = stmt.getConnection();
        } catch (SQLException e1) {
            // ignore
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

}

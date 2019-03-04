package com.dockerx.reactive.rxjava;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import java.sql.*;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Author  知秋
 * @email fei6751803@163.com
 * @time Created by Auser on 2018/4/21 23:13.
 */
public class FlowableSelect {

    public static <T> Flowable<T> create(Callable<Connection> connectionFactory, List<Object> parameters, String sql,
                                         Function<? super ResultSet, T> mapper) {
        Callable<ResultSet> initialState = () -> {
            Connection con = connectionFactory.call();
            PreparedStatement ps = con.prepareStatement(sql);
            // TODO set parameters
            ResultSet rs = ps.executeQuery();
            return rs;
        };
        BiConsumer<ResultSet, Emitter<T>> generator = (rs, emitter) -> {
            if (rs.next()) {
                emitter.onNext(mapper.apply(rs));
            } else {
                emitter.onComplete();
            }
        };
        Consumer<ResultSet> disposeState = FlowableSelect::closeSilently;
        return Flowable.generate(initialState, generator, disposeState);
    }

    public static void closeSilently(ResultSet rs) {
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
}

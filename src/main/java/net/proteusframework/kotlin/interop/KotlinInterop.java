/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package net.proteusframework.kotlin.interop;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Functions for handling interop with kotlin lambdas and functions.
 *
 * Because kotlin functions that expect a kotlin lambda expect kotlin function types {@link Function1}, {@link Function2}..., we
 * can't just pass a java lambda into them easily.  This call provides helper functions for converting common java functional
 * interfaces into kotlin functions.
 *
 * @author Alan Holt (aholt@proteus.co)
 * @since 4/25/18
 */
public class KotlinInterop
{
    private KotlinInterop()
    {
        // Do Nothing
    }

    /**
     * Convert a {@link Function} into a {@link Function1} for kotlin interop.
     *
     * @param <A> the type of the single parameter
     * @param <B> the type of the return value
     * @param func the function to convert
     * @return the kotlin function
     */
    public static <A, B> Function1<A, B> kfunc(Function<A, B> func)
    {
        return func::apply;
    }

    /**
     * Convert a {@link BiFunction} into a {@link Function2} for kotlin interop.
     *
     * @param <A> the type of the first parameter
     * @param <B> the type of the second parameter
     * @param <C> the type of the return value
     * @param func the function to convert
     * @return the kotlin function
     */
    public static <A, B, C> Function2<A, B, C> kfunc2(BiFunction<A, B, C> func)
    {
        return func::apply;
    }

    /**
     * Convert a {@link BiConsumer} into a {@link Function2} (returning {@link Unit}) for kotlin interop.
     *
     * @param <A> the type of the first parameter
     * @param <B> the type of the second parameter
     * @param bc the BiConsumer to convert
     * @return the kotlin function
     */
    public static <A, B> Function2<A, B, Unit> biKonsumer(BiConsumer<A, B> bc)
    {
        return (a, b) -> { bc.accept(a, b); return Unit.INSTANCE; };
    }

    /**
     * Convert a {@link Consumer} into a {@link Function1} for kotlin interop.
     *
     * @param <A> the type of the single parameter
     * @param c the consumer to convert
     * @return the kotlin function
     */
    public static <A> Function1<A, Unit> konsumer(Consumer<A> c)
    {
        return a -> { c.accept(a); return Unit.INSTANCE; };
    }

    /**
     * Convert a {@link Supplier} into a {@link Function0} for kotlin interop.
     *
     * @param <A> the type return value
     * @param s the supplier to convert
     * @return the kotlin function
     */
    public static <A> Function0<A> ksupplier(Supplier<A> s)
    {
        return s::get;
    }

    /**
     * Convert a {@link Runnable} into a {@link Function0<Unit>} for kotlin interop.
     *
     * @param r the runnable to convert
     * @return the kotlin runnable
     */
    public static Function0<Unit> krunnable(Runnable r)
    {
        return () -> {
            r.run();
            return Unit.INSTANCE;
        };
    }
}

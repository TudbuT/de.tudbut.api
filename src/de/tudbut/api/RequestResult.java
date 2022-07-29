package de.tudbut.api;

import de.tudbut.tools.Nullable;

public class RequestResult<T> extends Nullable<T> {

    public RequestResult(Type type, T t, Class<? extends T> clazz) {
        super(t);
        result = type;
        this.type = clazz;
    }

    public static <T> RequestResult<T> FAIL(T t) {
        return new RequestResult<>(Type.FAIL, t, (Class<? extends T>) t.getClass());
    }
    public static <T> RequestResult<T> SUCCESS(T t) {
        return new RequestResult<>(Type.SUCCESS, t, (Class<? extends T>) t.getClass());
    }
    public static RequestResult<Void> FAIL() {
        return new RequestResult<>(Type.FAIL, null, Void.class);
    }
    public static RequestResult<Void> SUCCESS() {
        return new RequestResult<>(Type.SUCCESS, null, Void.class);
    }

    public final Type result;
    public final Class<? extends T> type;

    public <R> R unwrapSuccess() {
        if(result == Type.SUCCESS)
            return (R) get(); // ignore the warning. the potential ClassCastException is intentional.
        else
            throw new IllegalStateException("RequestResult<" + type + ">.unwrapSuccess() called on failed request");
    }

    public <R> Nullable<R> success() {
        if(result == Type.SUCCESS)
            return new Nullable<R>((R) this.get()); // Fail if type does not match!
        else
            return new Nullable<R>(null);
    }

    public <R> Nullable<R> fail() {
        if(result == Type.FAIL)
            return new Nullable<R>((R) this.get()); // Fail if type does not match!
        else
            return new Nullable<R>(null);
    }

    public <R> Nullable<R> success(Class<R> expect) {
        if(result == Type.SUCCESS && type == expect)
            return new Nullable<R>((R) this.get());
        else
            return new Nullable<R>(null);
    }

    public <R> Nullable<R> fail(Class<R> expect) {
        if(result == Type.FAIL && type == expect)
            return new Nullable<R>((R) this.get());
        else
            return new Nullable<R>(null);
    }

    public String toString() {
        return result + ":" + type.getName() + ":" + super.toString();
    }

    public static enum Type {
        SUCCESS,
        FAIL,
        ;
    }
}

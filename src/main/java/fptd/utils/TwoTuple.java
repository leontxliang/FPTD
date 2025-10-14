package fptd.utils;

public class TwoTuple<A, B> {

    public final A first;
    public final B second;

    public TwoTuple(final A a, final B b) {
        this.first = a;
        this.second = b;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}
package eu.javaland.fpworkshop.datastructures;

import static io.vavr.API.TODO;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import io.vavr.Function1;
import io.vavr.control.Option;

sealed interface List<E> {

    static <E> List<E> listOf(E... elements){
        var list = List.<E>nil();
        for(var i = elements.length-1; i >= 0; i--){
            list = cons(elements[i], list);
        }
        return list;
    }

    static <E> List<E> cons(E element, List<E> tail){
        return new Cons<>(element, tail);
    }

    static <E> List<E> nil(){
        return Nil.instance();
    }

    static <E> List<E> fill(int n, Supplier<E> supplier) {
        return TODO();
    }

    default List<E> tail(){
        if(this instanceof List.Cons<E> list){
            return list.tail;
        }
        return Nil.instance();
    }

    default E head(){
        if(this instanceof List.Cons<E> list){
            return list.head;
        }
        throw new NoSuchElementException("head of empty list");
    }

    default Option<E> headOption(){
        return TODO();
    }

    default <T> List<T> map(Function1<? super E, ? extends T> mapper){
        return TODO();
    }

    default List<E> drop(int n) {
        return TODO();
    }

    default <U> U foldLeft(U zero, BiFunction<? super U, ? super E, ? extends U> f){
        U result = zero;
        List<E> tail = this;
        while(!(tail instanceof Nil)){
            result = f.apply(result, tail.head());
            tail = tail.tail();
        }
        return result;
    }

    final class Nil<A> implements List<A> {
        private static final Nil<?> instance = new Nil<>();

        private Nil(){}

        @SuppressWarnings("unchecked")
        public static <B> List<B> instance(){
            return (List<B>) instance;
        }

        @Override
        public String toString() {
            return "Nil{}";
        }
    }

    record Cons<A>(A head, List<A> tail) implements List<A>{}

}

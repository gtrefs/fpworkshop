package eu.javaland.fpworkshop.higherorderfunctions;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static io.vavr.API.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ZipShould {

    private Function3<BiFunction<Integer, Integer, Integer>, List<Integer>, List<Integer>, List<Integer>> createMyZip() {
        return (BiFunction<Integer, Integer, Integer> func, List<Integer> list1, List<Integer> list2) -> list1.zipWith(list2, func);
    }

    private Function3<List, Iterable<? extends Integer>, Function2<? super Integer, ? super Integer, ? extends Integer>, List<Integer>> createMyZip2() {
        return List::zipWith;
    }

    @Test
    void callZipFunction() {
        // given
        final Function3<BiFunction<Integer, Integer, Integer>, List<Integer>, List<Integer>, List<Integer>> myZip = createMyZip();
        // when
        List<Integer> result = myZip.apply((a, b) -> a * b, List(1, 2, 3), List(1, 2, 3));
        // then
        assertThat(result, is(equalTo(List(1, 4, 9))));
    }

    @Test
    void curriedZip() {
        // given
        final Function1<BiFunction<Integer, Integer, Integer>, Function1<List<Integer>, Function1<List<Integer>, List<Integer>>>> curriedMyZip = createMyZip().curried();
        // when
        final List<Integer> result = curriedMyZip.apply((a, b) -> a * b).apply(List(1, 2, 3)).apply(List(1, 2, 3));
        // then
        assertThat(result, is(equalTo(List(1, 4, 9))));
    }

    @Test
    void partialFunctionApplication() {
        // given
        final Function1<List<Integer>, List<Integer>> multiplyListElements = createMyZip().curried().apply((a, b) -> a * b).apply(List(1, 2, 3));
        // when
        List<Integer> result = multiplyListElements.apply(List(4, 5, 6));
        // then
        assertThat(result, is(equalTo(List(4, 10, 18))));
    }
}

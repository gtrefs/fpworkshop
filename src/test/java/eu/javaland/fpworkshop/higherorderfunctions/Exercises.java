package eu.javaland.fpworkshop.higherorderfunctions;

import io.vavr.Function1;
import io.vavr.Function2;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.vavr.API.TODO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class Exercises {

    // Exercise 1
    // Implement makeAdder which returns a function which always adds the number given to makeAdder
    // Hint: Go by the types
    @Test
    void makeAdderShouldReturnAdder() {
        Function<Integer, Integer> addOne = makeAdder(1);

        assertThat(addOne.apply(1), equalTo(2));
    }

    Function<Integer, Integer> makeAdder(int a) {
        return TODO();
    }

    // Exercise 2
    // Make a curried function from a BiFunction
    // Hint: Go by the types
    @Test
    void createCurriedFunction() {
        // given
        BiFunction<String, String, String> concat = (a, b) -> a + b;
        // when
        Function<String, Function<String, String>> curriedConcat = curried(concat);
        // then
        assertThat(curriedConcat.apply("foo").apply("bar"), is(equalTo(concat.apply("foo", "bar"))));
    }

    private Function<String, Function<String, String>> curried(BiFunction<String, String, String> concat) {
        return TODO();
    }

    // Exercise 3
    // Implement compose and andThen

    Function<Integer, Integer> add3 = a -> a + 3;
    Function<Integer, Integer> multiplyBy5 = a -> a * 5;

    @Test
    void composeFunctions() {
        // when
        Function<Integer, Integer> composed = compose(add3, multiplyBy5);
        // then
        assertThat(composed.apply(4), is(equalTo(23)));
    }

    private Function<Integer, Integer> compose(Function<Integer, Integer> f, Function<Integer, Integer> g) {
        return TODO();
    }

    @Test
    void andThenFunctions() {
        // when
        Function<Integer, Integer> composed = andThen(add3, multiplyBy5);
        // then
        assertThat(composed.apply(4), is(equalTo(35)));
    }

    private Function<Integer, Integer> andThen(Function<Integer, Integer> f, Function<Integer, Integer> g) {
        return TODO();
    }

    // Exercise 4
    // Partial Function Application
    @Test
    void partialFunctionApplication() {
        // given
        final Function2<Integer, Integer, Integer> adder = (a, b) -> a + b;
        // when
        final Function1<Integer, Integer> addOne = createPartialApplicatedFunction(adder.curried(), 1);
        // then
        assertThat(addOne.apply(5), is(equalTo(6)));

    }

    private Function1<Integer, Integer> createPartialApplicatedFunction(Function1<Integer, Function1<Integer, Integer>> func, Integer operand1) {
        return TODO();
    }
}

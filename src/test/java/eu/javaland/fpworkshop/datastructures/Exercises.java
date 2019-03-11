package eu.javaland.fpworkshop.datastructures;

import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import static eu.javaland.fpworkshop.datastructures.List.listOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Exercises {

    // Exercise 1
    // head() has a side effect, because it throws an exception when it is called on an empty list.
    // Type Option allows us to represent potentially absent values.
    // Implement function headOption() which returns a Some with head or None if head is not present.

    @Test
    void beSomeForHeadOfNonEmptyList(){
        var list = listOf("Hello", "JavaLand");

        var headOption = list.headOption();

        assertThat(headOption.isDefined(), is(true));
        assertThat(headOption.get(), is("Hello"));
    }

    @Test
    void beNoneForHeadOfEmptyList(){
        var list = listOf();

        var headOption = list.headOption();

        assertThat(headOption.isDefined(), is(false));
        assertThat(headOption, is(Option.none()));
    }

    // Exercise 2
    // Implement a function which maps each value to another value.
    // Hint: Go by the types.
    @Test
    void beMappableFromCharactersToIntegers(){
        var list = listOf('a', 'b');

        var mappedList = list.map(character -> (int) character);

        assertThat(mappedList, equalTo(listOf(97, 98)));
    }

    @Test
    void beMappableFromEmptyListToEmptyList(){
        var list = List.<Character>nil();

        List<Integer> mappedList = list.map(character -> (int) character);

        assertThat(mappedList, equalTo(List.<Integer>nil()));
    }
}

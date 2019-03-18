package eu.javaland.fpworkshop.datastructures;

import org.junit.jupiter.api.Test;

import static eu.javaland.fpworkshop.datastructures.List.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ListShould {

    @Test
    void madeOfConsAndOneNil(){
        List<String> sentence = cons("Hello", cons("JavaLand", nil()));

        assertThat(sentence.head(), is(equalTo("Hello")));
        assertThat(sentence.tail().head(), is(equalTo("JavaLand")));
        assertThat(sentence.tail().tail(), is(equalTo(nil())));
    }

    @Test
    void beCreatedWithASmartConstructor(){
        List<String> list = listOf("Hello", "JavaLand");

        List<String> expected = cons("Hello", cons("JavaLand", nil()));

        assertThat(list, is(equalTo(expected)));
    }

}

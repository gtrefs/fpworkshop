package eu.javaland.fpworkshop.higherorderfunctions;

import io.vavr.Function1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompositionShould {

    @Test
    void compose_two_functions() {
        Function1<Integer, Integer> f = a -> a * 2;
        Function1<Integer, Integer> g = b -> b + 2;
        Function1<Integer, Integer> h = c -> f.apply(g.apply(c));
        Function1<Integer, Integer> i = f.compose(g);

        assertEquals(6, h.apply(1).intValue());
        assertEquals(6, i.apply(1).intValue());
        assertEquals(h.apply(1).intValue(), i.apply(1).intValue());
    }
}

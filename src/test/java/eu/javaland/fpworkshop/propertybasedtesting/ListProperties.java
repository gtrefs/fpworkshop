package eu.javaland.fpworkshop.propertybasedtesting;

import io.vavr.collection.List;
import net.jqwik.api.*;


import static org.assertj.core.api.Assertions.assertThat;

public class ListProperties {

    @Property
    public void reverseList(@ForAll("lists") List<String> list){
//        Assume.that(list.size() > 10);
        assertThat(reverse(reverse(list))).isEqualTo(list);
    }

    private static List<String> reverse(List<String> list) {
//        if(list.size() >= 10) return List.empty();
        return list.reverse();
    }


    @Provide
    public Arbitrary<List<String>> lists(){
        return Arbitraries.strings().alpha().list().map(List::ofAll);
    }
}

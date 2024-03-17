package eu.javaland.fpworkshop.propertybasedtesting;

import net.jqwik.api.*;
import org.jsoup.Jsoup;

import java.util.stream.Collectors;


// For fuzzing with Jazzer, see here: https://www.youtube.com/watch?v=Ai3wnnSFC-8
// Jazzer does find inputs which break JSoup jqwik highly likely will not do that because it does not take coverage into
// consideration. The focus is more on design and verification.
public class JSoupFuzzing {

    @Property(tries = 5_000)
    public void shouldNotCrash(@ForAll("htmlInBody") String html){
        Jsoup.parse(html);
    }

    @Provide
    public Arbitrary<String> htmlInBody(){
        return Arbitraries.chars()
                .all()
                .list()
                .withSizeDistribution(RandomDistribution.uniform())
                .map(chars -> {
                    if(chars == null) return null;
                    return """
                           <html>
                           <body>
                           """
                            +
                            chars.stream().map(String::valueOf).collect(Collectors.joining())
                            +
                           """
                           </body>
                           </html>
                           """;
                });
    }
}

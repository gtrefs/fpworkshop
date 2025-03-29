package eu.javaland.fpworkshop.propertybasedtesting;

import eu.javaland.fpworkshop.propertybasedtesting.Exercises.DataAccessLayer.Query;
import io.vavr.concurrent.Future;
import net.jqwik.api.*;
import net.jqwik.api.statistics.Histogram;
import net.jqwik.api.statistics.Statistics;
import net.jqwik.api.statistics.StatisticsReport;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.vavr.API.TODO;
import static org.assertj.core.api.Assertions.assertThat;

public class Exercises {

    // Exercise 1
    // Is the following property a good property for a list reversal algorithm?
    // Why and why not? If not, how could you overcome its flaw?

    @Property
    public void symmetricReversal(@ForAll List<String> list){
        var copy = List.copyOf(list);
        Collections.reverse(list);
        Collections.reverse(list);

        assertThat(copy).isEqualTo(list);
    }

    @Property
    @StatisticsReport(format = Histogram.class)
    public void maxMustBeMin(@ForAll List<String> list){
        Statistics.label("length").collect(list.size());
        Assume.that(!list.isEmpty());

        var copy = List.copyOf(list);
        Collections.reverse(list);

        assertThat(copy.getFirst()).isEqualTo(list.getLast());
    }

    // Exercise 2
    // Modelling / Refactoring

    // Modeling essentially requires you to write an indirect and very simple implementation of your code — often an
    // algorithmically inefficient one — and pit it against the real implementation. The model should be so simple
    // that it is obviously correct. You can then optimize the real system as much as you want: as long as both
    // implementations behave the same way, there’s a good chance that the complex one is as good as the obviously
    // correct one, but faster. [Property Based Testing with PropEr, Erlang and Elixir, page 33]

    // Think of refactoring as a model based property. The old code becomes the model because the behaviour should
    // not change. How should the reallyBadCode method look like?

    @Property
    public void modelBasedRefactoring(@ForAll int numbers){
        var toBeReFactored = new ToBeReFactored();

        assertThat(toBeReFactored.reallyBadCode(numbers)).isEqualTo(toBeReFactored.reallyBadCodeModel(numbers));
    }

    static class ToBeReFactored {

        public String reallyBadCode(int number) {
            return TODO();
        }
        public String reallyBadCodeModel(int number){
            if(number > 10){
                if(number < 20){
                    return "Nice";
                }
            }
            return "Not so so nice";
        }
    }

    // Exercise 3
    // Statistics / Fuzzing

    // In many situations you’d like to know if jqwik will really generate the kind of values you expect and if the
    // frequency and distribution of certain value classes meets your testing needs. [jqwik User Guide, Collecting and
    // Reporting Statistics]

    // To fuzz test a UNIX utility meant to automatically generate random input and command-line parameters for the
    // utility. The project was designed to test the reliability of UNIX command line programs by executing a large
    // number of random inputs in quick succession until they crashed. [Wikipedia, Fuzzing]

    // A crash could be seen as a violation of the invariant that "the application should work as expected". We can
    // take the idea of fuzzing and add the statistics to it. For example, after reworking a data access layer, what
    // is the improvement of avoiding unnecessary database calls (Caching)?

    // Please add a cache and record if there was a cache miss or a cache hit. You can use the Statistics class from
    // jqwik to monitor hits and misses. A good candidate for a cache would be a ConcurrentHashMap. The compute method
    // guarantees atomic access to the keys and lets you run a computation creating the value.

    DataAccessLayer dataAccessLayer = new DataAccessLayer(DataAccessLayer.Database.inMemory());

    @Property
    @StatisticsReport(format = Histogram.class)
    public void collectStatistics(@ForAll("queries") Query query){
       dataAccessLayer.query(query).get();
    }

    @Provide
    public Arbitrary<Query> queries(){
        return Arbitraries.strings().ascii().ofMinLength(3).ofMaxLength(4).map(Query::new);
    }

    static class DataAccessLayer {

        private final Database database;
        private ConcurrentHashMap<Query, Future<String>> cache = new ConcurrentHashMap<>();

        DataAccessLayer(Database database) {
            this.database = database;
        }

        public Future<String> query(Query query){
            return cache.compute(query, (key, value) -> {
                if(value != null) {
                    return value;
                }
                return database.run(key);
            });
        }

        record Query(String query) {
        }

        private interface Database {
            static Database inMemory(){
                return new InMemory();
            }
            String read();
            Future<String> run(Query query);
        }

        private static class InMemory implements Database {
            AtomicInteger accessCounter = new AtomicInteger(0);
            public String read() {
                return "Access: " + accessCounter.getAndIncrement();
            }

            public Future<String> run(Query query){
                return Future.of(this::read);
            }
        }
    }
}

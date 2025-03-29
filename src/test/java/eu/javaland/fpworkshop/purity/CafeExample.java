package eu.javaland.fpworkshop.purity;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class CafeExample {

    @Test
    public void movingTheSideEffectAwayFromTheCore(){
        var creditCard = new CreditCard();
        var cafe = new Cafe();

        Tuple2<Coffee, Charge> cup = cafe.buyCoffee(creditCard);

        assertThat(cup, notNullValue());
    }

    @Test
    public void buyMultipleCups(){
        var creditCard = new CreditCard();
        var cafe = new Cafe();

        var cups = cafe.buyCoffees(creditCard, 4);

        assertThat(cups._2.price, is(equalTo(new BigDecimal(4))));
        assertThat(cups._2.creditCard, is(equalTo(creditCard)));
    }

    @Test
    public void americanExpressSometimesTimesOut(){
        var americanExpress = PaymentProvider.americanExpress();
        var creditCard = new CreditCard();
        var cafe = new Cafe();

        var cups = cafe.buyCoffees(creditCard, 4);

        americanExpress.process(cups._2);
    }

    static class Cafe {
        Tuple2<Coffee, Charge> buyCoffee(CreditCard creditCard) {
            var cup = new Coffee();
            return Tuple.of(cup, new Charge(creditCard, cup.price));
        }

        Tuple2<List<Coffee>, Charge> buyCoffees(CreditCard creditCard, int n) {
            var purchases = List.fill(n, buyCoffee(creditCard));
            var coffeesAndCharges = purchases.unzip(Function.identity());
            return coffeesAndCharges.map2(charges -> charges.reduce(Charge::combine));
        }
    }

    static class Coffee {
        BigDecimal price = new BigDecimal(1);
    }

    record CreditCard() {
    }

    record Charge(CreditCard creditCard, BigDecimal price) {

        Charge combine(Charge other) {
            if (creditCard.equals(other.creditCard)) {
                return new Charge(creditCard, price.add(other.price));
            }
            throw new IllegalArgumentException("Can't combine charges to different cards");
        }
    }

    private interface PaymentProvider {
        static PaymentProvider americanExpress() {
            return new PaymentProvider() {
                @Override
                public void process(Charge charge) {
                    try {
                        Thread.sleep(2_000);
                        if (Math.random() > 0.2) throw new IllegalStateException("The server does not respond");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        default void process(Charge charge) {
        }
    }
}

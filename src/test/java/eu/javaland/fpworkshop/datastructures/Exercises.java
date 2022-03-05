package eu.javaland.fpworkshop.datastructures;

import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static eu.javaland.fpworkshop.datastructures.List.listOf;
import static io.vavr.API.TODO;
import static io.vavr.concurrent.Future.failed;
import static io.vavr.concurrent.Future.successful;
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

    // Exercise 3
    // Implement a function which removes the first n items from the list
    @Test
    void droppingThreeItemsFromAListOfFourItemsShouldBeAListOfOneItem(){
        var list = listOf(1,2,3,4);

        var actual = list.drop(3);

        assertThat(actual, equalTo(listOf(4)));
    }

    @Test
    void droppingThreeItemsFromAnEmptyListShouldBeTheEmptyList(){
        var list = listOf();

        var actual = list.drop(3);

        assertThat(actual, equalTo(listOf()));
    }

    @Test
    void droppingThreeItemsFromAListOfTwoItemsListShouldBeTheEmptyList(){
        var list = listOf(1, 2);

        var actual = list.drop(3);

        assertThat(actual, equalTo(listOf()));
    }

    @Test
    void droppingZeroItemsFromAListOfTwoItemsListShouldBeTheSameList(){
        var list = listOf(1, 2);

        var actual = list.drop(0);

        assertThat(actual, equalTo(listOf(1, 2)));
    }

    @Test
    void droppingMinusTwoItemsFromAListOfTwoItemsListShouldBeTheSameList(){
        var list = listOf(1, 2);

        var actual = list.drop(-2);

        assertThat(actual, equalTo(listOf(1, 2)));
    }

    // Exercise 4
    // Implement a function sum which computes the sum of a list of integers
    // Tip: Use function foldLeft
    @Test
    void sumShouldBe6ForListOneTwoThree(){
        var list = listOf(1,2,3);

        var sum = sum(list);

        assertThat(sum, is(equalTo(6)));
    }

    @Test
    void sumShouldBe0ForEmptyList(){
        var list = List.<Integer>listOf();

        var sum = sum(list);

        assertThat(sum, is(equalTo(0)));
    }

    private int sum(List<Integer> list) {
        return TODO();
    }

    // Exercise 5
    // Implement a function product which computes the sum of a list of integers
    // Tip: Use function foldLeft
    @Test
    void productShouldBe6ForListOneTwoThree(){
        var list = listOf(1,2,3);

        var sum = product(list);

        assertThat(sum, is(equalTo(6)));
    }

    @Test
    void productShouldBe0ForEmptyList(){
        var list = List.<Integer>listOf();

        var sum = sum(list);

        assertThat(sum, is(equalTo(0)));
    }

    private int product(List<Integer> list) {
        return TODO();
    }

    // Exercise 6
    // Implement a function fill which contains n values supplied by the given Supplier
    @Test
    void aListFilledWithThreeTimesOneShouldBeTheSameAsAListOfThreeOnes(){
        var list = List.fill(3, () -> 1);

        assertThat(list, is(equalTo(List.listOf(1,1,1))));
    }

    // Exercise 7
    // Consider the implementations of sum and product.
    // We say a type A, a function op and an element zero of A form Monoid, if and only if
    // * op(op(x, y), z) == op(x, op(y, z)) for any x, y and z of type A
    // * op(zero, x) == op(x, zero) for any x of type A
    // For sum this is: Type = Integer, op = + and zero = 0
    // For product this is: Type = Integer, op = * and zero = 1
    // Whenever there is a Monoid, it can be used to fold a list.
    //
    // Implement a function insists which repeats an asynchronous operation n times or until
    // it succeeds. Type Future forms a Monoid with op = recoverWith and zero = Future.failed().
    @Test
    public void reachPaymentProviderAfterThreeTries(){
        var maxAttempts = 5;
        var actualInvocations = new AtomicInteger(0);

        var future = insist(chargePaymentProviderAfterThirdTry(actualInvocations), maxAttempts);

        var result = future.get();
        assertThat(actualInvocations.get(), equalTo(3));
        assertThat(result, equalTo("Credit card charged"));
    }

    private Supplier<Future<String>> chargePaymentProviderAfterThirdTry(AtomicInteger actualInvocations) {
        return () -> {
                actualInvocations.getAndIncrement();
                return actualInvocations.get() >= 3 ? successful("Credit card charged") :
                        failed(new IllegalStateException("Server does not respond."));
            };
    }

    @Test
    public void exhaustRetriesWhenPaymentProviderIsNotReachable(){
        var maxAttempts = 5;
        var actualInvocations = new AtomicInteger(0);
        var paymentProviderHasANonResponsiverServer = (Supplier<Future<String>>) () -> {
            actualInvocations.getAndIncrement();
            return failed(new IllegalStateException("Server does not respond."));
        };

        insist(paymentProviderHasANonResponsiverServer, maxAttempts).await();

        assertThat(actualInvocations.get(), equalTo(maxAttempts));
    }

    public <A> Future<A> insist(Supplier<Future<A>> asyncComputation, int maxAttempts) {
        return TODO();
    }

}

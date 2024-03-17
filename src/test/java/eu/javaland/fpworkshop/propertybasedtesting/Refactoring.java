package eu.javaland.fpworkshop.propertybasedtesting;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class Refactoring {


    @Property
    public void modelBasedRefactoring(@ForAll int numbers){
        // TODO: Show how to copy code and how to create the model
    }

    // Think of an idea how could I refactor this?

    static class ToBeReFactored {

        public String reallyBadCode(int number){
            if(number > 10){
                if(number < 20){
                    return "Nice";
                }
            }
            return "Not so nice";
        }

    }
}
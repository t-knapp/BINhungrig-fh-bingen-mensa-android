package de.fhbingen.mensa.Exceptions;

/**
 * Created by rana on 15.02.14.
 */
public class NotExcepectedServerAnswer extends Exception {
    public NotExcepectedServerAnswer(){

    }

    public NotExcepectedServerAnswer(String s){
        super(s);
    }
}

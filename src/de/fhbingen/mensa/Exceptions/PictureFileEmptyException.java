package de.fhbingen.mensa.Exceptions;

/**
 * Created by tknapp on 27.03.14.
 */
public class PictureFileEmptyException extends Exception {

    public PictureFileEmptyException(){

    }

    public PictureFileEmptyException(String msg){
        super(msg);
    }
}

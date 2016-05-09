package ro.idp.dashboard.ui.components;


import java.util.Observable;

/**
 * Created by alexandru.trifan on 08.05.2016.
 */
public class MessageObservable extends Observable {

    private static MessageObservable instance = null;
    public static MessageObservable getInstance() {
        if(instance == null) {
            instance = new MessageObservable();
        }

        return instance;
    }

    private MessageObservable() {
        super();
    }

    public void changeData(Object data) {
        setChanged();
        notifyObservers(data);
    }
}

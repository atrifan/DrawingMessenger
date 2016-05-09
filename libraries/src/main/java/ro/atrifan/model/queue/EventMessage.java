package ro.atrifan.model.queue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Enti on 2/24/2016.
 */
public class EventMessage implements Serializable{
    private Events eventType;
    private Map<String, String> data = new HashMap<String, String>();


    public Events getEventType() {
        return eventType;
    }

    public void setEventType(Events eventType) {
        this.eventType = eventType;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(String key, String value) {
        this.data.put(key, value);
    }

}

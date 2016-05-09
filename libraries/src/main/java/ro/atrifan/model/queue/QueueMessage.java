package ro.atrifan.model.queue;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueueMessage implements Serializable{
    Events eventType;
}

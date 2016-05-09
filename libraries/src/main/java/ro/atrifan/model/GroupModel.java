package ro.atrifan.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alexandru.trifan on 25.04.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupModel implements Serializable{
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

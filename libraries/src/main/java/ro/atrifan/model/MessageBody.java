package ro.atrifan.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class MessageBody implements Serializable{
    private String message;
    private String author;
    private Date createdOn;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}

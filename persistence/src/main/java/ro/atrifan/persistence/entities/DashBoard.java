package ro.atrifan.persistence.entities;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import ro.atrifan.model.DrawingsModel;
import ro.atrifan.model.MessagesModel;

import javax.persistence.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@Entity
@TypeDefs({ @TypeDef(name = "DrawingsJsonObject", typeClass = JSONDrawingsModel.class),
            @TypeDef(name = "MessagesJsonObject", typeClass = JSONMessagesModel.class)})
public class DashBoard {

    @Id
    @GeneratedValue
    private long id;

    @OneToOne
    private Group group;

    @Type(type = "DrawingsJsonObject")
    private DrawingsModel drawings;

    @Type(type = "MessagesJsonObject")
    private MessagesModel messages;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public DrawingsModel getDrawings() {
        return drawings;
    }

    public void setDrawings(DrawingsModel drawings) {
        this.drawings = drawings;
    }

    public MessagesModel getMessages() {
        return messages;
    }

    public void setMessages(MessagesModel messages) {
        this.messages = messages;
    }
}

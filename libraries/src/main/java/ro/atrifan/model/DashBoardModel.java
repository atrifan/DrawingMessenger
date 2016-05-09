package ro.atrifan.model;

/**
 * Created by alexandru.trifan on 26.04.2016.
 */
public class DashBoardModel {

    private DrawingsModel drawingsModel;
    private MessagesModel messagesModel;
    private String name;



    public MessagesModel getMessagesModel() {
        return messagesModel;
    }

    public void setMessagesModel(MessagesModel messagesModel) {
        this.messagesModel = messagesModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DrawingsModel getDrawingsModel() {
        return drawingsModel;
    }

    public void setDrawingsModel(DrawingsModel drawingsModel) {
        this.drawingsModel = drawingsModel;
    }
}

package ro.atrifan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandru.trifan on 26.04.2016.
 */
public class MessagesModel {
    private List<MessageModel> messages = new ArrayList<MessageModel>();

    public List<MessageModel> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageModel> messages) {
        this.messages = messages;
    }

    public void addMessage(MessageModel messagesModel) {
        messages.add(messagesModel);
    }
}

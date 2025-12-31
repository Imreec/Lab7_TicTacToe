package il.cshaifasweng.OCSFMediatorExample.client;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;

public class SimpleClient extends AbstractClient {
	
	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
	}

    @Override
    protected void handleMessageFromServer(Object msg) {
        // convert to our Message type
        if (msg instanceof Message) {
            Message message = (Message) msg;
            // post event to EventBus for GUI to handle
            EventBus.getDefault().post(new TaskMessageEvent(message));
        }
    }
	
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("192.168.1.219", 3000);
		}
		return client;
	}

}

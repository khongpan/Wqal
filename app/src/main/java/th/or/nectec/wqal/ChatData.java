package th.or.nectec.wqal;

import java.util.ArrayList;

/**
 * Created by Mink on 11/13/2017.
 */

public class ChatData {
    private static final ChatData ourInstance = new ChatData();
    private static ArrayList<String> mChatData = new ArrayList<String>();

    public static ChatData getInstance() {
        return ourInstance;
    }

    private ChatData() {
    }

    public void append(String s) {
        mChatData.add(s);
    }

    public String get(int index) {
        return mChatData.get(index);
    }

    public int getSize() {
        return mChatData.size();
    }


}

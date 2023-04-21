public class MessageBuilder { // this class gets all the info that needs to be sent to the client, and builds a message according to the protocol.


    public static String buildString(String action, String... vars){
        String syntaxedStr = "S|" + action + "|";
        for (String var:vars) {
            syntaxedStr += var + "|";
        }

        return syntaxedStr.length() + "|" + syntaxedStr;
    }





}


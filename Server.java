import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


//Message class
class Message{
    private String user1;
    private String user2;
    private String timestamp;
    private String status;                 //true = read, false = unread
    private String message;
    public Message(String user1, String user2, String timestamp, String status, String message){
        this.user1 = user1;
        this.user2 = user2;
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
    }
    public String getUser1(){
        return user1;
    }

    public void setUser1(String user1){
        this.user1 = user1;
    }

    public String getUser2(){
        return user2;
    }

    public void setUser2(String user2){
        this.user2 = user2;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void addTimestamp(String nTimestamp){
        timestamp = timestamp + "`" + nTimestamp;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void addMessage(String nMessage, String sender){
        message = message + "`" + sender+ ": "+nMessage;
    }
}


// User class
class User {
    private String username;
    private String password;
    private String email;
    private String friends;
    private String requests;
    private boolean online;
    public User(String username, String password, String email, String friends, String requests, boolean online) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.friends = friends;
        this.requests = requests;
        this.online = online;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRequests() {
        return requests;
    }

    public void setRequests(String requests) {
        this.requests = requests;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
// Server Class
class Server {
    // test
    public static void main(String[] args)
    {
        // will house user data obtained from csv file on startup
        ArrayList<User> users = new ArrayList<User>();
        String line = "";
        String splitBy = ",";
        // The following block takes the information in userInfo.csv and loads into the users ArrayList
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader("userInfo.csv"));
                line = br.readLine(); // gets rid of first line of userInfo.csv
                while ((line = br.readLine()) != null) {
                    String[] employee = line.split(splitBy);    // use comma as separator
                    User temp = new User(employee[0],employee[1],employee[2], employee[3], employee[4], Boolean.parseBoolean(employee[5]));
                    users.add(temp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //loads in messages from messages.csv
        ArrayList<Message> messages = new ArrayList<Message>();
        String splitLine = "|";
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader("messages.csv"));
                line = br.readLine(); // gets rid of first line of messages.csv
                while ((line = br.readLine()) != null) {
                    String[] msgLine = line.split(splitLine);           // use | as separator
                    Message temp = new Message(msgLine[0],msgLine[1],msgLine[2], msgLine[3], msgLine[4]);
                    messages.add(temp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ServerSocket server = null;

        try {

            // server is listening on port 1234
            server = new ServerSocket(1234);
            server.setReuseAddress(true);

            // running infinite loop for getting
            // client request
            while (true) {
                // socket object to receive incoming client
                // requests
                Socket client = server.accept();

                // Displaying that new client is connected
                // to server
                System.out.println("New client connected "
                        + client.getInetAddress()
                        .getHostAddress());

                // create a new thread object
                ClientHandler clientSocket
                        = new ClientHandler(client, users,messages);

                // This thread will handle the client
                // separately
                new Thread(clientSocket).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        ArrayList<User> users;
        ArrayList<Message> messages;
        boolean logIn = false;
        User currentUser= new User("","","","none", "none", false);  //logged in user details.
        Message currMessage = new Message("", "", "none", "true", "none");
        // Constructor
        public ClientHandler(Socket socket, ArrayList<User> users, ArrayList<Message> messages)
        {
            this.clientSocket = socket;
            this.users = users;
            this.messages = messages;
        }

        public void run()
        {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                // get the outputstream of client
                out = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                // get the inputstream of client
                in = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));
                // welcome message sent to user on start
                out.println("Welcome to Run Together! Please enter the word 'new' to create a new account or 'login' to log into an existing account");
                String line;
                while(!logIn) {

                    while ((line = in.readLine()) != null) {
                        if (line.equals("new")){ // New username
                            User temp = new User("","","","none", "none", false);
                            out.println("Please enter a username:");
                            boolean userNameCheck = false;
                            while (!userNameCheck) {
                                while ((line = in.readLine()) != null) {
                                    for (User x : users) {
                                        if (line.equals(x.getUsername())) line = "";
                                    }
                                    if (line.equals("")) {
                                        out.println("Username taken or invalid. Please enter a different username:");
                                    } else {
                                        temp.setUsername(line);
                                        userNameCheck = true;
                                        break;
                                    }
                                }
                            }

                            out.println("Please enter an email:");
                            boolean emailNameCheck = false;
                            while (!emailNameCheck) {
                                while ((line = in.readLine()) != null) {
                                    for (User x : users) {
                                        if (line.equals(x.getEmail())) line = "";
                                    }
                                    if (!line.contains("@")) line = "";
                                    if (line.equals("")) {
                                        out.println("Email taken or invalid. Please enter a different Email:");
                                    } else {
                                        temp.setEmail(line);
                                        emailNameCheck = true;
                                        break;
                                    }
                                }
                            }

                            out.println("Please enter a password:");
                            boolean passwordCheck = false;
                            while (!passwordCheck) {
                                while ((line = in.readLine()) != null) {
                                    if (line.length() < 7) {
                                        out.println("Password invalid. Please enter a Password with more than 8 characters:");
                                    } else {
                                        temp.setPassword(line);
                                        passwordCheck = true;
                                        break;
                                    }
                                }
                            }
                            users.add(temp);
                            logIn = true;
                            updateDatabase(users);
                            out.println("New account created successfully!");
                            currentUser = temp;
                            currentUser.setOnline(true);
                            break;
                        }
                        else if (line.equals("login")){ //Login
                            boolean trueUser = false;
                            boolean passwordCheck = false;
                            User checkLog= new User("","","","none", "none", false);
                            out.println("Please enter your username:");

                            while(!trueUser) {
                                while((line = in.readLine()) != null){
                                    for (User x : users) {
                                        if (line.equals(x.getUsername())){
                                            checkLog = x;
                                            trueUser = true;
                                            break;
                                        }
                                    }
                                    if(trueUser){
                                        break;
                                    } else {
                                        out.println("Username does not exist. Please enter a different username:");
                                    }
                                }
                            }
                            if(trueUser){
                                out.println("Please enter your password:");
                                while(!passwordCheck) {
                                    while((line = in.readLine()) != null) {
                                        if(line.equals(checkLog.getPassword())){
                                            passwordCheck = true;
                                            logIn = true;
                                            currentUser = checkLog;
                                            currentUser.setOnline(true);
                                            out.println("Log-In successful. Welcome "+ currentUser.getUsername()+".");
                                            break;
                                        } else {
                                            out.println("Password is wrong. Please enter your password again:");
                                        }
                                    }
                                }

                                break;
                            }
                        }

                        else {
                            out.println("Welcome to Run Together! Please enter the word 'new' to create a new account or 'login' to log into an existing account");
                        }
                    }

                }
                while(logIn){
                    out.println("1 to check Friends 2 to add Friends 3 to check Friend Requests 4 to check Messages 5 to create a new Message 6 to start an individual run 7 to Exit");
                    while((line = in.readLine()) != null) {

                        if (line.equals("1")) {
                            checkFriends(currentUser,out);
                            break;
                        }
                        else if (line.equals("2")) {
                            addFriends(out, in, line);
//                            boolean checkUser = false;
//                            int userIndex = 0;
//                            int currentUserIndex = users.indexOf(currentUser);
//                            out.println("Please enter a username:");
//                            while((line = in.readLine()) != null){
//                                for (User x : users) {
//                                    if (line.equals(x.getUsername())){
//                                        checkUser = true;
//                                        userIndex = users.indexOf(x);
//                                        break;
//                                    }
//                                }
//                                if (checkUser){
//                                    User temp = users.get(userIndex);
//                                    if (temp.getRequests().equals("none")) {
//                                        temp.setRequests(currentUser.getUsername());
//                                    } else {
//                                        String requests = temp.getRequests();
//                                        requests = requests + " " + currentUser.getUsername();
//                                        temp.setRequests(requests);
//                                    }
//                                    users.set(userIndex, temp);
//                                    updateDatabase(users);
//                                    break;
//                                } else {
//                                    out.println("User does not exist.");
//                                    break;
//                                }
//                            }
                            break;
                        }
                        else if (line.equals("3")) {
                            checkReq(out, in, line);
//                            out.println("Pending requests: " + currentUser.getRequests());
//                            out.println("Enter a username to accept the request.");
//                            if (currentUser.getRequests().equals("none")) break;
//                            while((line = in.readLine()) != null){
//                                if (currentUser.getRequests().contains(line) && line.matches(".*[a-zA-Z]+.*")) {
//                                    int currentUserIndex = users.indexOf(currentUser);
//                                    int userIndex = 0;
//                                    boolean checkUser = false;
//                                    for (User x : users) {
//                                        if (line.equals(x.getUsername())){
//                                            checkUser = true;
//                                            userIndex = users.indexOf(x);
//                                            break;
//                                        }
//                                    }
//                                    if (checkUser){
//                                        User temp = users.get(userIndex);
//                                        if (temp.getFriends().equals("none")) {
//                                            temp.setFriends(currentUser.getUsername());
//                                        } else {
//                                            String friends = temp.getFriends();
//                                            friends = friends + " " + currentUser.getUsername();
//                                            temp.setFriends(friends);
//                                        }
//                                        if (currentUser.getFriends().equals("none")) {
//                                            currentUser.setFriends(temp.getUsername());
//                                        } else {
//                                            String friends = currentUser.getFriends();
//                                            friends = friends + " " + temp.getUsername();
//                                            currentUser.setFriends(friends);
//                                            String[] requests = currentUser.getRequests().split(" ");
//                                            String updatedRequests = "";
//                                            for (String r:requests) {
//                                                if (!r.equals(temp.getUsername())) {
//                                                    updatedRequests += r;
//                                                    updatedRequests += " ";
//                                                }
//                                            }
//                                            if (updatedRequests.length() == 0) {
//                                                currentUser.setRequests("none");
//                                            } else {
//                                                currentUser.setRequests(updatedRequests);
//                                            }
//                                        }
//                                        users.set(userIndex, temp);
//                                        users.set(currentUserIndex, currentUser);
//                                        updateDatabase(users);
//                                        break;
//                                    } else {
//                                        out.println("User does not exist.");
//                                        break;
//                                    }
//                                }
//                            }
                            break;
                        }
                        else if (line.equals("4")) {
                            checkMsg(out, in, line);
//
//                            StringBuilder friendMsgList = new StringBuilder("Users you have texts with: ");
//                            boolean flag = false;
//                            for(Message x : messages){
//                                if(x.getUser1().equals(currentUser.getUsername())) {
//                                    if(x.getStatus().equals("true")) {
//                                        friendMsgList.append(x.getUser2());
//                                        friendMsgList.append("(Read) ");
//                                    } else {
//                                        friendMsgList.append(x.getUser2());
//                                        friendMsgList.append("(Unread) ");
//                                    }
//                                    flag = true;
//                                }
//                            }
//                            if(!flag) {
//                                friendMsgList.append("none");
//                            }
//                            out.println(friendMsgList.toString());
//                            out.println("Please enter a user to text with:");
//                            Message secondCurrMsg = new Message("", "", "none", "true", "none");
//                            int currentUserIndex = 0;
//                            int secCurrUserIndex = 0;
//                            boolean trueMessage = false;
//                            boolean noMessage = false;
//                            while((line = in.readLine())!= null) {
//                                if(line.equals("none")){
//                                    noMessage = true;
//                                    break;
//                                }
//                                for(Message x:messages){
//                                    if(x.getUser2().equals(currentUser.getUsername())){
//                                        if(x.getUser1().equals(line)){
//                                            secondCurrMsg = x;
//                                            secCurrUserIndex = users.indexOf(currentUser);
//                                        }
//                                    }
//                                    if(x.getUser1().equals(currentUser.getUsername())) {
//                                        if(x.getUser2().equals((line))){
//                                            currentUserIndex = users.indexOf(currentUser);
//                                            currMessage = x;
//                                            trueMessage = true;
//                                        }
//                                    }
//                                }
//                                if(trueMessage){
//                                    break;
//                                } else {
//                                    out.println("You don't have a text from this user. Please enter a user you have messages with:");
//                                }
//                            }
//                            if(noMessage) break;
//
//                            String[] msg = currMessage.getMessage().split("`");
//
//
//                            out.println("Most recent message: " + msg[msg.length-1]);
//
//
//                            out.println("Please enter your reply: ");
//                            while((line = in.readLine())!= null){
//                                currMessage.addMessage(line, currentUser.getUsername());
//                                currMessage.setStatus("true");
//                                secondCurrMsg.addMessage(line, currentUser.getUsername());
//                                secondCurrMsg.setStatus("false");
//                            }
//                            messages.set(currentUserIndex,currMessage);
//                            messages.set(secCurrUserIndex,secondCurrMsg);
//                            updateMessages(messages);
                            break;
                        }
                        else if (line.equals("5")) {
                            newMsg(out,in,line);
//                            Message tmp1 = new Message(currentUser.getUsername(), "",  "none", "true", "none");
//                            Message tmp2 = new Message("", currentUser.getUsername(),  "none", "false", "none");
//                            out.println("All friends: " + currentUser.getFriends());
//
//                            out.println("Enter a username to send a new message to:");
//
//                            boolean trueFriend = false;
//                            String friend;
//                            while(!trueFriend) {
//                                while((friend = in.readLine()) != null){
//                                    for(Message x : messages) {
//                                        if ( friend.equals(x.getUser2()) && currentUser.getUsername().equals(x.getUser1()) ) friend = "";
//                                    }
//                                    if(friend.equals("")){
//                                        out.println("You already have a message with this person. Please enter another user:");
//                                    }else {
//                                        if (currentUser.getFriends().contains(friend)){
//                                            trueFriend = true;
//                                            tmp1.setUser2(friend);
//                                            tmp2.setUser1(friend);
//                                            break;
//                                        }
//                                        else {
//                                            out.println("Username is not a friend. Please enter a different username:");
//                                        }
//                                    }
//                                }
//                            }
//                            if(trueFriend) {
//                                String text;
//                                out.println("Please enter your message for: "+tmp1.getUser2() + " and enter to send.");
//                                text = in.readLine();
//
//                                    if(text.equals('`')){
//                                        break;
//                                    }
//                                    else {
//                                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//                                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
//                                        tmp1.addTimestamp(sdf.format(timestamp));
//                                        tmp2.addTimestamp(sdf.format(timestamp));
//                                        tmp1.addMessage(text, currentUser.getUsername());
//                                        tmp2.addMessage(text, currentUser.getUsername());
//                                    }
//                            }
//                            messages.add(tmp1);
//                            messages.add(tmp2);
//                            updateMessages(messages);
//                            out.println("Message sent successfully!");
                            break;
                        }
                        else if (line.equals("6")) {
                            indRun(out);
//                            out.println("Starting 30 second Individual Run!");
//                            int countDownStarter = 30;
//                            while (countDownStarter > 0) {
//                                out.println(countDownStarter);
//                                countDownStarter--;
//                                TimeUnit.SECONDS.sleep(1);
//                            }
//                            out.println("Run Complete!");
                            break;
                        }
                        else if (line.equals("7")) {
                            out.println("Exiting Now");
                            logIn = false;
                            currentUser.setOnline(false);
                            updateDatabase(users);
                            updateMessages(messages);
                            break;
                        }
                        else {
                            out.println("Please enter a valid number");
                            break;
                        }
                    }
                }
//            while ((line = in.readLine()) != null) {
//
//                    // writing the received message from
//                    // client
//                    System.out.printf(
//                            " Sent from the client: %s\n",
//                            line);
//                    out.println("line");
//                }
            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                    currentUser.setOnline(false);
                    updateDatabase(users);
                    updateMessages(messages);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // TODO: write update messages func. similar to  updateDatabase.
        public void updateMessages(ArrayList<Message> messagess) {  //user|sender|timestamp|status|message
            try(PrintWriter writer = new PrintWriter("messages.csv")) {

                StringBuilder sb = new StringBuilder();  //  each line in messages.csv
                sb.append("user1");
                sb.append('|');
                sb.append("user2");
                sb.append('|');
                sb.append("timestamp");
                sb.append('|');
                sb.append("status");
                sb.append('|');
                sb.append("message");
                sb.append('\n');

                for(Message x: messagess) {
                    sb.append(x.getUser1());
                    sb.append('|');
                    sb.append(x.getUser2());
                    sb.append('|');
                    sb.append(x.getTimestamp());
                    sb.append('|');
                    sb.append(x.getStatus());
                    sb.append('|');
                    sb.append(x.getMessage());
                    sb.append('\n');
                }

                writer.write(sb.toString());
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }

        public void updateDatabase(ArrayList<User> users) {
            try (PrintWriter writer = new PrintWriter("userInfo.csv")) {

                StringBuilder sb = new StringBuilder();
                sb.append("Username");
                sb.append(',');
                sb.append("Password");
                sb.append(',');
                sb.append("Email");
                sb.append(',');
                sb.append("Friends");
                sb.append(',');
                sb.append("Requests");
                sb.append(',');
                sb.append("Status");
                sb.append('\n');
                for (User x: users) {
                    sb.append(x.getUsername());
                    sb.append(',');
                    sb.append(x.getPassword());
                    sb.append(',');
                    sb.append(x.getEmail());
                    sb.append(',');
                    sb.append(x.getFriends());
                    sb.append(',');
                    sb.append(x.getRequests());
                    sb.append(',');
                    sb.append(x.isOnline());
                    sb.append('\n');
                }

                writer.write(sb.toString());
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }

        public void checkFriends(User currentUser, PrintWriter out){
            out.println("All friends: " + currentUser.getFriends());
        }

        public void addFriends(PrintWriter out, BufferedReader in, String line) throws IOException {
            boolean checkUser = false;
            int userIndex = 0;
            int currentUserIndex = users.indexOf(currentUser);
            out.println("Please enter a username:");
            while((line = in.readLine()) != null){
                for (User x : users) {
                    if (line.equals(x.getUsername())){
                        checkUser = true;
                        userIndex = users.indexOf(x);
                        break;
                    }
                }
                if (checkUser){
                    User temp = users.get(userIndex);
                    if (temp.getRequests().equals("none")) {
                        temp.setRequests(currentUser.getUsername());
                    } else {
                        String requests = temp.getRequests();
                        requests = requests + " " + currentUser.getUsername();
                        temp.setRequests(requests);
                    }
                    users.set(userIndex, temp);
                    updateDatabase(users);
                    break;
                } else {
                    out.println("User does not exist.");
                    break;
                }
            }
        }

        public void checkReq(PrintWriter out, BufferedReader in, String line) throws IOException {
            out.println("Pending requests: " + currentUser.getRequests());
            out.println("Enter a username to accept the request.");
            if (currentUser.getRequests().equals("none"))
                return;

            while((line = in.readLine()) != null){
                if (currentUser.getRequests().contains(line) && line.matches(".*[a-zA-Z]+.*")) {
                    int currentUserIndex = users.indexOf(currentUser);
                    int userIndex = 0;
                    boolean checkUser = false;
                    for (User x : users) {
                        if (line.equals(x.getUsername())){
                            checkUser = true;
                            userIndex = users.indexOf(x);
                            break;
                        }
                    }
                    if (checkUser){
                        User temp = users.get(userIndex);
                        if (temp.getFriends().equals("none")) {
                            temp.setFriends(currentUser.getUsername());
                        } else {
                            String friends = temp.getFriends();
                            friends = friends + " " + currentUser.getUsername();
                            temp.setFriends(friends);
                        }
                        if (currentUser.getFriends().equals("none")) {
                            currentUser.setFriends(temp.getUsername());
                        } else {
                            String friends = currentUser.getFriends();
                            friends = friends + " " + temp.getUsername();
                            currentUser.setFriends(friends);
                            String[] requests = currentUser.getRequests().split(" ");
                            String updatedRequests = "";
                            for (String r:requests) {
                                if (!r.equals(temp.getUsername())) {
                                    updatedRequests += r;
                                    updatedRequests += " ";
                                }
                            }
                            if (updatedRequests.length() == 0) {
                                currentUser.setRequests("none");
                            } else {
                                currentUser.setRequests(updatedRequests);
                            }
                        }
                        users.set(userIndex, temp);
                        users.set(currentUserIndex, currentUser);
                        updateDatabase(users);
                        break;
                    } else {
                        out.println("User does not exist.");
                        break;
                    }
                }
            }
        }

        public void checkMsg(PrintWriter out, BufferedReader in, String line) throws IOException {

            StringBuilder friendMsgList = new StringBuilder("Users you have texts with: ");
            boolean flag = false;
            for(Message x : messages){
                if(x.getUser1().equals(currentUser.getUsername())) {
                    if(x.getStatus().equals("true")) {
                        friendMsgList.append(x.getUser2());
                        friendMsgList.append("(Read) ");
                    } else {
                        friendMsgList.append(x.getUser2());
                        friendMsgList.append("(Unread) ");
                    }
                    flag = true;
                }
            }
            if(!flag) {
                friendMsgList.append("none");
            }
            out.println(friendMsgList.toString());
            out.println("Please enter a user to text with:");
            Message secondCurrMsg = new Message("", "", "none", "true", "none");
            int currentUserIndex = 0;
            int secCurrUserIndex = 0;
            boolean trueMessage = false;
            boolean noMessage = false;
            while((line = in.readLine())!= null) {
                if(line.equals("none")){
                    noMessage = true;
                    break;
                }
                for(Message x:messages){
                    if(x.getUser2().equals(currentUser.getUsername())){
                        if(x.getUser1().equals(line)){
                            secondCurrMsg = x;
                            secCurrUserIndex = users.indexOf(currentUser);
                        }
                    }
                    if(x.getUser1().equals(currentUser.getUsername())) {
                        if(x.getUser2().equals((line))){
                            currentUserIndex = users.indexOf(currentUser);
                            currMessage = x;
                            trueMessage = true;
                        }
                    }
                }
                if(trueMessage){
                    break;
                } else {
                    out.println("You don't have a text from this user. Please enter a user you have messages with:");
                }
            }
            if(noMessage) return;

            String[] msg = currMessage.getMessage().split("`");


            out.println("Most recent message: " + msg[msg.length-1]);


            out.println("Please enter your reply: ");
            while((line = in.readLine())!= null){
                currMessage.addMessage(line, currentUser.getUsername());
                currMessage.setStatus("true");
                secondCurrMsg.addMessage(line, currentUser.getUsername());
                secondCurrMsg.setStatus("false");
                break;
            }
            messages.set(currentUserIndex,currMessage);
            messages.set(secCurrUserIndex,secondCurrMsg);
            updateMessages(messages);
            out.println("Message sent successfully!");
        }

        public void newMsg(PrintWriter out, BufferedReader in, String line) throws IOException{
            Message tmp1 = new Message(currentUser.getUsername(), "",  "none", "true", "none");
            Message tmp2 = new Message("", currentUser.getUsername(),  "none", "false", "none");
            out.println("All friends: " + currentUser.getFriends());

            out.println("Enter a username to send a new message to:");

            boolean trueFriend = false;
            String friend;
            while(!trueFriend) {
                while((friend = in.readLine()) != null){
                    for(Message x : messages) {
                        if ( friend.equals(x.getUser2()) && currentUser.getUsername().equals(x.getUser1()) ) friend = "";
                    }
                    if(friend.equals("")){
                        out.println("You already have a message with this person. Please enter another user:");
                    }else {
                        if (currentUser.getFriends().contains(friend)){
                            trueFriend = true;
                            tmp1.setUser2(friend);
                            tmp2.setUser1(friend);
                            break;
                        }
                        else {
                            out.println("Username is not a friend. Please enter a different username:");
                        }
                    }
                }
            }
            if(trueFriend) {
                String text;
                out.println("Please enter your message for: "+tmp1.getUser2() + " and enter to send.");
                text = in.readLine();

                if(text.equals('`')){
                    return;
                }
                else {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                    tmp1.addTimestamp(sdf.format(timestamp));
                    tmp2.addTimestamp(sdf.format(timestamp));
                    tmp1.addMessage(text, currentUser.getUsername());
                    tmp2.addMessage(text, currentUser.getUsername());
                }
            }
            messages.add(tmp1);
            messages.add(tmp2);
            updateMessages(messages);
            out.println("Message sent successfully!");
        }

        public void indRun(PrintWriter out) throws InterruptedException, IOException {

            out.println("Starting 30 second Individual Run!");
            int countDownStarter = 30;

            while (countDownStarter > 0) {
                out.println(countDownStarter);
                countDownStarter--;
                TimeUnit.SECONDS.sleep(1);
            }
            out.println("Run Complete!");
        }
    }
}

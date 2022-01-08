package org.company.threads;

import lombok.Data;
import org.company.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** First, see: {@link ReadModeHandler}
 *  This class whose instances can be executed by threats (and in particular an executor service), is responsible for
 *  initiating a thread that handles a Callable ReadModeHandler instance. More importantly though, this class prints
 *  the messages stored in a user's inbox.
 *  @author Theofanis Gkoufas
 */

@Data
public class UserInboxPrinter implements Callable<Boolean> {

    private User user;
    /** This list helps in determining which message has been displayed and which has not. That is because even if we
     *  removed a message from the user's inbox, it wouldn't make a change since these operations are being executed from
     *  the side of the client (meaning that we cannot expect changes to be saved on an object that was received by the
     *  server, because we do not alter the object that is stored on the server's main memory, but rather the object in the
     *  client's main memory, which serves as a copy). That is why when an operation requires altering of an object, it
     *  happens in the server side.
     */
    private List<String> messagesDisplayed;

    public UserInboxPrinter(User user) {
        this.user = user;
        messagesDisplayed = new ArrayList<>();
    }

    /*
        The loop below (ln44-54) will keep executing as long as the thread that handles the ReadModeHandler
         object is not finished. When it finishes, then it shuts down the thread (ln56) and returns true so that
         it will indicate that the read-mode should close.
    */
    @Override
    public Boolean call() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new ReadModeHandler());
        while (!future.isDone()) {
            if (!this.getUser().getUserInbox().isEmpty()) {
                for (String message :
                        this.getUser().getUserInbox()) {
                    if (!isAlreadyDisplayed(message)) {
                        System.out.println(message);
                    }
                    addMessage(message);
                }
            }
        }
        executorService.shutdown();
        return true;
    }

    /** Add a message to the messagesDisplayed list.
     * @param message The message that has already been displayed.
     */
    public void addMessage(String message) {
        messagesDisplayed.add(message);
    }

    /** Checks whether a message has already been displayed or not.
     * @param message The sequence of characters (message) that we are questioning.
     * @return True or False.
     */
    public boolean isAlreadyDisplayed(String message) {
        return messagesDisplayed.contains(message);
    }

}
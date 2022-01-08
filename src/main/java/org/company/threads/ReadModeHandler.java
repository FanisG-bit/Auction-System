package org.company.threads;

import lombok.Data;
import java.util.Scanner;
import java.util.concurrent.Callable;

/** When the user chooses the readInbox command, then the program is in a state where it continuously receives the user's
 *  object (which provides access to that person's inbox). The program will then display any new messages that are in
 *  the user's inbox. In order to close the read-inbox "mode", the user should press 'x' on the keyboard.
 *  @author Theofanis Gkoufas
 */

@Data
public class ReadModeHandler implements Callable<Boolean> {

    /** Waits for the user to press 'x' on the keyboard, in order to return a value that will indicate the exit from
     * the read-inbox "mode" (command).
     * @return true when the user types 'x'.
     */
    @Override
    public Boolean call() {
        String line;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("Type 'x' to exit read-inbox mode");
            line = scanner.nextLine();
            if (!line.equalsIgnoreCase("x")) {
                System.out.println("Not a valid command. The only valid statement inside read-mode is 'x'.");
            }
        }while (!line.equalsIgnoreCase("x"));
        return true;
    }

}
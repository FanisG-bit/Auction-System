package org.company.service;

import lombok.Data;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Data
public class HandleInbox implements Callable<Boolean> {

    @Override
    public Boolean call() {
        String line;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("Type 'x' to exit read-inbox mode");
            line = scanner.nextLine();
        }while (!line.equalsIgnoreCase("x"));
        return true;
    }
}
package org.company.service;

import lombok.Data;
import org.company.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Data
public class PrintInbox implements Callable<Boolean> {

    private User user;
    private List<String> messagesDisplayed;

    public PrintInbox(User user) {
        this.user = user;
        messagesDisplayed = new ArrayList<>();
    }

    @Override
    public Boolean call() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new HandleInbox());
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

    public void addMessage(String message) {
        messagesDisplayed.add(message);
    }

    public boolean isAlreadyDisplayed(String message) {
        return messagesDisplayed.contains(message);
    }

}
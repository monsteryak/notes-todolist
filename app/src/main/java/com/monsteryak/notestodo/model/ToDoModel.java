package com.monsteryak.notestodo.model;

public class ToDoModel extends ToDoTaskId {

    private String task , due;
    private int status;

    public String getTask() {
        return task;
    }

    public String getDue() {
        return due;
    }

    public int getStatus() {
        return status;
    }

}

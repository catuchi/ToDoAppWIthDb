package com.example.todoappwithdb;

public class Todo {
    private int id;
    private String task;
    private boolean urgent;

    public Todo(String task, boolean urgent) {
        this.task = task;
        this.urgent = urgent;
    }

    public Todo(int id, String task, boolean urgent) {
        this.id = id;
        this.task = task;
        this.urgent = urgent;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

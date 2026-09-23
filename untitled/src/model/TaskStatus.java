package model;

public enum TaskStatus {
    TODO("К выполнению"),
    IN_PROGRESS("В процессе"),
    DONE("Завершено");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

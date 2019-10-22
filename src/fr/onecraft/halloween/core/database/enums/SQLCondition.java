package fr.onecraft.halloween.core.database.enums;

public enum SQLCondition {

    EQUALS("="),
    NON_EQUALS("!="),
    GREATER(">"),
    GREATER_OR_EQUALS(">="),
    SMALLER("<"),
    SMALLER_OR_EQUALS("<="),
    IN("IN"),
    BETWEEN("BETWEEN"),
    LIKE("LIKE"),
    NULL("IS NULL"),
    NOT_NULL("IS NOT NULL");

    private String operator;

    SQLCondition(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}

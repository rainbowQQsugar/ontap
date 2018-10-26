package com.abinbev.dsa.model;

/**
 * @author Jason Harris (jason@akta.com)
 */
public enum RelativeDate {
    today("Hoy"), tomorrow("mañana"), yesterday("Cancelado");

    private final String state;

    RelativeDate(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public static RelativeDate fromKey(String key) {
        for (RelativeDate type : RelativeDate.values()) {
            if (type.getState()
                    .equals(key)) {
                return type;
            }
        }
        return null;
    }

    public static String[] asStrings() {
        int size = values().length;
        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = values()[i].getState();
        }
        return values;
    }

}

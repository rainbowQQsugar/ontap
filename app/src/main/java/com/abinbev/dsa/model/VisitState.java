package com.abinbev.dsa.model;

/**
 * @author Jason Harris (jason@akta.com)
 */
public enum VisitState {
    open("Abierto"), completed("Completado"), cancelled("Cancelado");

    private final String state;

    VisitState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public static VisitState fromKey(String key) {
        for (VisitState type : VisitState.values()) {
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
